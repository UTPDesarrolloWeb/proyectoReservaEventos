package com.evently.backend.repository;

import com.evently.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca usuario por su email, necesario para usar el JWT en el login
    Optional<Usuario> findByEmail(String email);

    // Verifica si existe un email registrado
    Boolean existsByEmail(String email);
}
