package com.evently.evento.service;

import com.evently.evento.client.AuthServiceClient;
import com.evently.evento.dto.UsuarioDTO;
import com.evently.evento.model.Organizador;
import com.evently.evento.model.Plan;
import com.evently.evento.model.TipoPlan;
import com.evently.evento.repository.OrganizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OrganizadorService {
    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private PlanService planService;

    public Organizador registrarOrganizador(Long usuarioId, TipoPlan tipoPlan) {
        // Verifica que el usuario existe vía Feign
        try {
            UsuarioDTO usuario = authServiceClient.obtenerUsuarioPorId(usuarioId);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado con id: " + usuarioId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error verificando usuario: " + e.getMessage());
        }

        // Verifica que no sea ya organizador
        if (organizadorRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new RuntimeException("El usuario ya es organizador");
        }

        Plan plan = planService.obtenerPlanPorTipo(tipoPlan);
        LocalDateTime vencimiento = LocalDateTime.now().plusMonths(1);

        Organizador organizador = new Organizador();
        organizador.setUsuarioId(usuarioId);
        organizador.setPlan(plan);
        organizador.setFechaVencimientoPlan(vencimiento);
        organizador.setEventosCreados(0);

        return organizadorRepository.save(organizador);
    }

    public Organizador obtenerPorUsuarioId(Long usuarioId) {
        return organizadorRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> registrarOrganizador(usuarioId, TipoPlan.BASICO));
    }

    public Organizador cambiarPlan(Long organizadorId, TipoPlan nuevoTipo) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        Plan nuevoPlan = planService.obtenerPlanPorTipo(nuevoTipo);

        organizador.setPlan(nuevoPlan);
        organizador.setFechaVencimientoPlan(LocalDateTime.now().plusMonths(1));

        return organizadorRepository.save(organizador);
    }

    public boolean puedeCrearEvento(Organizador organizador) {
        int limite = organizador.getPlan().getLimiteEventos();
        int creados = organizador.getEventosCreados();
        return creados < limite;
    }
}
