package com.evently.notificacion.service;

import com.evently.notificacion.model.Notificacion;
import com.evently.notificacion.model.TipoNotificacion;
import com.evently.notificacion.repository.NotificacionRepository;
import com.evently.notificacion.websocket.NotificacionSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {
    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private NotificacionSocket notificacionSocket;

    public Notificacion enviarNotificacion(Long usuarioId, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(usuarioId);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setLeida(false);

        Notificacion guardada = notificacionRepository.save(notificacion);

        // Enviar en tiempo real via WebSocket
        notificacionSocket.enviarNotificacionUsuario(usuarioId, mensaje);

        return guardada;
    }

    public List<Notificacion> misNotificaciones(Long usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId);
    }

    public List<Notificacion> notificacionesSinLeer(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public Notificacion marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository
                .findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    public int contarSinLeer(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalse(usuarioId).size();
    }
}
