package com.evently.notificacion.client;

import com.evently.notificacion.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://auth-service:8081}")
public interface AuthClient {
    @GetMapping("/api/auth/usuario")
    UsuarioDTO obtenerUsuarioPorEmail(@RequestParam("email") String email);
}
