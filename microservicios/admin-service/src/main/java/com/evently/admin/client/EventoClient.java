package com.evently.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "evento-service", url = "${evento.service.url:http://evento-service:8082}")
public interface EventoClient {
    @GetMapping("/api/organizadores/plan/{tipoPlan}")
    List<Object> organizadoresPorPlan(@PathVariable("tipoPlan") String tipoPlan);

    @GetMapping("/api/eventos/estado/{estado}")
    Object eventosPorEstado(@PathVariable("estado") String estado, @RequestParam("pagina") int pagina, @RequestParam("cantidad") int cantidad);

    @GetMapping("/api/organizadores/estado-plan")
    Object organizadoresPorEstadoPlan();
}
