package com.evently.backend.repository;

import com.evently.backend.model.Notificacion;
import com.evently.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Notificaciones de un usuario
    List<Notificacion> findByUsuario(Usuario usuario);

    // Notificaciones no leídas de un usuario
    List<Notificacion> findByUsuarioAndLeidaFalse(Usuario usuario);
}
