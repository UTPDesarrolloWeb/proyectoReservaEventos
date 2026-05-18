package com.evently.backend.repository;

import com.evently.backend.model.Organizador;
import com.evently.backend.model.Usuario;
import com.evently.backend.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;


import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
    // Busca el organizador por usuario
    Optional<Organizador> findByUsuario(Usuario usuario);

    // Cuenta los organizadores por plan
    Long countByPlan(Plan plan);

    // Lista los organizadores por plan
    List<Organizador> findByPlan(Plan plan);

    // Organizadores con plan vencido
    List<Organizador> findByFechaVencimientoPlanBefore(
            LocalDateTime fecha);

    // Organizadores con plan activo
    List<Organizador> findByFechaVencimientoPlanAfter(
            LocalDateTime fecha);
}
