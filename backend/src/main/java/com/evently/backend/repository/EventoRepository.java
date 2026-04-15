package com.evently.backend.repository;

import com.evently.backend.model.Evento;
import com.evently.backend.model.EstadoEvento;
import com.evently.backend.model.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Lista los eventos publicados para los clientes
    List<Evento> findByEstado(EstadoEvento estado);

    // Lista los eventos de un organizador en específico
    List<Evento> findByOrganizador(Organizador organizador);

    // Lista los eventos del organizador por estado
    List<Evento> findByOrganizadorAndEstado(Organizador organizador, EstadoEvento estado);
}
