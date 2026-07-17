package com.evently.pago.client;

import com.evently.pago.dto.ReservaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// @FeignClient(name = "reserva-service", url = "http://reserva-service:8083")
@FeignClient(name = "reserva-service", url = "${reserva.service.url:http://reserva-service:8083}")
public interface ReservaClient {
    @GetMapping("/api/reservas/{id}")
    ReservaDTO obtenerPorId(@PathVariable("id") Long id);

    @PutMapping("/api/reservas/{id}/confirmar")
    ReservaDTO confirmarReserva(@PathVariable("id") Long id);

    @PutMapping("/api/reservas/{id}/cancelar")
    ReservaDTO cancelarReserva(@PathVariable("id") Long id);
}
