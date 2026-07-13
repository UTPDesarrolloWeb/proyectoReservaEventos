package com.evently.pago.service;

import com.evently.pago.client.ReservaClient;
import com.evently.pago.dto.ReservaDTO;
import com.evently.pago.dto.UsuarioDTO;
import com.evently.pago.dto.EventoDTO;
import com.evently.pago.model.EstadoPago;
import com.evently.pago.model.MetodoPago;
import com.evently.pago.model.Pago;
import com.evently.pago.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;


@Service
public class PagoService {
    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaClient reservaClient;

    @Autowired
    private com.evently.pago.client.EventoClient eventoClient;

    @Autowired
    private com.evently.pago.client.AuthClient authClient;

    @Autowired
    private com.evently.pago.client.NotificacionClient notificacionClient;

    @Autowired
    private PDFService pdfService;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagoService.class);

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.trim().isEmpty() && !secretKey.contains("tu_stripe_secret_key")) {
            Stripe.apiKey = secretKey;
        }
    }


    private static final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

    public Pago procesarPago(Long reservaId, MetodoPago metodo, String transaccionExternaId) {
        synchronized (reservaId.toString().intern()) {
            // Verificar si la reserva existe y obtener datos mediante FeignClient
            ReservaDTO reserva = reservaClient.obtenerPorId(reservaId);

            if (reserva == null) {
                throw new RuntimeException("Reserva no encontrada");
            }

            // Si ya hay un pago COMPLETADO, no volver a procesar
            java.util.Optional<Pago> pagoExistenteOpt = pagoRepository.findByReservaId(reservaId);
            if (pagoExistenteOpt.isPresent()) {
                Pago pagoExistente = pagoExistenteOpt.get();
                if (pagoExistente.getEstado() == EstadoPago.COMPLETADO) {
                    log.info("Pago para reserva #{} ya estaba COMPLETADO, omitiendo.", reservaId);
                    return pagoExistente;
                }
                // Si estaba PENDIENTE, lo marcamos como COMPLETADO
                log.info("Actualizando pago PENDIENTE a COMPLETADO para reserva #{}", reservaId);
                pagoExistente.setEstado(EstadoPago.COMPLETADO);
                pagoExistente.setTransaccionId(transaccionExternaId != null ? transaccionExternaId : "ST-" + UUID.randomUUID());
                Pago pagoActualizado = pagoRepository.save(pagoExistente);

                // Ejecutamos la confirmación lenta y notificaciones en segundo plano para liberar el hilo HTTP inmediatamente
                executor.submit(() -> {
                    try {
                        reservaClient.confirmarReserva(reservaId);
                        // Re-obtener la reserva para traer el QR generado
                        ReservaDTO reservaConfirmada = reservaClient.obtenerPorId(reservaId);
                        enviarNotificacionPagoAprobado(reservaConfirmada, pagoActualizado);
                    } catch (Exception e) {
                        log.error("Error en procesamiento asincrono de reserva #{}: ", reservaId, e);
                    }
                });

                return pagoActualizado;
            }

            Pago pago = new Pago();
            pago.setReservaId(reservaId);
            pago.setMonto(reserva.getMontoTotal());
            pago.setMetodoPago(metodo);

            if (transaccionExternaId == null || transaccionExternaId.isEmpty()) {
                pago.setTransaccionId("TX-" + UUID.randomUUID().toString());
            } else {
                pago.setTransaccionId(transaccionExternaId);
            }

            pago.setEstado(EstadoPago.COMPLETADO);

            Double comisionPorcentaje = 5.0;
            Double comisionPlataforma = pago.getMonto() * (comisionPorcentaje / 100);
            pago.setComisionPlataforma(comisionPlataforma);
            pago.setMontoOrganizador(pago.getMonto() - comisionPlataforma);

            Pago pagoGuardado = pagoRepository.save(pago);

            // Ejecutamos la confirmación lenta y notificaciones en segundo plano
            executor.submit(() -> {
                try {
                    reservaClient.confirmarReserva(reservaId);
                    // Re-obtener la reserva para traer el QR generado
                    ReservaDTO reservaConfirmada = reservaClient.obtenerPorId(reservaId);
                    enviarNotificacionPagoAprobado(reservaConfirmada, pagoGuardado);
                } catch (Exception e) {
                    log.error("Error en procesamiento asincrono de reserva #{}: ", reservaId, e);
                }
            });

            return pagoGuardado;
        }
    }

    private void enviarNotificacionPagoAprobado(ReservaDTO reserva, Pago pago) {
        try {
            UsuarioDTO usuario = authClient.obtenerUsuarioPorId(reserva.getClienteId());
            EventoDTO evento = eventoClient.obtenerPorId(reserva.getEventoId());

            String mensaje = String.format(
                    "💳 ¡Pago Aprobado!\n\nTu pago de S/. %.2f para el evento \"%s\" fue procesado exitosamente.\nDetalle de reserva: #%d\nTransacción: %s\n\n¡Gracias por tu compra! 🎟️",
                    pago.getMonto(),
                    evento != null ? evento.getTitulo() : "Evento",
                    reserva.getId(),
                    pago.getTransaccionId()
            );

            // Generar PDF y convertir a Base64
            String pdfBase64 = null;
            if (usuario != null && evento != null) {
                try {
                    byte[] pdfBytes = pdfService.generarBoletaPago(pago, reserva, usuario, evento);
                    pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
                } catch (Exception pdfEx) {
                    log.error("⚠️ Error al generar PDF para adjunto de correo: {}", pdfEx.getMessage());
                }
            }

            Map<String, Object> notifRequest = new HashMap<>();
            notifRequest.put("usuarioId", reserva.getClienteId());
            notifRequest.put("mensaje", mensaje);
            notifRequest.put("tipo", "CONFIRMACION_PAGO");
            notifRequest.put("telefono", usuario != null ? usuario.getTelefono() : null);
            notifRequest.put("email", usuario != null ? usuario.getEmail() : null);
            if (pdfBase64 != null) {
                notifRequest.put("pdfBase64", pdfBase64);
                notifRequest.put("pdfNombre", "boleta-reserva-" + reserva.getId() + ".pdf");
            }

            notificacionClient.enviarWhatsApp(notifRequest);
            log.info("📱 Notificación WhatsApp + correo con PDF enviados para pago de reserva #{}", reserva.getId());
        } catch (Exception e) {
            log.warn("⚠️ No se pudo enviar la notificación para el pago de la reserva #{}: {}", reserva.getId(), e.getMessage());
        }
    }

    public Pago procesarReembolso(Long reservaId) {
        Pago pago = pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado para esta reserva"));

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new RuntimeException("Solo se pueden reembolsar pagos completados");
        }

        pago.setEstado(EstadoPago.REEMBOLSADO);
        Pago pagoActualizado = pagoRepository.save(pago);

        // Cancela la reserva a traves de FeignClient
        reservaClient.cancelarReserva(reservaId);

        return pagoActualizado;
    }

    public List<Pago> listarPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    public Pago obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public List<Pago> misPagos(List<Long> reservaIds) {
        return pagoRepository.findByReservaIdIn(reservaIds);
    }

    public Page<Pago> misPagosPaginados(List<Long> reservaIds, int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(pagina, cantidad, Sort.by("fechaPago").descending());
        return pagoRepository.findByReservaIdIn(reservaIds, pageable);
    }

    public Page<Pago> todosLosPagos(int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(pagina, cantidad, Sort.by("fechaPago").descending());
        return pagoRepository.findAll(pageable);
    }

    public Map<String, String> crearSessionPago(Long reservaId) {
        ReservaDTO reserva = reservaClient.obtenerPorId(reservaId);
        if (reserva == null) {
            throw new RuntimeException("Reserva no encontrada");
        }

        EventoDTO evento = eventoClient.obtenerPorId(reserva.getEventoId());
        if (evento == null) {
            throw new RuntimeException("Evento no encontrado");
        }

        try {
            Stripe.apiKey = secretKey;

            double precioUnitario = evento.getPrecio() > 0 ? evento.getPrecio() : (reserva.getMontoTotal() / reserva.getCantidadEntradas());
            long montoCentavos = Math.round(precioUnitario * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "&reservaId=" + reservaId)
                    .setCancelUrl(cancelUrl + "&reservaId=" + reservaId)
                    .setClientReferenceId(reservaId.toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity((long) reserva.getCantidadEntradas())
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("pen")
                                                    .setUnitAmount(montoCentavos)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Entradas para: " + evento.getTitulo())
                                                                    .setDescription("Evento: " + evento.getTitulo())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("sessionUrl", session.getUrl());
            return response;
        } catch (Exception e) {
            log.error("Error al crear sesión de Stripe: ", e);
            throw new RuntimeException("Error al crear sesión de pago en Stripe: " + e.getMessage());
        }
    }

    public void procesarWebhookStripe(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Error al validar firma de webhook de Stripe: ", e);
            throw new RuntimeException("Firma de webhook inválida: " + e.getMessage());
        }

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                String externalReference = session.getClientReferenceId();
                if (externalReference == null || externalReference.trim().isEmpty()) {
                    throw new RuntimeException("Referencia externa vacía en el webhook de Stripe");
                }

                Long reservaId = Long.parseLong(externalReference);
                synchronized (reservaId.toString().intern()) {
                    String paymentId = session.getId();
                    double amount = session.getAmountTotal() / 100.0;

                    java.util.Optional<Pago> pagoExistenteOpt = pagoRepository.findByReservaId(reservaId);
                    ReservaDTO reserva = reservaClient.obtenerPorId(reservaId);

                    if (pagoExistenteOpt.isPresent()) {
                        Pago pagoExistente = pagoExistenteOpt.get();
                        if (pagoExistente.getEstado() == EstadoPago.COMPLETADO) {
                            return; // ya procesado
                        }
                        pagoExistente.setEstado(EstadoPago.COMPLETADO);
                        pagoExistente.setTransaccionId("ST-" + paymentId);
                        pagoExistente.setMonto(amount);
                        Pago pagoGuardado = pagoRepository.save(pagoExistente);
                        reservaClient.confirmarReserva(reservaId);
                        enviarNotificacionPagoAprobado(reserva, pagoGuardado);
                    } else {
                        Pago pago = new Pago();
                        pago.setReservaId(reservaId);
                        pago.setMonto(amount);
                        pago.setMetodoPago(MetodoPago.STRIPE);
                        pago.setTransaccionId("ST-" + paymentId);
                        pago.setEstado(EstadoPago.COMPLETADO);

                        Double comisionPorcentaje = 5.0; // 5% por defecto
                        Double comisionPlataforma = pago.getMonto() * (comisionPorcentaje / 100);
                        pago.setComisionPlataforma(comisionPlataforma);
                        pago.setMontoOrganizador(pago.getMonto() - comisionPlataforma);

                        Pago pagoGuardado = pagoRepository.save(pago);
                        reservaClient.confirmarReserva(reservaId);
                        enviarNotificacionPagoAprobado(reserva, pagoGuardado);
                    }
                }
            }
        }
    }
}
