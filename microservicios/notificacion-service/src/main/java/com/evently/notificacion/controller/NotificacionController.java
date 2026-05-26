package com.evently.notificacion.controller;

import com.evently.notificacion.client.AuthClient;
import com.evently.notificacion.dto.UsuarioDTO;
import com.evently.notificacion.model.Notificacion;
import com.evently.notificacion.service.NotificacionService;
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
    private AuthClient authClient;

    private Long obtenerUsuarioId(Authentication authentication) {
        UsuarioDTO usuario = authClient.obtenerUsuarioPorEmail(authentication.getName());
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return usuario.getId();
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> misNotificaciones(
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(notificacionService.misNotificaciones(usuarioId));
    }

    @GetMapping("/sin-leer")
    public ResponseEntity<List<Notificacion>> sinLeer(
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(notificacionService.notificacionesSinLeer(usuarioId));
    }

    @GetMapping("/contador")
    public ResponseEntity<Map<String, Integer>> contador(
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        int total = notificacionService.contarSinLeer(usuarioId);
        return ResponseEntity.ok(Map.of("sinLeer", total));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }
}
