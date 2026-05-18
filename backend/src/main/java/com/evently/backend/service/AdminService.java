package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class AdminService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PlanRepository planRepository;



    // Dashboard completo del administrador
    public Map<String, Object> getDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        // Total usuarios registrados
        dashboard.put("totalUsuarios",
                usuarioRepository.count());

        // Total organizadores
        dashboard.put("totalOrganizadores",
                organizadorRepository.count());

        // Organizadores por plan
        Map<String, Long> organizadoresPorPlan = new HashMap<>();
        List<Plan> planes = planRepository.findByActivoTrue();
        for (Plan plan : planes) {
            organizadoresPorPlan.put(
                    plan.getNombre().name(),
                    organizadorRepository.countByPlan(plan));
        }
        dashboard.put("organizadoresPorPlan", organizadoresPorPlan);

        // Ingresos por comisiones
        Double comisiones = pagoRepository.sumComisionesTotal();
        dashboard.put("ingresosPorComisiones",
                comisiones != null ? comisiones : 0.0);

        // Ingresos por suscripciones
        Double suscripciones = pagoRepository.sumIngresosSuscripciones();
        dashboard.put("ingresosPorSuscripciones",
                suscripciones != null ? suscripciones : 0.0);

        // Total eventos publicados
        dashboard.put("totalEventosPublicados",
                eventoRepository.findByEstado(EstadoEvento.PUBLICADO).size());

        // Total reservas confirmadas
        dashboard.put("totalReservasConfirmadas",
                reservaRepository.findByEstado(EstadoReserva.CONFIRMADA).size());

        // Total pagos completados
        dashboard.put("totalPagosCompletados",
                pagoRepository.findByEstado(EstadoPago.COMPLETADO).size());

        return dashboard;
    }

    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Listar organizadores por plan
    public List<Organizador> organizadoresPorPlan(TipoPlan tipoPlan) {
        Plan plan = planRepository.findByNombre(tipoPlan)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        return organizadorRepository.findByPlan(plan);
    }

    // Activar o desactivar usuario
    public Usuario toggleUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(!usuario.getActivo());
        return usuarioRepository.save(usuario);
    }

    // Historial de todos los pagos
    public Page<Pago> historialPagos(int pagina, int cantidad) {
        Pageable pageable = PageRequest.of(
                pagina, cantidad,
                Sort.by("fechaPago").descending());
        return pagoRepository.findAll(pageable);
    }

    // Ingresos por periodo
    public Map<String, Object> ingresosPorPeriodo(String periodo) {

        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime inicio;

        switch (periodo.toLowerCase()) {
            case "semana" -> inicio = fin.minusWeeks(1);
            case "mes"    -> inicio = fin.minusMonths(1);
            case "anio"   -> inicio = fin.minusYears(1);
            default       -> inicio = fin.minusMonths(1);
        }

        List<Pago> pagos = pagoRepository.findByPeriodo(inicio, fin);

        double totalIngresos = pagos.stream()
                .mapToDouble(Pago::getMonto).sum();
        double totalComisiones = pagos.stream()
                .mapToDouble(Pago::getComisionPlataforma).sum();
        double totalNeto = pagos.stream()
                .mapToDouble(Pago::getMontoOrganizador).sum();

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("periodo", periodo);
        response.put("desde", inicio);
        response.put("hasta", fin);
        response.put("totalPagos", pagos.size());
        response.put("totalIngresos", totalIngresos);
        response.put("totalComisiones", totalComisiones);
        response.put("totalNetoOrganizadores", totalNeto);

        return response;
    }

    // Listar eventos por estado con paginación
    public Page<Evento> eventosPorEstado(String estado,
                                         int pagina, int cantidad) {
        EstadoEvento estadoEvento = EstadoEvento.valueOf(
                estado.toUpperCase());
        Pageable pageable = PageRequest.of(
                pagina, cantidad,
                Sort.by("fechaCreacion").descending());
        return eventoRepository.findByEstado(estadoEvento, pageable);
    }
}
