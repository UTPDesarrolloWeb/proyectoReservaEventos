package com.evently.backend.controller;

import com.evently.backend.model.Notificacion;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {
    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Ver mis notificaciones
    @GetMapping
    public ResponseEntity<List<Notificacion>> misNotificaciones(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                notificacionService.misNotificaciones(usuario));
    }

    // Ver las notificaciones no leídas
    @GetMapping("/sin-leer")
    public ResponseEntity<List<Notificacion>> sinLeer(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                notificacionService.notificacionesSinLeer(usuario));
    }

    // Cuenta las notificaciones sin leer
    @GetMapping("/contador")
    public ResponseEntity<Map<String, Integer>> contador(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int total = notificacionService.contarSinLeer(usuario);
        return ResponseEntity.ok(Map.of("sinLeer", total));
    }

    // Marca las notificación como leída
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificacionService.marcarComoLeida(id));
    }
}
