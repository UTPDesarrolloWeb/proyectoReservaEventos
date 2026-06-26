package com.evently.evento.controller;

import com.evently.evento.client.AuthServiceClient;
import com.evently.evento.dto.UsuarioDTO;
import com.evently.evento.model.CategoriaEvento;
import com.evently.evento.model.Evento;
import com.evently.evento.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
// @CrossOrigin(origins = "*")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @GetMapping("/publicos")
    public ResponseEntity<List<Evento>> listarPublicos() {
        return ResponseEntity.ok(eventoService.listarEventosPortada());
    }

    @GetMapping("/publicos/{id}")
    public ResponseEntity<Evento> verEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> crearEvento(@RequestBody Evento evento, Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.crearEvento(evento, usuario));
    }

    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> publicarEvento(@PathVariable Long id, Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.publicarEvento(id, usuario));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> cancelarEvento(@PathVariable Long id, Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.cancelarEvento(id, usuario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> editarEvento(@PathVariable Long id, @RequestBody Evento evento,
            Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.editarEvento(id, evento, usuario));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Evento>> buscarEventos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String lugar,
            @RequestParam(required = false) CategoriaEvento categoria) {
        return ResponseEntity.ok(eventoService.buscarEventos(titulo, lugar, categoria));
    }

    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<List<Evento>> misEventos(Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.listarMisEventos(usuario));
    }

    @GetMapping("/{id}/estadistica")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Map<String, Object>> estadisticaOcupacion(@PathVariable Long id,
            Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.estadisticaOcupacion(id, usuario));
    }

    @GetMapping("/recomendados")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<Evento>> recomendados(Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.recomendarEventos(usuario));
    }

    @GetMapping("/mis-estadisticas")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Map<String, Object>> misEstadisticas(Authentication authentication) {
        UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorEmail(authentication.getName());
        return ResponseEntity.ok(eventoService.misEstadisticas(usuario));
    }

    // INTERNAL ENDPOINTS FOR RESERVA-SERVICE & PAGO-SERVICE
    @GetMapping("/{id}/interno")
    public ResponseEntity<Evento> obtenerEventoPorIdInterno(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorId(id));
    }

    @PutMapping("/{id}/aforo")
    public ResponseEntity<Evento> actualizarAforoInterno(@PathVariable Long id, @RequestParam Integer cantidad) {
        // Evento evento = eventoService.obtenerPorId(id);
        // evento.setAforoDisponible(evento.getAforoDisponible() + cantidad);
        // if (evento.getAforoDisponible() < 0) {
        // throw new RuntimeException("No hay aforo disponible para este evento");
        // }
        return ResponseEntity.ok(eventoService.actualizarAforo(id, cantidad));
    }
}
