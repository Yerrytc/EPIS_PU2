package com.upt.sistema.shared.service;

import com.upt.sistema.modules.sista.service.SistaMigradoService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertaInasistenciaScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertaInasistenciaScheduler.class);

    private final SistaMigradoService sistaService;
    private final NotificationService notificationService;
    private final JdbcTemplate jdbc;

    public AlertaInasistenciaScheduler(SistaMigradoService sistaService,
                                        NotificationService notificationService,
                                        JdbcTemplate jdbc) {
        this.sistaService = sistaService;
        this.notificationService = notificationService;
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "0 0 8,14,20 * * *")
    public void ejecutar() {
        try {
            String ultima = sistaService.obtenerConfiguracion("ultima_alerta_inasistencia_enviada", "");
            String ahora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            List<Map<String, Object>> alertas = sistaService.listarAlertasInasistencia();
            if (alertas.isEmpty()) {
                log.info("Sin alertas de inasistencia para enviar.");
                return;
            }

            String destinatario = sistaService.obtenerConfiguracion("correo_alertas_inasistencia", "");
            if (destinatario.isBlank()) {
                log.warn("No hay correo de alertas configurado.");
                return;
            }

            if (ultima.contains("T")) {
                LocalDateTime ultimaDT = LocalDateTime.parse(ultima, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (ultimaDT.isAfter(LocalDateTime.now().minusHours(6))) {
                    log.info("Alerta ya enviada recientemente, omitiendo.");
                    return;
                }
            }

            StringBuilder html = new StringBuilder();
            html.append("<h2>Alertas de inasistencia - EPIS</h2>");
            html.append("<p>Se detectaron estudiantes con 3 o mas inasistencias consecutivas:</p>");
            html.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse'>");
            html.append("<tr><th>Estudiante</th><th>Inasistencias</th><th>Desde</th><th>Hasta</th></tr>");
            for (Map<String, Object> a : alertas) {
                html.append("<tr>");
                html.append("<td>").append(a.get("nombre_estudiante")).append("</td>");
                html.append("<td>").append(a.get("inasistencias_consecutivas")).append("</td>");
                html.append("<td>").append(a.get("fecha_inicio")).append("</td>");
                html.append("<td>").append(a.get("fecha_fin")).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("<p><small>Generado el ").append(ahora).append("</small></p>");

            notificationService.sendHtmlMessage(destinatario, "[EPIS] Alertas de inasistencia", html.toString());
            log.info("Alerta de inasistencia enviada a {}", destinatario);

            jdbc.update("""
                    INSERT INTO sistema_configuracion (clave, valor)
                    VALUES ('ultima_alerta_inasistencia_enviada', ?)
                    ON DUPLICATE KEY UPDATE valor = VALUES(valor), fecha_actualizacion = CURRENT_TIMESTAMP
                    """, ahora);
        } catch (Exception e) {
            log.error("Error al enviar alerta de inasistencia: {}", e.getMessage(), e);
        }
    }
}
