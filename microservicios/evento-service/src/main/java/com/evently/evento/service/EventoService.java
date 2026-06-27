package com.evently.evento.service;

import com.evently.evento.dto.UsuarioDTO;
import com.evently.evento.model.*;
import com.evently.evento.repository.EventoRepository;
import com.evently.evento.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventoService {
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private OrganizadorService organizadorService;

    @Autowired
    private ReservaRepository reservaRepository;

    public Evento crearEvento(Evento evento, UsuarioDTO usuario) {
        Organizador organizador = organizadorService.obtenerPorUsuarioId(usuario.getId());

        if (organizador.getFechaVencimientoPlan().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tu plan ha vencido. Renuévalo para crear eventos.");
        }

        if (!organizadorService.puedeCrearEvento(organizador)) {
            throw new RuntimeException("Límite de eventos alcanzado. Mejora tu plan.");
        }

        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.BORRADOR);

        organizador.setEventosCreados(organizador.getEventosCreados() + 1);

        return eventoRepository.save(evento);
    }

    public Evento publicarEvento(Long eventoId, UsuarioDTO usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        if (evento.getOrganizador().getFechaVencimientoPlan().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tu plan ha vencido. Renuévalo para publicar eventos.");
        }

        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new RuntimeException("Solo se pueden publicar eventos en borrador");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        return eventoRepository.save(evento);
    }

    public Evento cancelarEvento(Long eventoId, UsuarioDTO usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new RuntimeException("No se puede cancelar un evento finalizado");
        }

        evento.setEstado(EstadoEvento.CANCELADO);
        return eventoRepository.save(evento);
    }

    public Evento editarEvento(Long eventoId, Evento eventoActualizado, UsuarioDTO usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        if (evento.getEstado() == EstadoEvento.FINALIZADO ||
                evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new RuntimeException("No se puede editar un evento finalizado o cancelado");
        }

        evento.setTitulo(eventoActualizado.getTitulo());
        evento.setDescripcion(eventoActualizado.getDescripcion());
        evento.setFechaEvento(eventoActualizado.getFechaEvento());
        evento.setLugar(eventoActualizado.getLugar());
        evento.setPrecio(eventoActualizado.getPrecio());
        evento.setImagenUrl(eventoActualizado.getImagenUrl());

        return eventoRepository.save(evento);
    }

    public List<Evento> listarEventosPublicos() {
        return eventoRepository.findByEstado(EstadoEvento.PUBLICADO);
    }

    public List<Evento> listarMisEventos(UsuarioDTO usuario) {
        Organizador organizador = organizadorService.obtenerPorUsuarioId(usuario.getId());
        return eventoRepository.findByOrganizador(organizador);
    }

    public Evento obtenerPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con id: " + id));
    }

    private void verificarPropietario(Evento evento, UsuarioDTO usuario) {
        if (!evento.getOrganizador().getUsuarioId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este evento");
        }
    }

    public List<Evento> listarEventosPortada() {
        return eventoRepository.findTop12ByEstadoOrderByFechaEventoAsc(EstadoEvento.PUBLICADO);
    }

    public List<Evento> buscarEventos(String titulo, String lugar, CategoriaEvento categoria) {
        String categoriaStr = categoria != null ? categoria.name() : null;
        return eventoRepository.buscarEventosPublicos(titulo, lugar, categoriaStr);
    }

    public Map<String, Object> estadisticaOcupacion(Long eventoId, UsuarioDTO usuario) {
        Evento evento = obtenerPorId(eventoId);
        verificarPropietario(evento, usuario);

        int vendidas = evento.getAforo() - evento.getAforoDisponible();
        double porcentaje = ((double) vendidas / evento.getAforo()) * 100;

        Map<String, Object> estadistica = new HashMap<>();
        estadistica.put("evento", evento.getTitulo());
        estadistica.put("aforoTotal", evento.getAforo());
        estadistica.put("entradasVendidas", vendidas);
        estadistica.put("aforoDisponible", evento.getAforoDisponible());
        estadistica.put("porcentajeOcupacion", String.format("%.1f%%", porcentaje));
        estadistica.put("estado", evento.getEstado());

        return estadistica;
    }

    public List<Evento> recomendarEventos(UsuarioDTO cliente) {
        List<Reserva> reservas = reservaRepository.findByClienteId(cliente.getId());

        if (reservas.isEmpty()) {
            return eventoRepository.findTop12ByEstadoOrderByFechaEventoAsc(EstadoEvento.PUBLICADO)
                    .stream()
                    .limit(6)
                    .collect(Collectors.toList());
        }

        CategoriaEvento categoriaFavorita = reservas.stream()
                .map(r -> r.getEvento().getCategoria())
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return eventoRepository.findRecomendados(categoriaFavorita, cliente.getId());
    }

    public Map<String, Object> misEstadisticas(UsuarioDTO usuario) {
        Organizador organizador = organizadorService.obtenerPorUsuarioId(usuario.getId());
        List<Evento> todosEventos = eventoRepository.findByOrganizador(organizador);

        long publicados = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.PUBLICADO).count();
        long cancelados = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.CANCELADO).count();
        long borradores = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.BORRADOR).count();
        long finalizados = todosEventos.stream().filter(e -> e.getEstado() == EstadoEvento.FINALIZADO).count();

        int totalVendidas = todosEventos.stream().mapToInt(e -> e.getAforo() - e.getAforoDisponible()).sum();

        String eventoMasPopular = todosEventos.stream()
                .max(java.util.Comparator.comparingInt(e -> e.getAforo() - e.getAforoDisponible()))
                .map(Evento::getTitulo)
                .orElse("Sin eventos");

        double tasaPromedio = todosEventos.isEmpty() ? 0
                : todosEventos.stream()
                        .mapToDouble(e -> ((double) (e.getAforo() - e.getAforoDisponible()) / e.getAforo()) * 100)
                        .average()
                        .orElse(0);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalEventos", todosEventos.size());
        estadisticas.put("eventosPublicados", publicados);
        estadisticas.put("eventosCancelados", cancelados);
        estadisticas.put("eventosBorradores", borradores);
        estadisticas.put("eventosFinalizados", finalizados);
        estadisticas.put("totalEntradasVendidas", totalVendidas);
        estadisticas.put("eventoMasPopular", eventoMasPopular);
        estadisticas.put("tasaOcupacionPromedio", String.format("%.1f%%", tasaPromedio));

        return estadisticas;
    }

    public Evento actualizarAforo(Long id, Integer cantidad) {
        Evento evento = obtenerPorId(id);
        evento.setAforoDisponible(evento.getAforoDisponible() + cantidad);
        if (evento.getAforoDisponible() < 0) {
            throw new RuntimeException("No hay aforo disponible para este evento");
        }
        return eventoRepository.save(evento);
    }
}
