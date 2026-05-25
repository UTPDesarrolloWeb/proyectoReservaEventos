package com.evently.evento.repository;

import com.evently.evento.model.Organizador;
import com.evently.evento.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
    Optional<Organizador> findByUsuarioId(Long usuarioId);
    Long countByPlan(Plan plan);
    List<Organizador> findByPlan(Plan plan);
    List<Organizador> findByFechaVencimientoPlanBefore(LocalDateTime fecha);
    List<Organizador> findByFechaVencimientoPlanAfter(LocalDateTime fecha);
}
