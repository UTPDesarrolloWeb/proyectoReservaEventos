package com.evently.evento.repository;

import com.evently.evento.model.Plan;
import com.evently.evento.model.TipoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByNombre(TipoPlan nombre);
    List<Plan> findByActivoTrue();
}
