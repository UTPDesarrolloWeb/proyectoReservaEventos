package com.evently.backend.controller;

import com.evently.backend.model.CategoriaEvento;
import com.evently.backend.model.Evento;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.UsuarioRepository;
import com.evently.backend.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {
    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Lista los eventos públicos
    @GetMapping("/publicos")
    public ResponseEntity<List<Evento>> listarPublicos() {
        return ResponseEntity.ok(eventoService.listarEventosPortada());
    }

    // Vee los detalle de un evento
    @GetMapping("/publicos/{id}")
    public ResponseEntity<Evento> verEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorId(id));
    }

    // Crea el evento - uso del Organizador
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> crearEvento(
            @RequestBody Evento evento,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(eventoService.crearEvento(evento, usuario));
    }

    // Publica el evento - uso del Organizador
    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> publicarEvento(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(eventoService.publicarEvento(id, usuario));
    }

    // Cancela el evento - uso del Organizador
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> cancelarEvento(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(eventoService.cancelarEvento(id, usuario));
    }

    // Edita el evento - uso del Organizador
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Evento> editarEvento(
            @PathVariable Long id,
            @RequestBody Evento evento,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                eventoService.editarEvento(id, evento, usuario));
    }

    // Búsqueda con filtros opcionales de título, lugar y categoría
    @GetMapping("/buscar")
    public ResponseEntity<List<Evento>> buscarEventos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String lugar,
            @RequestParam(required = false) CategoriaEvento categoria) {

        return ResponseEntity.ok(
                eventoService.buscarEventos(titulo, lugar, categoria));
    }

    // Mis eventos - uso del Organizador
    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<List<Evento>> misEventos(
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(eventoService.listarMisEventos(usuario));
    }

    // Estadística de ocupación - solo ORGANIZADOR
    @GetMapping("/{id}/estadistica")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Map<String, Object>> estadisticaOcupacion(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado"));

        return ResponseEntity.ok(
                eventoService.estadisticaOcupacion(id, usuario));
    }
}
