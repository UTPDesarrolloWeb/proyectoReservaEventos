package com.evently.pago.repository;

import com.evently.pago.model.Plan;
import com.evently.pago.model.TipoPlan;
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
