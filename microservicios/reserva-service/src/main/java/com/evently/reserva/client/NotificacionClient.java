package com.evently.reserva.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notificacion-service", url = "${notificacion.service.url:http://notificacion-service:8085}")
public interface NotificacionClient {

    @PostMapping("/api/notificaciones/whatsapp")
    Map<String, Object> enviarWhatsApp(@RequestBody Map<String, Object> request);
}
