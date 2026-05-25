package com.evently.evento.client;

import com.evently.evento.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${app.auth-service.url:http://auth-service:8081}")
public interface AuthServiceClient {

    @GetMapping("/api/auth/usuarios/{id}")
    UsuarioDTO obtenerUsuarioPorId(@PathVariable("id") Long id);

    @GetMapping("/api/auth/usuarios/email/{email}")
    UsuarioDTO obtenerUsuarioPorEmail(@PathVariable("email") String email);
}
