package com.evently.evento.repository;

import com.evently.evento.model.CategoriaEvento;
import com.evently.evento.model.EstadoEvento;
import com.evently.evento.model.Evento;
import com.evently.evento.model.Organizador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByEstado(EstadoEvento estado);
    List<Evento> findByEstadoAndCategoria(EstadoEvento estado, CategoriaEvento categoria);
    List<Evento> findByOrganizador(Organizador organizador);
    List<Evento> findTop12ByEstadoOrderByFechaEventoAsc(EstadoEvento estado);

    @Query(value = "SELECT * FROM eventos WHERE estado = 'PUBLICADO' " +
            "AND (:titulo IS NULL OR LOWER(titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
            "AND (:lugar IS NULL OR LOWER(lugar) LIKE LOWER(CONCAT('%', :lugar, '%'))) " +
            "AND (:categoria IS NULL OR categoria = :categoria)",
            nativeQuery = true)
    List<Evento> buscarEventosPublicos(
            @Param("titulo") String titulo,
            @Param("lugar") String lugar,
            @Param("categoria") String categoria);

    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.estado = 'PUBLICADO' " +
            "AND e.categoria = :categoria " +
            "AND e.id NOT IN " +
            "(SELECT r.evento.id FROM Reserva r WHERE r.clienteId = :clienteId)")
    List<Evento> findRecomendados(
            @Param("categoria") CategoriaEvento categoria,
            @Param("clienteId") Long clienteId);
}
