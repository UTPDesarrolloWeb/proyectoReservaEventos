package com.evently.backend.controller;

import com.evently.backend.model.EstadoPago;
import com.evently.backend.model.MetodoPago;
import com.evently.backend.model.Pago;
import com.evently.backend.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {
    @Autowired
    private PagoService pagoService;

    // Procesa el pago - uso del cliente
    @PostMapping("/procesar/{reservaId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Pago> procesarPago(
            @PathVariable Long reservaId,
            @RequestParam MetodoPago metodoPago) {

        // Simulamos un ID de transacción
        String transaccionId = "SIM-" + UUID.randomUUID().toString();

        return ResponseEntity.ok(
                pagoService.procesarPago(reservaId,
                        metodoPago, transaccionId));
    }

    // Procesa el reembolso - uso del cliente
    @PutMapping("/reembolso/{reservaId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Pago> procesarReembolso(
            @PathVariable Long reservaId) {

        return ResponseEntity.ok(
                pagoService.procesarReembolso(reservaId));
    }

    // Ver pago de una reserva
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<Pago> verPago(
            @PathVariable Long reservaId) {

        return ResponseEntity.ok(
                pagoService.obtenerPagoPorReserva(reservaId));
    }

    // Lista los pagos por estado - uso del admin
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pago>> listarPorEstado(
            @PathVariable EstadoPago estado) {

        return ResponseEntity.ok(
                pagoService.listarPagosPorEstado(estado));
    }
}
