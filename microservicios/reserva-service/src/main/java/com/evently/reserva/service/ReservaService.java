package com.evently.reserva.service;

import com.evently.reserva.client.AuthClient;
import com.evently.reserva.client.EventoClient;
import com.evently.reserva.client.NotificacionClient;
import com.evently.reserva.dto.EventoDTO;
import com.evently.reserva.dto.UsuarioDTO;
import com.evently.reserva.model.EstadoReserva;
import com.evently.reserva.model.Reserva;
import com.evently.reserva.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
    private static final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EventoClient eventoClient;

    @Autowired
    private QRService qrService;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private NotificacionClient notificacionClient;

    public Reserva crearReserva(Long eventoId, int cantidadEntradas, UsuarioDTO cliente) {

        EventoDTO evento = eventoClient.obtenerPorId(eventoId);

        if (!"PUBLICADO".equals(evento.getEstado()) && !"AGOTADO".equals(evento.getEstado())) {
            throw new RuntimeException("El evento no está disponible para reservas");
        }

        if (evento.getAforoDisponible() < cantidadEntradas) {
            throw new RuntimeException("No hay suficientes entradas disponibles");
        }

        if (reservaRepository.existsByClienteIdAndEventoIdAndEstadoIn(
                cliente.getId(), eventoId,
                java.util.List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))) {
            throw new RuntimeException("Ya tienes una reserva activa para este evento");
        }

        // Reduce el aforo disponible para el evento a través del Feign client
        eventoClient.actualizarAforo(eventoId, -cantidadEntradas);

        Reserva reserva = new Reserva();
        reserva.setClienteId(cliente.getId());
        reserva.setEventoId(eventoId);
        reserva.setCantidadEntradas(cantidadEntradas);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setMontoTotal(evento.getPrecio() * cantidadEntradas);

        return reservaRepository.save(reserva);
    }

    public Reserva confirmarReserva(Long reservaId) {
        Reserva reserva = obtenerPorId(reservaId);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        EventoDTO evento = eventoClient.obtenerPorId(reserva.getEventoId());

        String contenidoQR = String.format(
                "EVENTLY|RESERVA:%d|EVENTO:%s|CLIENTE_ID:%d|ENTRADAS:%d|MONTO:%.2f",
                reserva.getId(),
                evento.getTitulo(),
                reserva.getClienteId(),
                reserva.getCantidadEntradas(),
                reserva.getMontoTotal()
        );

        reserva.setCodigoQR(qrService.generarQR(contenidoQR));
        Reserva reservaConfirmada = reservaRepository.save(reserva);

        // Enviar notificación WhatsApp + correo de confirmación de reserva en segundo plano
        executor.submit(() -> {
            try {
                UsuarioDTO usuario = authClient.obtenerUsuarioPorId(reserva.getClienteId());
                String telefono = usuario != null ? usuario.getTelefono() : null;
                String email = usuario != null ? usuario.getEmail() : null;

                String mensaje = String.format(
                        "✅ ¡Reserva confirmada!\n\n📋 Reserva #%d\n🎫 Evento: %s\n🎟️ Entradas: %d\n💰 Total: $%.2f\n\n¡Disfruta el evento! 🎉",
                        reserva.getId(),
                        evento.getTitulo(),
                        reserva.getCantidadEntradas(),
                        reserva.getMontoTotal()
                );

                Map<String, Object> notifRequest = new HashMap<>();
                notifRequest.put("usuarioId", reserva.getClienteId());
                notifRequest.put("mensaje", mensaje);
                notifRequest.put("tipo", "CONFIRMACION_RESERVA");
                notifRequest.put("telefono", telefono);

                notificacionClient.enviarWhatsApp(notifRequest);
                log.info("📱 Notificación WhatsApp + correo enviados para reserva #{}", reservaId);
            } catch (Exception e) {
                log.warn("⚠️ No se pudo enviar notificación para reserva #{}: {}", reservaId, e.getMessage());
            }
        });

        return reservaConfirmada;
    }

    public Reserva cancelarReserva(Long reservaId, UsuarioDTO cliente) {
        Reserva reserva = obtenerPorId(reservaId);

        if (!reserva.getClienteId().equals(cliente.getId())) {
            throw new RuntimeException("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new RuntimeException("La reserva ya está cancelada");
        }

        // Devolver aforo al evento
        eventoClient.actualizarAforo(reserva.getEventoId(), reserva.getCantidadEntradas());

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> misReservas(UsuarioDTO cliente) {
        return reservaRepository.findByClienteId(cliente.getId());
    }

    public List<Reserva> reservasPorEvento(Long eventoId, UsuarioDTO usuario) {
        EventoDTO evento = eventoClient.obtenerPorId(eventoId);

        if (!evento.getOrganizadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para ver estas reservas");
        }

        return reservaRepository.findByEventoId(eventoId);
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con id: " + id));
    }

    public Page<Reserva> misReservasPaginadas(UsuarioDTO cliente, int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(pagina, cantidad, Sort.by("fechaReserva").descending());
        return reservaRepository.findByClienteId(cliente.getId(), pageable);
    }
}

