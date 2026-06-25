package com.evently.reserva.client;

import com.evently.reserva.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", url = "http://auth-service:8081")
public interface AuthClient {
    @GetMapping("/api/auth/usuario")
    UsuarioDTO obtenerUsuarioPorEmail(@RequestParam("email") String email);
}
