package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.EventoRepository;
import com.evently.backend.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventoService {
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private OrganizadorService organizadorService;

    @Autowired
    private ReservaRepository reservaRepository;

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

    // Funcion Logica
    // Estadística de ocupación de un evento
    public Map<String, Object> estadisticaOcupacion(Long eventoId,
                                                    Usuario usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        int vendidas = evento.getAforo() - evento.getAforoDisponible();
        double porcentaje = ((double) vendidas / evento.getAforo()) * 100;

        Map<String, Object> estadistica = new HashMap<>();
        estadistica.put("evento", evento.getTitulo());
        estadistica.put("aforoTotal", evento.getAforo());
        estadistica.put("entradasVendidas", vendidas);
        estadistica.put("aforoDisponible", evento.getAforoDisponible());
        estadistica.put("porcentajeOcupacion",
                String.format("%.1f%%", porcentaje));
        estadistica.put("estado", evento.getEstado());

        return estadistica;
    }

    // Recomendación de eventos basada en historial del cliente
    public List<Evento> recomendarEventos(Usuario cliente) {

        // Obtener reservas del cliente
        List<Reserva> reservas = reservaRepository.findByCliente(cliente);

        if (reservas.isEmpty()) {
            // Sin historial → devolver los próximos 6 eventos
            return eventoRepository
                    .findTop12ByEstadoOrderByFechaEventoAsc(
                            EstadoEvento.PUBLICADO)
                    .stream()
                    .limit(6)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Obtener la categoría más reservada
        CategoriaEvento categoriaFavorita = reservas.stream()
                .map(r -> r.getEvento().getCategoria())
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        // Buscar eventos de esa categoría
        return eventoRepository.findRecomendados(
                categoriaFavorita, cliente);
    }

}
