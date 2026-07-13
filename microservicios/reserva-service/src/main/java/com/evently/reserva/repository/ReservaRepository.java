package com.evently.reserva.repository;

import com.evently.reserva.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteId(Long clienteId);
    Page<Reserva> findByClienteId(Long clienteId, Pageable pageable);
    List<Reserva> findByEventoId(Long eventoId);
    boolean existsByClienteIdAndEventoId(Long clienteId, Long eventoId);
    boolean existsByClienteIdAndEventoIdAndEstadoIn(Long clienteId, Long eventoId, java.util.List<com.evently.reserva.model.EstadoReserva> estados);
}
