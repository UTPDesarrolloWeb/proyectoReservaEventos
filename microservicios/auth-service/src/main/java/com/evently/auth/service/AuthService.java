package com.evently.auth.service;

import com.evently.auth.dto.LoginRequest;
import com.evently.auth.dto.RegisterRequest;
import com.evently.auth.dto.Verify2faRequest;
import com.evently.auth.dto.GoogleLoginRequest;
import com.evently.auth.model.Usuario;
import com.evently.auth.repository.UsuarioRepository;
import com.evently.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    @org.springframework.beans.factory.annotation.Value("${google.client-id:}")
    private String googleClientId;

    public Map<String, Object> register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setTelefono(request.getTelefono());

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("token", token);
        response.put("rol", usuario.getRol());
        response.put("email", usuario.getEmail());

        return response;
    }

    public Map<String, Object> login(LoginRequest request) {
        // Verificar si el usuario existe y es de Google antes de intentar autenticar
        usuarioRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            if ("GOOGLE".equals(u.getProveedor())) {
                throw new RuntimeException("Esta cuenta fue creada con Google. Por favor, usa el botón \"Iniciar sesión con Google\" para acceder.");
            }
        });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener la URL de two-factor-service
        String twoFactorUrl = System.getenv("TWO_FACTOR_SERVICE_URL");
        if (twoFactorUrl == null || twoFactorUrl.isEmpty()) {
            twoFactorUrl = "http://localhost:8087";
        }

        // Llamar a two-factor-service para generar y enviar el codigo
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("email", usuario.getEmail());
            restTemplate.postForEntity(twoFactorUrl + "/api/2fa/send", body, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el código de verificación por correo: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mfaRequired", true);
        response.put("email", usuario.getEmail());
        response.put("mensaje", "Se ha enviado un código de verificación a su correo Gmail");

        return response;
    }

    public Map<String, Object> verify2fa(Verify2faRequest request) {
        // Obtener la URL de two-factor-service
        String twoFactorUrl = System.getenv("TWO_FACTOR_SERVICE_URL");
        if (twoFactorUrl == null || twoFactorUrl.isEmpty()) {
            twoFactorUrl = "http://localhost:8087";
        }

        // Verificar el codigo en two-factor-service
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put("codigo", request.getCodigo());
            restTemplate.postForEntity(twoFactorUrl + "/api/2fa/verify", body, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Código de verificación incorrecto o expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Login exitoso");
        response.put("token", token);
        response.put("rol", usuario.getRol());
        response.put("nombre", usuario.getNombre());
        response.put("email", usuario.getEmail());

        return response;
    }

    public Map<String, Object> loginGoogle(GoogleLoginRequest request) {
        String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
        Map<String, Object> tokenInfo;
        try {
            RestTemplate restTemplate = new RestTemplate();
            tokenInfo = restTemplate.getForObject(tokenInfoUrl, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al validar el token de Google: " + e.getMessage());
        }

        if (tokenInfo == null || tokenInfo.containsKey("error")) {
            String errorDetail = tokenInfo != null ? (String) tokenInfo.get("error_description") : "respuesta nula";
            throw new RuntimeException("Token de Google inválido: " + errorDetail);
        }

        // Validar la audiencia (client id) si está configurado en el backend
        // Acepta cualquiera de los Client IDs de OAuth del mismo proyecto Google (mismo número de proyecto)
        if (googleClientId != null && !googleClientId.isEmpty() && !googleClientId.equals("TU_GOOGLE_CLIENT_ID")) {
            String aud = (String) tokenInfo.get("aud");
            // Extraer el número de proyecto de ambos IDs para comparar si son del mismo proyecto
            String projectNumberConfigured = googleClientId.split("-")[0];
            String projectNumberToken = aud != null ? aud.split("-")[0] : "";
            if (!projectNumberConfigured.equals(projectNumberToken)) {
                throw new RuntimeException("La audiencia del token de Google no coincide con el proyecto configurado. Token aud: " + aud + ", configurado: " + googleClientId);
            }
        }

        String email = (String) tokenInfo.get("email");
        String nombre = (String) tokenInfo.get("given_name");
        String familyName = (String) tokenInfo.get("family_name");
        String fullName = (String) tokenInfo.get("name");

        String apellidoParsed = familyName;
        if (apellidoParsed == null || apellidoParsed.trim().isEmpty()) {
            if (fullName != null && nombre != null && fullName.startsWith(nombre)) {
                apellidoParsed = fullName.substring(nombre.length()).trim();
            }
        }
        
        final String apellido = (apellidoParsed != null && !apellidoParsed.trim().isEmpty()) ? apellidoParsed : "Google";

        // Buscar o registrar al usuario si es nuevo
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setNombre(nombre != null && !nombre.trim().isEmpty() ? nombre : "Google User");
            nuevo.setApellido(apellido);
            nuevo.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            nuevo.setRol(com.evently.auth.model.Rol.CLIENTE);
            nuevo.setActivo(true);
            nuevo.setProveedor("GOOGLE"); // Marcar como cuenta de Google
            return usuarioRepository.save(nuevo);
        });

        // Si la cuenta ya existe y el proveedor es nulo, la actualizamos a GOOGLE
        if (usuario.getProveedor() == null) {
            usuario.setProveedor("GOOGLE");
            usuario = usuarioRepository.save(usuario);
        }

        // Si la cuenta ya existe pero era LOCAL, no la sobreescribimos para permitir que entre localmente o por Google
        if ("LOCAL".equals(usuario.getProveedor())) {
            // El usuario ya tiene cuenta local con ese email — dejar que entre normalmente con 2FA
        }

        if (!usuario.getActivo()) {
            throw new RuntimeException("El usuario está inactivo");
        }

        // Obtener la URL de two-factor-service
        String twoFactorUrl = System.getenv("TWO_FACTOR_SERVICE_URL");
        if (twoFactorUrl == null || twoFactorUrl.isEmpty()) {
            twoFactorUrl = "http://localhost:8087";
        }

        // Llamar a two-factor-service para generar y enviar el codigo OTP por correo
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("email", usuario.getEmail());
            restTemplate.postForEntity(twoFactorUrl + "/api/2fa/send", body, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el código de verificación por correo: " + e.getMessage());
        }

        // Retornar mfaRequired para que el frontend muestre el formulario de OTP
        Map<String, Object> response = new HashMap<>();
        response.put("mfaRequired", true);
        response.put("email", usuario.getEmail());
        response.put("mensaje", "Se ha enviado un código de verificación a su correo Gmail");

        return response;
    }
}
