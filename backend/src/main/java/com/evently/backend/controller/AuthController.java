package com.evently.backend.controller;

import com.evently.backend.dto.LoginRequest;
import com.evently.backend.dto.RegisterRequest;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Cerrar sesión
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Sesión cerrada exitosamente");
        response.put("instruccion", "Elimina el token del almacenamiento local");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Ver perfil - cualquier usuario autenticado
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> miPerfil(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado"));

        return ResponseEntity.ok(usuario);
    }

    // Editar perfil - cualquier usuario autenticado
    @PutMapping("/perfil")
    public ResponseEntity<Usuario> editarPerfil(
            @RequestBody Usuario usuarioActualizado,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado"));

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setApellido(usuarioActualizado.getApellido());

        return ResponseEntity.ok(
                usuarioRepository.save(usuario));
    }

    @GetMapping("/usuario")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorEmail(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId());
        response.put("nombre", usuario.getNombre());
        response.put("apellido", usuario.getApellido());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol().name());

        return ResponseEntity.ok(response);
    }
}