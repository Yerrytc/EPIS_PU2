package com.upt.sistema.shared.service;

import com.upt.sistema.shared.dto.DashboardStatsDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardStatsDTO obtenerEstadisticas() {
        return new DashboardStatsDTO(
                contar("estudiante"),
                contar("objetos"),
                contar("tutoria_solicitudes"),
                contar("rsu_proyecto"),
                contar("intervencion")
        );
    }

    public Map<String, Long> obtenerMetricasResumen() {
        DashboardStatsDTO stats = obtenerEstadisticas();

        Map<String, Long> metricas = new LinkedHashMap<>();
        metricas.put("Estudiantes", stats.getTotalSista());
        metricas.put("Objetos", stats.getTotalObjetos());
        metricas.put("Tutorias", stats.getTotalTutorias());
        metricas.put("RSU", stats.getTotalRsu());
        metricas.put("Intervenciones", stats.getTotalIntervenciones());
        return metricas;
    }

    public List<Map<String, Object>> obtenerActividadOperativa() {
        try {
            return jdbc.queryForList("""
                SELECT 'Justificaciones pendientes' AS tipo,
                       COUNT(*) AS total,
                       'Tutoria' AS modulo,
                       '/tutoria' AS ruta
                FROM tutoria_justificacion
                WHERE COALESCE(estado_revision, 'PENDIENTE') = 'PENDIENTE'
                UNION ALL
                SELECT 'Objetos sin entregar', COUNT(*), 'Objetos perdidos', '/objetos'
                FROM objetos
                WHERE estado = 'No entregado'
                UNION ALL
                SELECT 'Proyectos RSU por aprobar', COUNT(*), 'RSU', '/rsu'
                FROM rsu_proyecto
                WHERE estado_aprobacion <> 'APROBADO'
                UNION ALL
                SELECT 'Estudiantes con 3+ faltas', COUNT(*), 'SISTA/Tutoria', '/tutoria'
                FROM (
                    SELECT codigo_estudiante
                    FROM tutoria_inasistencia
                    GROUP BY codigo_estudiante
                    HAVING COUNT(*) >= 3
                ) alertas
                """);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    public List<Map<String, Object>> obtenerUltimosMovimientos() {
        try {
            return jdbc.queryForList("""
                SELECT 'Objeto registrado' AS evento, nombre_objeto AS detalle, fecha_encontrado AS fecha, '/objetos' AS ruta
                FROM objetos
                UNION ALL
                SELECT 'Intervencion SISTA', motivo, fecha, '/sista/intervenciones'
                FROM intervencion
                UNION ALL
                SELECT 'Proyecto RSU', nombre, fecha_inicio, '/rsu'
                FROM rsu_proyecto
                ORDER BY fecha DESC
                LIMIT 8
                """);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    public String obtenerCorreoAlertas() {
        try {
            List<String> rows = jdbc.queryForList("SELECT valor FROM sistema_configuracion WHERE clave = 'correo_alertas_inasistencia'", String.class);
            return rows.isEmpty() ? "tutoria@upt.edu.pe" : rows.get(0);
        } catch (DataAccessException ex) {
            return "tutoria@upt.edu.pe";
        }
    }

    private long contar(String tabla) {
        String tablaSegura = switch (tabla) {
            case "estudiante" -> "estudiante";
            case "objetos" -> "objetos";
            case "tutoria_solicitudes" -> "tutoria_solicitudes";
            case "rsu_proyecto" -> "rsu_proyecto";
            case "intervencion" -> "intervencion";
            default -> throw new IllegalArgumentException("Tabla no permitida: " + tabla);
        };
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM " + tablaSegura, Long.class);
        return total == null ? 0 : total;
    }
}
