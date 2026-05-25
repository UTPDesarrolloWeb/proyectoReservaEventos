package com.evently.pago.service;

import com.evently.pago.client.ReservaClient;
import com.evently.pago.dto.ReservaDTO;
import com.evently.pago.dto.UsuarioDTO;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PagoService {
    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaClient reservaClient;

    public Pago procesarPago(Long reservaId, MetodoPago metodo, String transaccionExternaId) {
        // Verificar si la reserva existe y obtener datos mediante FeignClient
        ReservaDTO reserva = reservaClient.obtenerPorId(reservaId);

        if (reserva == null) {
            throw new RuntimeException("Reserva no encontrada");
        }

        // Si ya hay un pago en proceso o completado, fallamos
        if (pagoRepository.findByReservaId(reservaId).isPresent()) {
            throw new RuntimeException("Ya existe un pago asociado a esta reserva");
        }

        Pago pago = new Pago();
        pago.setReservaId(reservaId);
        pago.setMonto(reserva.getMontoTotal());
        pago.setMetodoPago(metodo);

        // Simulamos la pasarela de pagos
        if (transaccionExternaId == null || transaccionExternaId.isEmpty()) {
            pago.setTransaccionId("TX-" + UUID.randomUUID().toString());
        } else {
            pago.setTransaccionId(transaccionExternaId);
        }

        pago.setEstado(EstadoPago.COMPLETADO);

        // Por simplicidad, seteamos un porcentaje de comisin fijo, ya que no tenemos acceso directo al Plan del Organizador aca
        // En un caso real, podramos buscar la comisin en base al organizador del evento
        Double comisionPorcentaje = 5.0; // 5% por defecto

        Double comisionPlataforma = pago.getMonto() * (comisionPorcentaje / 100);
        pago.setComisionPlataforma(comisionPlataforma);
        pago.setMontoOrganizador(pago.getMonto() - comisionPlataforma);

        Pago pagoGuardado = pagoRepository.save(pago);

        // Confirmamos la reserva en el microservicio de reservas
        reservaClient.confirmarReserva(reservaId);

        return pagoGuardado;
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
}
