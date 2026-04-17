package com.evently.backend.repository;

import com.evently.backend.model.Reserva;
import com.evently.backend.model.Evento;
import com.evently.backend.model.Usuario;
import com.evently.backend.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // Historial de reservas de un cliente
    List<Reserva> findByCliente(Usuario cliente);

    // Reservas de un evento específico
    List<Reserva> findByEvento(Evento evento);

    // Reservas confirmadas de un evento
    List<Reserva> findByEventoAndEstado(Evento evento, EstadoReserva estado);

    // Verificar si un cliente ya reservó un evento
    Boolean existsByClienteAndEvento(Usuario cliente, Evento evento);

    List<Reserva> findByEstado(EstadoReserva estado);
}
