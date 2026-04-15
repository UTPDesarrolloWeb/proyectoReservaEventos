package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {
    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private NotificacionService notificacionService;

    // Procesa el pago
    public Pago procesarPago(Long reservaId, MetodoPago metodoPago,
                             String transaccionId) {

        Reserva reserva = reservaService.obtenerPorId(reservaId);

        // Verifica que la reserva este pendiente
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new RuntimeException(
                    "La reserva no está pendiente de pago");
        }

        // Crea el pago
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMonto(reserva.getMontoTotal());
        pago.setMetodoPago(metodoPago);
        pago.setTransaccionId(transaccionId);
        pago.setEstado(EstadoPago.COMPLETADO);

        // Guarda el pago
        Pago pagoGuardado = pagoRepository.save(pago);

        // Confirma la reserva
        reservaService.confirmarReserva(reservaId);

        // Envia la notificación al cliente
        notificacionService.enviarNotificacion(
                reserva.getCliente(),
                "Tu pago fue procesado exitosamente para: " +
                        reserva.getEvento().getTitulo(),
                TipoNotificacion.CONFIRMACION_PAGO
        );

        return pagoGuardado;
    }

    // Procesa el reembolso
    public Pago procesarReembolso(Long reservaId) {

        Reserva reserva = reservaService.obtenerPorId(reservaId);

        Pago pago = pagoRepository.findByReserva(reserva)
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado para esta reserva"));

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new RuntimeException(
                    "Solo se pueden reembolsar pagos completados");
        }

        pago.setEstado(EstadoPago.REEMBOLSADO);
        Pago pagoActualizado = pagoRepository.save(pago);

        // Cancela la reserva
        reservaService.cancelarReserva(reservaId,
                reserva.getCliente());

        // Notifica al cliente
        notificacionService.enviarNotificacion(
                reserva.getCliente(),
                "Tu reembolso fue procesado para: " +
                        reserva.getEvento().getTitulo(),
                TipoNotificacion.REEMBOLSO
        );

        return pagoActualizado;
    }

    // Historial de pagos por estado
    public List<Pago> listarPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    // Obtener pago por reserva
    public Pago obtenerPagoPorReserva(Long reservaId) {
        Reserva reserva = reservaService.obtenerPorId(reservaId);
        return pagoRepository.findByReserva(reserva)
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado"));
    }
}
