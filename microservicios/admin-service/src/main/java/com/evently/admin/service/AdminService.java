package com.evently.admin.service;

import com.evently.admin.client.AuthClient;
import com.evently.admin.client.EventoClient;
import com.evently.admin.client.PagoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private EventoClient eventoClient;

    @Autowired
    private PagoClient pagoClient;

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        // In a real scenario, we might call multiple services to aggregate data.
        // For simplicity, we just return a placeholder or aggregate basic info.
        dashboard.put("usuariosTotales", authClient.listarUsuarios().size());
        dashboard.put("eventos", eventoClient.eventosPorEstado("PUBLICADO", 0, 10));
        dashboard.put("ingresosMes", pagoClient.ingresosPorPeriodo("mes"));
        return dashboard;
    }

    public List<Object> listarUsuarios() {
        return authClient.listarUsuarios();
    }

    public List<Object> organizadoresPorPlan(String tipoPlan) {
        return eventoClient.organizadoresPorPlan(tipoPlan);
    }

    public Object toggleUsuario(Long id) {
        return authClient.toggleUsuario(id);
    }

    public Object historialPagos(int pagina, int cantidad) {
        return pagoClient.historialPagos(pagina, cantidad);
    }

    public Object ingresosPorPeriodo(String periodo) {
        return pagoClient.ingresosPorPeriodo(periodo);
    }

    public Object eventosPorEstado(String estado, int pagina, int cantidad) {
        return eventoClient.eventosPorEstado(estado, pagina, cantidad);
    }

    public Object organizadoresPorEstadoPlan() {
        return eventoClient.organizadoresPorEstadoPlan();
    }
}
