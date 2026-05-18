package com.evently.backend.repository;

import com.evently.backend.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Lista los eventos publicados para los clientes
    List<Evento> findByEstado(EstadoEvento estado);

    // Lista los eventos de un organizador en específico
    List<Evento> findByOrganizador(Organizador organizador);

    // Trae los próximos 12 eventos ordenados por fecha para la portada
    List<Evento> findTop12ByEstadoOrderByFechaEventoAsc(EstadoEvento estado);

    // Busca eventos publicados con filtros opcionales de título, lugar y categoría
    // Si no se envía un filtro, ese campo se ignora y trae todos los resultados
    @Query(value = "SELECT * FROM eventos WHERE estado = 'PUBLICADO' " +
            "AND (:titulo IS NULL OR LOWER(titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
            "AND (:lugar IS NULL OR LOWER(lugar) LIKE LOWER(CONCAT('%', :lugar, '%'))) " +
            "AND (:categoria IS NULL OR categoria = :categoria)",
            nativeQuery = true)
    List<Evento> buscarEventosPublicos(
            @Param("titulo") String titulo,
            @Param("lugar") String lugar,
            @Param("categoria") String categoria);

    // Busca eventos de la misma categoría excluyendo los ya reservados
    @Query("SELECT e FROM Evento e WHERE e.estado = 'PUBLICADO' " +
            "AND e.categoria = :categoria " +
            "AND e.id NOT IN " +
            "(SELECT r.evento.id FROM Reserva r WHERE r.cliente = :cliente)")
    List<Evento> findRecomendados(
            @Param("categoria") CategoriaEvento categoria,
            @Param("cliente") Usuario cliente);

    // Eventos por estado con paginación
    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);
}
