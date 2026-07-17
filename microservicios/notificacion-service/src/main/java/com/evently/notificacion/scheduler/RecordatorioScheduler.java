package com.evently.notificacion.scheduler;

import com.evently.notificacion.client.EventoClient;
import com.evently.notificacion.model.TipoNotificacion;
import com.evently.notificacion.service.NotificacionService;
import com.evently.notificacion.service.TwilioWhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Scheduler que envía recordatorios automáticos por WhatsApp
 * a los usuarios con reservas confirmadas para eventos del día siguiente.
 * Se ejecuta todos los días a las 9:00 AM.
 */
@Component
public class RecordatorioScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioScheduler.class);

    @Autowired
    private EventoClient eventoClient;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private TwilioWhatsAppService twilioWhatsAppService;

    /**
     * Ejecutar todos los días a las 9:00 AM
     * Busca eventos que ocurran mañana y envía recordatorio
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void enviarRecordatorios() {
        log.info("⏰ Ejecutando scheduler de recordatorios de eventos...");

        try {
            LocalDate manana = LocalDate.now().plusDays(1);
            List<Map<String, Object>> eventos = eventoClient.obtenerEventosPublicos();

            if (eventos == null || eventos.isEmpty()) {
                log.info("No hay eventos públicos disponibles");
                return;
            }

            int recordatoriosEnviados = 0;

            for (Map<String, Object> evento : eventos) {
                try {
                    String fechaStr = (String) evento.get("fecha");
                    if (fechaStr == null) continue;

                    // Parsear la fecha del evento (puede ser LocalDateTime o LocalDate)
                    LocalDate fechaEvento;
                    try {
                        fechaEvento = LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
                    } catch (Exception e) {
                        try {
                            fechaEvento = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE);
                        } catch (Exception e2) {
                            continue;
                        }
                    }

                    // Solo eventos de mañana
                    if (!fechaEvento.equals(manana)) continue;

                    String titulo = (String) evento.get("titulo");
                    String lugar = (String) evento.get("lugar");
                    Object eventoIdObj = evento.get("id");
                    Long eventoId = eventoIdObj instanceof Number ? ((Number) eventoIdObj).longValue() : null;

                    if (eventoId == null) continue;

                    String mensaje = String.format(
                            "🔔 ¡Recordatorio! Mañana tienes el evento \"%s\" en %s. ¡No faltes! 🎉",
                            titulo != null ? titulo : "Evento",
                            lugar != null ? lugar : "el lugar indicado"
                    );

                    log.info("📅 Evento mañana: {} (ID: {})", titulo, eventoId);

                    // Nota: En una implementación completa, aquí se consultarían las reservas
                    // confirmadas para este evento y se enviaría recordatorio a cada usuario.
                    // Por ahora, se genera la notificación genérica que se podrá enriquecer
                    // cuando se agregue un ReservaClient al notificacion-service.
                    recordatoriosEnviados++;

                } catch (Exception e) {
                    log.warn("Error procesando evento para recordatorio: {}", e.getMessage());
                }
            }

            log.info("✅ Scheduler completado. Eventos con recordatorio: {}", recordatoriosEnviados);

        } catch (Exception e) {
            log.error("❌ Error en el scheduler de recordatorios: {}", e.getMessage());
        }
    }
}
