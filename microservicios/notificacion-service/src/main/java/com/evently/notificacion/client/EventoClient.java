package com.evently.notificacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "evento-service", url = "${evento.service.url:http://evento-service:8082}")
public interface EventoClient {

    @GetMapping("/api/eventos/publicos")
    List<Map<String, Object>> obtenerEventosPublicos();

    @GetMapping("/api/eventos/{id}/interno")
    Map<String, Object> obtenerEventoPorId(@PathVariable("id") Long id);
}
