package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private QRService qrService;

    public Reserva crearReserva(Long eventoId, int cantidadEntradas,
                                Usuario cliente) {

        Evento evento = eventoService.obtenerPorId(eventoId);

        // Verifica que el evento está publicado
        if (evento.getEstado() != EstadoEvento.PUBLICADO &&
                evento.getEstado() != EstadoEvento.AGOTADO) {
            throw new RuntimeException(
                    "El evento no está disponible para reservas");
        }

        // Verifica que hay aforo disponible
        if (evento.getAforoDisponible() < cantidadEntradas) {
            throw new RuntimeException(
                    "No hay suficientes entradas disponibles");
        }

        // Verifica que el cliente no haya reservado ya
        if (reservaRepository.existsByClienteAndEvento(cliente, evento)) {
            throw new RuntimeException(
                    "Ya tienes una reserva para este evento");
        }

        // Reduce el aforo disponible para el evento
        evento.setAforoDisponible(
                evento.getAforoDisponible() - cantidadEntradas);

        // Si se agotó el aforo cambia de estado
        if (evento.getAforoDisponible() == 0) {
            evento.setEstado(EstadoEvento.AGOTADO);
        }

        // Crea la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEvento(evento);
        reserva.setCantidadEntradas(cantidadEntradas);
        reserva.setEstado(EstadoReserva.PENDIENTE);

        return reservaRepository.save(reserva);
    }

    // Confirma la reserva después del pago
    public Reserva confirmarReserva(Long reservaId) {
        Reserva reserva = obtenerPorId(reservaId);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        // Generar QR con los datos de la reserva
        String contenidoQR = String.format(
                "EVENTLY|RESERVA:%d|EVENTO:%s|CLIENTE:%s|ENTRADAS:%d|MONTO:%.2f",
                reserva.getId(),
                reserva.getEvento().getTitulo(),
                reserva.getCliente().getEmail(),
                reserva.getCantidadEntradas(),
                reserva.getMontoTotal()
        );

        reserva.setCodigoQR(qrService.generarQR(contenidoQR));
        return reservaRepository.save(reserva);
    }

    // Cancela la reserva
    public Reserva cancelarReserva(Long reservaId, Usuario cliente) {
        Reserva reserva = obtenerPorId(reservaId);

        // Verifica que es su reserva
        if (!reserva.getCliente().getEmail().equals(cliente.getEmail())) {
            throw new RuntimeException(
                    "No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new RuntimeException("La reserva ya está cancelada");
        }

        // Devolver aforo al evento
        Evento evento = reserva.getEvento();
        evento.setAforoDisponible(
                evento.getAforoDisponible() + reserva.getCantidadEntradas());

        // Si estaba agotado vuelve a publicarse
        if (evento.getEstado() == EstadoEvento.AGOTADO) {
            evento.setEstado(EstadoEvento.PUBLICADO);
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    // Historial de reservas del cliente
    public List<Reserva> misReservas(Usuario cliente) {
        return reservaRepository.findByCliente(cliente);
    }

    // Reservas de un evento - uso del organizador
    public List<Reserva> reservasPorEvento(Long eventoId, Usuario usuario) {
        Evento evento = eventoService.obtenerPorId(eventoId);

        // Verificar que es su evento
        if (!evento.getOrganizador().getUsuario()
                .getEmail().equals(usuario.getEmail())) {
            throw new RuntimeException(
                    "No tienes permiso para ver estas reservas");
        }

        return reservaRepository.findByEvento(evento);
    }

    // Obtiene su reserva por id
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Reserva no encontrada con id: " + id));
    }
}
