package com.evently.reserva.controller;

import com.evently.reserva.client.AuthClient;
import com.evently.reserva.dto.UsuarioDTO;
import com.evently.reserva.model.Reserva;
import com.evently.reserva.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AuthClient authClient;

    // Crea la reserva - uso del Cliente
    @PostMapping("/evento/{eventoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Reserva> crearReserva(
            @PathVariable Long eventoId,
            @RequestParam int cantidadEntradas,
            Authentication authentication) {

        UsuarioDTO cliente = authClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(reservaService.crearReserva(eventoId, cantidadEntradas, cliente));
    }

    // Cancela la reserva - uso del cliente
    @PutMapping("/{reservaId}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Reserva> cancelarReserva(
            @PathVariable Long reservaId,
            Authentication authentication) {

        UsuarioDTO cliente = authClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(reservaService.cancelarReserva(reservaId, cliente));
    }

    // Mis reservas trae todo - uso del cliente
    @GetMapping("/mis-reservas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<Reserva>> misReservas(Authentication authentication) {
        UsuarioDTO cliente = authClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(reservaService.misReservas(cliente));
    }

    // Mis reservas con paginación - uso del cliente
    @GetMapping("/mis-reservas/paginado")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<Reserva>> misReservasPaginadas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int cantidad,
            Authentication authentication) {

        UsuarioDTO cliente = authClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(reservaService.misReservasPaginadas(cliente, pagina, cantidad));
    }

    // Reservas realizadas de un evento - uso del Organizador
    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<List<Reserva>> reservasPorEvento(
            @PathVariable Long eventoId,
            Authentication authentication) {

        UsuarioDTO organizador = authClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(reservaService.reservasPorEvento(eventoId, organizador));
    }
}
