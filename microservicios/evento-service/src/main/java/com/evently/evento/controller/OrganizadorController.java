package com.evently.evento.controller;

import com.evently.evento.client.AuthServiceClient;
import com.evently.evento.dto.UsuarioDTO;
import com.evently.evento.model.Organizador;
import com.evently.evento.model.TipoPlan;
import com.evently.evento.service.OrganizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/organizadores")
@CrossOrigin(origins = "*")
public class OrganizadorController {

    @Autowired
    private OrganizadorService organizadorService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @PostMapping("/registrar/{tipoPlan}")
    @PreAuthorize("hasAuthority('ORGANIZADOR')")
    public ResponseEntity<Organizador> registrar(@PathVariable TipoPlan tipoPlan, Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(organizadorService.registrarOrganizador(usuario.getId(), tipoPlan));
    }

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasAuthority('ORGANIZADOR')")
    public ResponseEntity<Map<String, Object>> miPerfil(Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        Organizador organizador = organizadorService.obtenerPorUsuarioId(usuario.getId());

        // Note: verification of plan expiration can be handled internally or simple check
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("nombre", usuario.getNombre());
        perfil.put("apellido", usuario.getApellido());
        perfil.put("email", usuario.getEmail());
        perfil.put("plan", organizador.getPlan().getNombre());
        perfil.put("eventosCreados", organizador.getEventosCreados());
        perfil.put("fechaVencimientoPlan", organizador.getFechaVencimientoPlan());

        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/cambiar-plan/{tipoPlan}")
    @PreAuthorize("hasAuthority('ORGANIZADOR')")
    public ResponseEntity<Organizador> cambiarPlan(@PathVariable TipoPlan tipoPlan, Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        Organizador organizador = organizadorService.obtenerPorUsuarioId(usuario.getId());
        return ResponseEntity.ok(organizadorService.cambiarPlan(organizador.getId(), tipoPlan));
    }
}
