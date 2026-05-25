package com.evently.notificacion.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionSocket {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Envía notificación a un usuario específico
    public void enviarNotificacionUsuario(Long usuarioId,
                                          String mensaje) {
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/" + usuarioId, mensaje);
    }

    // Envía notificación a todos los conectados
    public void enviarNotificacionGlobal(String mensaje) {
        messagingTemplate.convertAndSend(
                "/topic/global", mensaje);
    }
}
