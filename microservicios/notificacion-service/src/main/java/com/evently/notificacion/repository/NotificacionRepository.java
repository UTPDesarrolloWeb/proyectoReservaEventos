package com.evently.notificacion.repository;

import com.evently.notificacion.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Notificaciones de un usuario
    List<Notificacion> findByUsuarioId(Long usuarioId);

    // Notificaciones no leidas de un usuario
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
}
