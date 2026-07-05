package com.evently.pago.controller;

import com.evently.pago.model.MetodoPago;
import com.evently.pago.model.Pago;
import com.evently.pago.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {
    @Autowired
    private PagoService pagoService;

    @Autowired
    private com.evently.pago.service.PDFService pdfService;

    @Autowired
    private com.evently.pago.client.ReservaClient reservaClient;

    @Autowired
    private com.evently.pago.client.AuthClient authClient;

    @Autowired
    private com.evently.pago.client.EventoClient eventoClient;

    // Procesar un pago
    @PostMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<Pago> procesarPago(
            @PathVariable Long reservaId,
            @RequestParam MetodoPago metodo,
            @RequestParam(required = false) String transaccionId) {

        return ResponseEntity.ok(
                pagoService.procesarPago(reservaId, metodo, transaccionId));
    }

    // Procesa el reembolso
    @PutMapping("/reembolso/{reservaId}")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<Pago> procesarReembolso(@PathVariable Long reservaId) {
        return ResponseEntity.ok(pagoService.procesarReembolso(reservaId));
    }

    // Ver pago de una reserva
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<Pago> verPago(@PathVariable Long reservaId) {
        return ResponseEntity.ok(pagoService.obtenerPagoPorReserva(reservaId));
    }

    // Lista los pagos por estado - uso del admin
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pago>> listarPorEstado(@PathVariable com.evently.pago.model.EstadoPago estado) {
        return ResponseEntity.ok(pagoService.listarPagosPorEstado(estado));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Pago>> historialPagos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int cantidad) {
        return ResponseEntity.ok(pagoService.todosLosPagos(pagina, cantidad));
    }

    @GetMapping("/ingresos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> ingresosPorPeriodo(
            @RequestParam(defaultValue = "mes") String periodo) {
        // Mocked implementation for now, or real if implemented in PagoService
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("ingresos", 0);
        return ResponseEntity.ok(map);
    }

    // Obtener un pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtenerPago(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    // Descargar boleta de pago
    @GetMapping("/boleta/{reservaId}")
    public ResponseEntity<byte[]> descargarBoleta(@PathVariable Long reservaId) {
        Pago pago = pagoService.obtenerPagoPorReserva(reservaId);
        com.evently.pago.dto.ReservaDTO reserva = reservaClient.obtenerPorId(reservaId);
        com.evently.pago.dto.UsuarioDTO cliente = authClient.obtenerUsuarioPorId(reserva.getClienteId());
        com.evently.pago.dto.EventoDTO evento = eventoClient.obtenerPorId(reserva.getEventoId());

        byte[] pdf = pdfService.generarBoletaPago(pago, reserva, cliente, evento);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=boleta-" + reservaId + ".pdf")
                .body(pdf);
    }
}
