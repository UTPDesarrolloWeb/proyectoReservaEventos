package com.evently.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pago-service", url = "http://localhost:8084")
public interface PagoClient {
    @GetMapping("/api/pagos/todos")
    Object historialPagos(@RequestParam("pagina") int pagina, @RequestParam("cantidad") int cantidad);

    @GetMapping("/api/pagos/ingresos")
    Object ingresosPorPeriodo(@RequestParam("periodo") String periodo);
}
