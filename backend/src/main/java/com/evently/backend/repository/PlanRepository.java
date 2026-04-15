package com.evently.backend.repository;

import com.evently.backend.model.Plan;
import com.evently.backend.model.TipoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    // Permite buscar el plan por su Tipo
    Optional<Plan> findByNombre(TipoPlan nombre);

    // Listamos los planes activos
    List<Plan> findByActivoTrue();
}
