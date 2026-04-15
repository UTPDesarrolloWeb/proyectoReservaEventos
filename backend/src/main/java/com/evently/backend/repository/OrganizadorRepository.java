package com.evently.backend.repository;

import com.evently.backend.model.Organizador;
import com.evently.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
    // Busca el organizador por usuario
    Optional<Organizador> findByUsuario(Usuario usuario);
}
