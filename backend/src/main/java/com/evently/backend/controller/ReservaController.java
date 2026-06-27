package com.evently.backend.controller;

import com.evently.backend.model.Reserva;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
// @CrossOrigin(origins = "*")
public class ReservaController {
        @Autowired
        private ReservaService reservaService;

        @Autowired
        private UsuarioRepository usuarioRepository;

        // Crea la reserva - uso del Cliente
        @PostMapping("/evento/{eventoId}")
        @PreAuthorize("hasAuthority('CLIENTE')")
        public ResponseEntity<Reserva> crearReserva(
                        @PathVariable Long eventoId,
                        @RequestParam int cantidadEntradas,
                        Authentication authentication) {

                Usuario cliente = usuarioRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return ResponseEntity.ok(
                                reservaService.crearReserva(eventoId,
                                                cantidadEntradas, cliente));
        }

        // Cancela la reserva - uso del cliente
        @PutMapping("/{reservaId}/cancelar")
        @PreAuthorize("hasAuthority('CLIENTE')")
        public ResponseEntity<Reserva> cancelarReserva(
                        @PathVariable Long reservaId,
                        Authentication authentication) {

                Usuario cliente = usuarioRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return ResponseEntity.ok(
                                reservaService.cancelarReserva(reservaId, cliente));
        }

        // Mis reservas trae todo - uso del cliente
        @GetMapping("/mis-reservas")
        @PreAuthorize("hasAuthority('CLIENTE')")
        public ResponseEntity<List<Reserva>> misReservas(
                        Authentication authentication) {

                Usuario cliente = usuarioRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return ResponseEntity.ok(reservaService.misReservas(cliente));
        }

        // Mis reservas con paginación - uso del cliente
        @GetMapping("/mis-reservas/paginado")
        @PreAuthorize("hasAuthority('CLIENTE')")
        public ResponseEntity<Page<Reserva>> misReservasPaginadas(
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "5") int cantidad,
                        Authentication authentication) {

                Usuario cliente = usuarioRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException(
                                                "Usuario no encontrado"));

                return ResponseEntity.ok(
                                reservaService.misReservasPaginadas(
                                                cliente, pagina, cantidad));
        }

        // Reservas realizadas de un evento - uso del Organizador
        @GetMapping("/evento/{eventoId}")
        @PreAuthorize("hasAuthority('ORGANIZADOR')")
        public ResponseEntity<List<Reserva>> reservasPorEvento(
                        @PathVariable Long eventoId,
                        Authentication authentication) {

                Usuario organizador = usuarioRepository
                                .findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return ResponseEntity.ok(
                                reservaService.reservasPorEvento(eventoId, organizador));
        }
}
