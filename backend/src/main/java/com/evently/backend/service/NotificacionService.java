package com.evently.backend.service;

import com.evently.backend.model.Notificacion;
import com.evently.backend.model.TipoNotificacion;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {
    @Autowired
    private NotificacionRepository notificacionRepository;

    // Envia la notificación
    public Notificacion enviarNotificacion(Usuario usuario,
                                           String mensaje,
                                           TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setLeida(false);
        return notificacionRepository.save(notificacion);
    }

    // Lista las notificaciones del usuario
    public List<Notificacion> misNotificaciones(Usuario usuario) {
        return notificacionRepository.findByUsuario(usuario);
    }

    // Lista las notificaciones no leídas
    public List<Notificacion> notificacionesSinLeer(Usuario usuario) {
        return notificacionRepository.findByUsuarioAndLeidaFalse(usuario);
    }

    // Marcar la notificación como leída
    public Notificacion marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository
                .findById(notificacionId)
                .orElseThrow(() -> new RuntimeException(
                        "Notificación no encontrada"));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    // Cuenta las notificaciones sin leer
    public int contarSinLeer(Usuario usuario) {
        return notificacionRepository
                .findByUsuarioAndLeidaFalse(usuario).size();
    }
}
