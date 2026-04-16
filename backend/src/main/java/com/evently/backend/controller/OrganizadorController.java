package com.evently.backend.controller;

import com.evently.backend.model.Organizador;
import com.evently.backend.model.TipoPlan;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.service.OrganizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizadores")
@CrossOrigin(origins = "*")
public class OrganizadorController {
    @Autowired
    private OrganizadorService organizadorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Registra como organizador eligiendo un plan
    @PostMapping("/registrar/{tipoPlan}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Organizador> registrar(
            @PathVariable TipoPlan tipoPlan,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                organizadorService.registrarOrganizador(
                        usuario.getId(), tipoPlan));
    }

    // Ver mi perfil de organizador
    @GetMapping("/mi-perfil")
    public ResponseEntity<Organizador> miPerfil(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                organizadorService.obtenerPorUsuario(usuario));
    }

    // Cambiar de plan
    @PutMapping("/cambiar-plan/{tipoPlan}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Organizador> cambiarPlan(
            @PathVariable TipoPlan tipoPlan,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Organizador organizador = organizadorService
                .obtenerPorUsuario(usuario);

        return ResponseEntity.ok(
                organizadorService.cambiarPlan(
                        organizador.getId(), tipoPlan));
    }
}
