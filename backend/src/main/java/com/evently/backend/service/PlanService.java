package com.evently.backend.service;

import com.evently.backend.model.Plan;
import com.evently.backend.model.TipoPlan;
import com.evently.backend.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {
    @Autowired
    private PlanRepository planRepository;

    // Lista todos los planes activos
    public List<Plan> listarPlanes() {
        return planRepository.findByActivoTrue();
    }

    // Obtiene el plan por su respectivo id
    public Plan obtenerPlanPorId(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Plan no encontrado con id: " + id));
    }

    // Obtiene el plan por tipo
    public Plan obtenerPlanPorTipo(TipoPlan tipo) {
        return planRepository.findByNombre(tipo)
                .orElseThrow(() -> new RuntimeException(
                        "Plan no encontrado: " + tipo));
    }

    // Crear plan - uso del Admin
    public Plan crearPlan(Plan plan) {
        return planRepository.save(plan);
    }

    // Actualiza el Plan - uso del Admin
    public Plan actualizarPlan(Long id, Plan planActualizado) {
        Plan plan = obtenerPlanPorId(id);
        plan.setNombre(planActualizado.getNombre());
        plan.setPrecio(planActualizado.getPrecio());
        plan.setLimiteEventos(planActualizado.getLimiteEventos());
        plan.setDescripcion(planActualizado.getDescripcion());
        plan.setComisionPorcentaje(planActualizado.getComisionPorcentaje());
        return planRepository.save(plan);
    }

    // Desactiva el plan - udo del Admin
    public void desactivarPlan(Long id) {
        Plan plan = obtenerPlanPorId(id);
        plan.setActivo(false);
        planRepository.save(plan);
    }
}
