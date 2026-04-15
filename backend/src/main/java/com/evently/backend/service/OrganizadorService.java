package com.evently.backend.service;

import com.evently.backend.model.Organizador;
import com.evently.backend.model.Plan;
import com.evently.backend.model.TipoPlan;
import com.evently.backend.model.Usuario;
import com.evently.backend.repository.OrganizadorRepository;
import com.evently.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrganizadorService {
    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlanService planService;

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
}
