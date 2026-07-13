package com.evently.pago.client;

import com.evently.pago.dto.EventoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "evento-service", url = "${evento.service.url:http://evento-service:8082}")
public interface EventoClient {
    @GetMapping("/api/eventos/{id}/interno")
    EventoDTO obtenerPorId(@PathVariable("id") Long id);
}
