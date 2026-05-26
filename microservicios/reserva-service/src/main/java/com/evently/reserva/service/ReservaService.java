package com.evently.reserva.service;

import com.evently.reserva.client.EventoClient;
import com.evently.reserva.dto.EventoDTO;
import com.evently.reserva.dto.UsuarioDTO;
import com.evently.reserva.model.EstadoReserva;
import com.evently.reserva.model.Reserva;
import com.evently.reserva.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EventoClient eventoClient;

    @Autowired
    private QRService qrService;

    public Reserva crearReserva(Long eventoId, int cantidadEntradas, UsuarioDTO cliente) {

        EventoDTO evento = eventoClient.obtenerPorId(eventoId);

        if (!"PUBLICADO".equals(evento.getEstado()) && !"AGOTADO".equals(evento.getEstado())) {
            throw new RuntimeException("El evento no está disponible para reservas");
        }

        if (evento.getAforoDisponible() < cantidadEntradas) {
            throw new RuntimeException("No hay suficientes entradas disponibles");
        }

        if (reservaRepository.existsByClienteIdAndEventoId(cliente.getId(), eventoId)) {
            throw new RuntimeException("Ya tienes una reserva para este evento");
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
        return reservaRepository.save(reserva);
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
