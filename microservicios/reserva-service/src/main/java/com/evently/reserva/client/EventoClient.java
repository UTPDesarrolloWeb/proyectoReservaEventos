package com.evently.reserva.client;

import com.evently.reserva.dto.EventoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "evento-service", url = "http://evento-service:8082")
public interface EventoClient {

    @GetMapping("/api/eventos/{id}/interno")
    EventoDTO obtenerPorId(@PathVariable("id") Long id);

    @PutMapping("/api/eventos/{id}/aforo")
    void actualizarAforo(@PathVariable("id") Long id, @RequestParam("cantidad") int cantidad);
}
