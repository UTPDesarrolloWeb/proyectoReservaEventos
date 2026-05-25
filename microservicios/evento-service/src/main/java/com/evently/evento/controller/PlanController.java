package com.evently.evento.controller;

import com.evently.evento.model.Plan;
import com.evently.evento.service.PlanService;
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

    @GetMapping
    public ResponseEntity<List<Plan>> listarPlanes() {
        return ResponseEntity.ok(planService.listarPlanes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> obtenerPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planService.obtenerPlanPorId(id));
    }

    @PostMapping
    public ResponseEntity<Plan> crearPlan(@RequestBody Plan plan) {
        return ResponseEntity.ok(planService.crearPlan(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plan> actualizarPlan(@PathVariable Long id, @RequestBody Plan plan) {
        return ResponseEntity.ok(planService.actualizarPlan(id, plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> desactivarPlan(@PathVariable Long id) {
        planService.desactivarPlan(id);
        return ResponseEntity.ok("Plan desactivado correctamente");
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> reactivarPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planService.reactivarPlan(id));
    }
}
