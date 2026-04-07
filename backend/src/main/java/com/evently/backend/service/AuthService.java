package com.evently.backend.service;

import com.evently.backend.dto.LoginRequest;
import com.evently.backend.dto.RegisterRequest;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Registro de nuevo usuario
    public Map<String, Object> register(RegisterRequest request) {

        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear el nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());

        // Guardar en BD
        usuarioRepository.save(usuario);

        // Generar token
        String token = jwtUtil.generateToken(usuario.getEmail());

        // Preparar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("token", token);
        response.put("rol", usuario.getRol());

        return response;
    }

    // Login de usuario existente
    public Map<String, Object> login(LoginRequest request) {

        // Spring Security verifica el email y la contraseña
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si termina aqui quiere decir que las credenciales son correctas
        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar token
        String token = jwtUtil.generateToken(usuario.getEmail());

        // Preparar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Login exitoso");
        response.put("token", token);
        response.put("rol", usuario.getRol());
        response.put("nombre", usuario.getNombre());

        return response;
    }
}
