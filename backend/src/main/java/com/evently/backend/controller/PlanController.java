package com.evently.backend.controller;

import com.evently.backend.model.Plan;
import com.evently.backend.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@CrossOrigin(origins = "*")
public class PlanController {
    @Autowired
    private PlanService planService;

    // Lista todos los planes activos
    @GetMapping
    public ResponseEntity<List<Plan>> listarPlanes() {
        return ResponseEntity.ok(planService.listarPlanes());
    }

    // Obtiene el plan con respecto a su id
    @GetMapping("/{id}")
    public ResponseEntity<Plan> obtenerPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planService.obtenerPlanPorId(id));
    }

    // Crea el Plan - uso del admin
    @PostMapping
    public ResponseEntity<Plan> crearPlan(@RequestBody Plan plan) {
        return ResponseEntity.ok(planService.crearPlan(plan));
    }

    // Actualiza el plan - uso del admin
    @PutMapping("/{id}")
    public ResponseEntity<Plan> actualizarPlan(
            @PathVariable Long id,
            @RequestBody Plan plan) {
        return ResponseEntity.ok(planService.actualizarPlan(id, plan));
    }

    // Desactiva el plan - uso del admin
    @DeleteMapping("/{id}")
    public ResponseEntity<String> desactivarPlan(@PathVariable Long id) {
        planService.desactivarPlan(id);
        return ResponseEntity.ok("Plan desactivado correctamente");
    }

    // Reactivamos el plan - uso del admin
    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> reactivarPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planService.reactivarPlan(id));
    }
}
