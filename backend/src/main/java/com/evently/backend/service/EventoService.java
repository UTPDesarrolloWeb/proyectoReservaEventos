package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoService {
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private OrganizadorService organizadorService;

    // Crea el evento
    public Evento crearEvento(Evento evento, Usuario usuario) {

        Organizador organizador = organizadorService
                .obtenerPorUsuario(usuario);

        // Verificar que el plan no haya vencido
        if (organizador.getFechaVencimientoPlan()
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Tu plan ha vencido. Renuévalo para crear eventos.");
        }

        // Verificar límite de eventos según el plan
        if (!organizadorService.puedeCrearEvento(organizador)) {
            throw new RuntimeException(
                    "Límite de eventos alcanzado. Mejora tu plan.");
        }

        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.BORRADOR);

        organizador.setEventosCreados(
                organizador.getEventosCreados() + 1);

        return eventoRepository.save(evento);
    }

    // Publica el evento
    public Evento publicarEvento(Long eventoId, Usuario usuario) {

        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        // Verificar que el plan no haya vencido
        if (evento.getOrganizador().getFechaVencimientoPlan()
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Tu plan ha vencido. Renuévalo para publicar eventos.");
        }

        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new RuntimeException(
                    "Solo se pueden publicar eventos en borrador");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        return eventoRepository.save(evento);
    }

    // Cancela el evento
    public Evento cancelarEvento(Long eventoId, Usuario usuario) {

        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new RuntimeException(
                    "No se puede cancelar un evento finalizado");
        }

        evento.setEstado(EstadoEvento.CANCELADO);
        return eventoRepository.save(evento);
    }

    // Edita el evento
    public Evento editarEvento(Long eventoId, Evento eventoActualizado,
                               Usuario usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        if (evento.getEstado() == EstadoEvento.FINALIZADO ||
                evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new RuntimeException(
                    "No se puede editar un evento finalizado o cancelado");
        }

        evento.setTitulo(eventoActualizado.getTitulo());
        evento.setDescripcion(eventoActualizado.getDescripcion());
        evento.setFechaEvento(eventoActualizado.getFechaEvento());
        evento.setLugar(eventoActualizado.getLugar());
        evento.setPrecio(eventoActualizado.getPrecio());
        evento.setImagenUrl(eventoActualizado.getImagenUrl());

        return eventoRepository.save(evento);
    }

    // Lista los eventos públicos para los clientes
    public List<Evento> listarEventosPublicos() {
        return eventoRepository.findByEstado(EstadoEvento.PUBLICADO);
    }

    // Lista los eventos del organizador
    public List<Evento> listarMisEventos(Usuario usuario) {
        Organizador organizador = organizadorService
                .obtenerPorUsuario(usuario);
        return eventoRepository.findByOrganizador(organizador);
    }

    // Obtiene el evento por id
    public Evento obtenerPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Evento no encontrado con id: " + id));
    }

    // Verifica que el usuario sea el dueño del evento
    private void verificarPropietario(Evento evento, Usuario usuario) {
        if (!evento.getOrganizador().getUsuario()
                .getEmail().equals(usuario.getEmail())) {
            throw new RuntimeException(
                    "No tienes permiso para modificar este evento");
        }
    }
    // Trae los próximos 12 eventos para mostrar en la portada
    public List<Evento> listarEventosPortada() {
        return eventoRepository.findTop12ByEstadoOrderByFechaEventoAsc(
                EstadoEvento.PUBLICADO);
    }

    // Busca eventos con filtros opcionales de título, lugar y categoría
    public List<Evento> buscarEventos(String titulo, String lugar, CategoriaEvento categoria) {
        String categoriaStr = categoria != null ? categoria.name() : null;
        return eventoRepository.buscarEventosPublicos(titulo, lugar, categoriaStr);
    }
}
