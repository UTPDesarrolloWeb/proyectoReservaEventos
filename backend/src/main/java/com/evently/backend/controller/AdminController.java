package com.evently.backend.controller;

import com.evently.backend.model.Organizador;
import com.evently.backend.model.TipoPlan;
import com.evently.backend.model.Usuario;
import com.evently.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Dashboard administrativo
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // Lista todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    // Lista organizadores por plan
    @GetMapping("/organizadores/{tipoPlan}")
    public ResponseEntity<List<Organizador>> organizadoresPorPlan(
            @PathVariable TipoPlan tipoPlan) {
        return ResponseEntity.ok(
                adminService.organizadoresPorPlan(tipoPlan));
    }

    // Activa o desactiva usuario
    @PutMapping("/usuarios/{id}/toggle")
    public ResponseEntity<Usuario> toggleUsuario(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUsuario(id));
    }
}
