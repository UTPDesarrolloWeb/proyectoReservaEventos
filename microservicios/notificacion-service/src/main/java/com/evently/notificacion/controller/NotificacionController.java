package com.evently.notificacion.controller;

import com.evently.notificacion.client.AuthClient;
import com.evently.notificacion.dto.UsuarioDTO;
import com.evently.notificacion.dto.WhatsAppRequest;
import com.evently.notificacion.model.Notificacion;
import com.evently.notificacion.model.TipoNotificacion;
import com.evently.notificacion.service.EmailService;
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

    @Autowired
    private EmailService emailService;

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

    /**
     * Endpoint interno para que otros microservicios envíen notificaciones con WhatsApp y correo.
     * No requiere autenticación de usuario final (llamado service-to-service).
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<Map<String, Object>> enviarWhatsApp(@RequestBody WhatsAppRequest request) {
        TipoNotificacion tipo;
        try {
            tipo = TipoNotificacion.valueOf(request.getTipo());
        } catch (Exception e) {
            tipo = TipoNotificacion.CONFIRMACION_RESERVA;
        }

        Notificacion notificacion = notificacionService.enviarNotificacionConWhatsApp(
                request.getUsuarioId(),
                request.getMensaje(),
                tipo,
                request.getTelefono()
        );

        // Enviar correo si viene el email
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String asunto = tipo == TipoNotificacion.CONFIRMACION_PAGO
                    ? "Evently - Pago Confirmado"
                    : "Evently - Reserva Confirmada";

            // Eliminar emojis del mensaje usando regex (unicode symbols y pictographs)
            String mensajeSinEmojis = request.getMensaje()
                    .replaceAll("[\\p{So}\\p{Cn}]", "")
                    .replace("✅", "")
                    .replace("📋", "")
                    .replace("🎫", "")
                    .replace("🎟️", "")
                    .replace("🎉", "")
                    .replace("💰", "")
                    .replace("💳", "");

            String lineasMensaje = mensajeSinEmojis.replace("\n", "<br>");
            
            String html = "<div style=\"font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 30px 20px; background-color: #f4f5f8;\">"
                    + "  <div style=\"background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #eef2f5;\">"
                    // Cabecera
                    + "    <div style=\"background: #4f46e5; padding: 30px; text-align: center;\">"
                    + "      <h1 style=\"color: #ffffff; margin: 0; font-size: 26px; font-weight: 700; letter-spacing: -0.5px;\">Evently</h1>"
                    + "      <p style=\"color: rgba(255,255,255,0.85); margin: 5px 0 0 0; font-size: 14px;\">Confirmacion de Reserva</p>"
                    + "    </div>"
                    // Cuerpo
                    + "    <div style=\"padding: 35px 30px; color: #1f2937; line-height: 1.6;\">"
                    + "      <h2 style=\"margin-top: 0; color: #111827; font-size: 18px; font-weight: 600;\">Estimado(a) cliente,</h2>"
                    + "      <p style=\"font-size: 15px; color: #4b5563; margin-bottom: 25px;\">" + lineasMensaje + "</p>"
                    + "      <div style=\"background-color: #f9fafb; border-left: 4px solid #4f46e5; border-radius: 6px; padding: 15px 20px; margin: 25px 0; font-size: 14px; color: #374151;\">"
                    + "        Su entrada en formato PDF ha sido adjuntada a este correo. Puede presentarla impresa o en su dispositivo movil para el ingreso al evento."
                    + "      </div>"
                    + "    </div>"
                    // Pie
                    + "    <div style=\"background-color: #fafbfe; padding: 20px; text-align: center; border-top: 1px solid #f1f3f7; font-size: 12px; color: #9ca3af;\">"
                    + "      Este es un correo automatico de Evently. Por favor no responda a este mensaje.<br>"
                    + "      &copy; 2026 Evently. Todos los derechos reservados."
                    + "    </div>"
                    + "  </div>"
                    + "</div>";

            emailService.enviarCorreo(
                request.getEmail(), 
                asunto, 
                html, 
                request.getPdfBase64(), 
                request.getPdfNombre()
            );
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Notificación enviada",
                "notificacionId", notificacion.getId(),
                "whatsappEnviado", request.getTelefono() != null && !request.getTelefono().isEmpty(),
                "correoEnviado", request.getEmail() != null && !request.getEmail().isEmpty()
        ));
    }
}

