package com.evently.backend.service;

import com.evently.backend.model.*;
import com.evently.backend.repository.OrganizadorRepository;
import com.evently.backend.repository.PagoRepository;
import com.evently.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrganizadorService {
    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private PagoRepository pagoRepository;


    // Registra al organizador con su plan elegido
    public Organizador registrarOrganizador(Long usuarioId, TipoPlan tipoPlan) {

        // Verifica que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con id: " + usuarioId));

        // Verifica que no sea ya organizador
        if (organizadorRepository.findByUsuario(usuario).isPresent()) {
            throw new RuntimeException("El usuario ya es organizador");
        }

        // Obtiene el plan elegido
        Plan plan = planService.obtenerPlanPorTipo(tipoPlan);

        // Calcula la fecha de vencimiento - 1 mes
        LocalDateTime vencimiento = LocalDateTime.now().plusMonths(1);

        // Crear el organizador
        Organizador organizador = new Organizador();
        organizador.setUsuario(usuario);
        organizador.setPlan(plan);
        organizador.setFechaVencimientoPlan(vencimiento);
        organizador.setEventosCreados(0);

        return organizadorRepository.save(organizador);
    }

    // Busca el organizador dado un usuario
    public Organizador obtenerPorUsuario(Usuario usuario) {
        return organizadorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Organizador no encontrado"));
    }

    // Cambiar de plan, puede subirlo o bajarlo
    public Organizador cambiarPlan(Long organizadorId, TipoPlan nuevoTipo) {

        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RuntimeException(
                        "Organizador no encontrado"));

        Plan nuevoPlan = planService.obtenerPlanPorTipo(nuevoTipo);

        organizador.setPlan(nuevoPlan);
        organizador.setFechaVencimientoPlan(
                LocalDateTime.now().plusMonths(1));

        return organizadorRepository.save(organizador);
    }

    // Verifica si el organizador puede crear más eventos, mayormente si supero el limite de acuerdo al plan
    public boolean puedeCrearEvento(Organizador organizador) {
        int limite = organizador.getPlan().getLimiteEventos();
        int creados = organizador.getEventosCreados();
        return creados < limite;
    }

    // Verifica si el plan vence pronto y notifica
    public void verificarVencimientoPlan(Organizador organizador) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vencimiento = organizador.getFechaVencimientoPlan();
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS
                .between(ahora, vencimiento);

        if (diasRestantes <= 7 && diasRestantes >= 0) {
            notificacionService.enviarNotificacion(
                    organizador.getUsuario(),
                    "Tu plan " + organizador.getPlan().getNombre() +
                            " vence en " + diasRestantes + " días. ¡Renuévalo!",
                    TipoNotificacion.RECORDATORIO_EVENTO
            );
        }
    }

    // Resumen de ingresos del organizador
    public Map<String, Object> misIngresos(Usuario usuario) {

        Organizador organizador = obtenerPorUsuario(usuario);
        List<Pago> pagos = pagoRepository.findByOrganizador(organizador);

        double ingresosBrutos = pagos.stream()
                .mapToDouble(Pago::getMonto).sum();
        double comisiones = pagos.stream()
                .mapToDouble(Pago::getComisionPlataforma).sum();
        double ingresoNeto = pagos.stream()
                .mapToDouble(Pago::getMontoOrganizador).sum();

        // Resumen por evento
        Map<String, Object> porEvento = new java.util.LinkedHashMap<>();
        pagos.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getReserva().getEvento().getTitulo()))
                .forEach((titulo, pagosList) -> {
                    Map<String, Object> resumen = new java.util.HashMap<>();
                    resumen.put("entradasVendidas", pagosList.stream()
                            .mapToInt(p -> p.getReserva()
                                    .getCantidadEntradas()).sum());
                    resumen.put("ingresosBrutos", pagosList.stream()
                            .mapToDouble(Pago::getMonto).sum());
                    resumen.put("comisiones", pagosList.stream()
                            .mapToDouble(Pago::getComisionPlataforma).sum());
                    resumen.put("ingresoNeto", pagosList.stream()
                            .mapToDouble(Pago::getMontoOrganizador).sum());
                    porEvento.put(titulo, resumen);
                });

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("ingresosBrutosTotales", ingresosBrutos);
        response.put("comisionesDescontadas", comisiones);
        response.put("ingresoNetoTotal", ingresoNeto);
        response.put("totalPagosRecibidos", pagos.size());
        response.put("detallePorEvento", porEvento);

        return response;
    }
}
