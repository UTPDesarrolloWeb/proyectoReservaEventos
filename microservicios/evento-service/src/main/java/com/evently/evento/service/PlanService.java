package com.evently.evento.service;

import com.evently.evento.model.Plan;
import com.evently.evento.model.TipoPlan;
import com.evently.evento.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlanService {
    @Autowired
    private PlanRepository planRepository;

    public List<Plan> listarPlanes() {
        return planRepository.findByActivoTrue();
    }

    public Plan obtenerPlanPorId(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con id: " + id));
    }

    public Plan obtenerPlanPorTipo(TipoPlan tipo) {
        return planRepository.findByNombre(tipo)
                .orElseGet(() -> {
                    Plan nuevo = new Plan();
                    nuevo.setNombre(tipo);
                    nuevo.setPrecio(0.0);
                    nuevo.setLimiteEventos(50);
                    nuevo.setActivo(true);
                    nuevo.setComisionPorcentaje(5.0);
                    nuevo.setDescripcion("Plan " + tipo.name() + " autogenerado");
                    return planRepository.save(nuevo);
                });
    }

    public Plan crearPlan(Plan plan) {
        return planRepository.save(plan);
    }

    public Plan actualizarPlan(Long id, Plan planActualizado) {
        Plan plan = obtenerPlanPorId(id);
        plan.setNombre(planActualizado.getNombre());
        plan.setPrecio(planActualizado.getPrecio());
        plan.setLimiteEventos(planActualizado.getLimiteEventos());
        plan.setDescripcion(planActualizado.getDescripcion());
        plan.setComisionPorcentaje(planActualizado.getComisionPorcentaje());
        return planRepository.save(plan);
    }

    public void desactivarPlan(Long id) {
        Plan plan = obtenerPlanPorId(id);
        plan.setActivo(false);
        planRepository.save(plan);
    }

    public Plan reactivarPlan(Long id) {
        Plan plan = obtenerPlanPorId(id);
        plan.setActivo(true);
        return planRepository.save(plan);
    }
}
