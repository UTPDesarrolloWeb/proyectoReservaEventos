package com.evently.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://auth-service:8081}")
public interface AuthClient {
    @GetMapping("/api/auth/usuarios")
    List<Object> listarUsuarios();

    @PutMapping("/api/auth/usuarios/{id}/toggle")
    Object toggleUsuario(@PathVariable("id") Long id);
}
