package com.evently.admin.controller;

import com.evently.admin.service.AdminService;
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

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Object>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @GetMapping("/organizadores/{tipoPlan}")
    public ResponseEntity<List<Object>> organizadoresPorPlan(@PathVariable String tipoPlan) {
        return ResponseEntity.ok(adminService.organizadoresPorPlan(tipoPlan));
    }

    @PutMapping("/usuarios/{id}/toggle")
    public ResponseEntity<Object> toggleUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUsuario(id));
    }

    @GetMapping("/pagos")
    public ResponseEntity<Object> historialPagos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {
        return ResponseEntity.ok(adminService.historialPagos(pagina, cantidad));
    }

    @GetMapping("/ingresos")
    public ResponseEntity<Object> ingresosPorPeriodo(
            @RequestParam(defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(adminService.ingresosPorPeriodo(periodo));
    }

    @GetMapping("/eventos")
    public ResponseEntity<Object> eventosPorEstado(
            @RequestParam(defaultValue = "PUBLICADO") String estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {
        return ResponseEntity.ok(adminService.eventosPorEstado(estado, pagina, cantidad));
    }

    @GetMapping("/organizadores/estado-plan")
    public ResponseEntity<Object> organizadoresPorEstadoPlan() {
        return ResponseEntity.ok(adminService.organizadoresPorEstadoPlan());
    }
}
