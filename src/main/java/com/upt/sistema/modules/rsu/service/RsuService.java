package com.upt.sistema.modules.rsu.service;

import com.upt.sistema.shared.validation.AppValidation;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RsuService {

    private final JdbcTemplate jdbc;

    public RsuService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Long> resumen() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("Proyectos", contar("rsu_proyecto"));
        data.put("Participantes", contar("rsu_participante"));
        data.put("Beneficiarios", contar("rsu_beneficiario"));
        data.put("Evidencias", contar("rsu_evidencia"));
        data.put("Con 30h cumplidas", contarHorasCumplidas());
        return data;
    }

    public List<Map<String, Object>> proyectos() {
        return jdbc.queryForList("""
                SELECT p.id_proyecto, p.nombre, p.tipo_proyecto, p.objetivo, p.problema_social,
                       p.ods, p.responsable, p.estado_aprobacion, p.escuela_aliada,
                       p.fecha_inicio, p.fecha_fin, p.estado, p.fecha_clausura, p.resultados,
                       COALESCE(SUM(pa.horas_cumplidas), 0) AS horas_registradas,
                       COUNT(DISTINCT pa.codigo_estudiante) AS total_estudiantes,
                       COUNT(DISTINCT b.id_beneficiario) AS total_beneficiarios
                FROM rsu_proyecto p
                LEFT JOIN rsu_participante pa ON pa.id_proyecto = p.id_proyecto
                LEFT JOIN rsu_beneficiario b ON b.id_proyecto = p.id_proyecto
                GROUP BY p.id_proyecto, p.nombre, p.tipo_proyecto, p.objetivo, p.problema_social,
                         p.ods, p.responsable, p.estado_aprobacion, p.escuela_aliada,
                         p.fecha_inicio, p.fecha_fin, p.estado, p.fecha_clausura, p.resultados
                ORDER BY p.id_proyecto DESC
                """);
    }

    public List<Map<String, Object>> actividades() {
        return jdbc.queryForList("""
                SELECT a.*, p.nombre AS proyecto
                FROM rsu_actividad a
                INNER JOIN rsu_proyecto p ON p.id_proyecto = a.id_proyecto
                ORDER BY a.fecha DESC, a.id_actividad DESC
                """);
    }

    public List<Map<String, Object>> participantes() {
        return jdbc.queryForList("""
                SELECT pa.*, p.nombre AS proyecto, e.nombres, e.apellidos,
                       CASE WHEN p.tipo_proyecto = 'SERVICIO_SOCIAL_UNIVERSITARIO' AND pa.horas_cumplidas >= 30 THEN 'CUMPLE'
                            WHEN p.tipo_proyecto = 'SERVICIO_SOCIAL_UNIVERSITARIO' THEN 'PENDIENTE'
                            ELSE 'NO APLICA' END AS control_horas
                FROM rsu_participante pa
                INNER JOIN rsu_proyecto p ON p.id_proyecto = pa.id_proyecto
                INNER JOIN estudiante e ON e.codigo = pa.codigo_estudiante
                ORDER BY p.nombre, e.apellidos
                """);
    }

    public List<Map<String, Object>> beneficiarios() {
        return jdbc.queryForList("""
                SELECT b.*, p.nombre AS proyecto
                FROM rsu_beneficiario b
                INNER JOIN rsu_proyecto p ON p.id_proyecto = b.id_proyecto
                ORDER BY b.id_beneficiario DESC
                """);
    }

    public List<Map<String, Object>> asistencias() {
        return jdbc.queryForList("""
                SELECT a.*, ac.tema, p.nombre AS proyecto
                FROM rsu_asistencia a
                INNER JOIN rsu_actividad ac ON ac.id_actividad = a.id_actividad
                INNER JOIN rsu_proyecto p ON p.id_proyecto = ac.id_proyecto
                ORDER BY a.id_asistencia DESC
                """);
    }

    public List<Map<String, Object>> evidencias() {
        return jdbc.queryForList("""
                SELECT e.*, p.nombre AS proyecto
                FROM rsu_evidencia e
                INNER JOIN rsu_proyecto p ON p.id_proyecto = e.id_proyecto
                ORDER BY e.id_evidencia DESC
                """);
    }

    public List<Map<String, Object>> concursos() {
        return jdbc.queryForList("""
                SELECT c.*, p.nombre AS proyecto
                FROM rsu_concurso c
                INNER JOIN rsu_proyecto p ON p.id_proyecto = c.id_proyecto
                ORDER BY c.fecha DESC, c.id_concurso DESC
                """);
    }

    public List<Map<String, Object>> estudiantes() {
        return jdbc.queryForList("SELECT codigo, CONCAT(nombres, ' ', apellidos) AS nombre FROM estudiante ORDER BY apellidos, nombres");
    }

    @Transactional
    public void registrarProyecto(String nombre, String tipo, String objetivo, String problemaSocial, String ods,
                                  String responsable, String escuelaAliada, String fechaInicio, String fechaFin) {
        String nombreOk = AppValidation.requiredText("Nombre del proyecto", nombre, 5, 180);
        String tipoOk = AppValidation.oneOf("tipo de proyecto", tipo, "SERVICIO_SOCIAL_UNIVERSITARIO", "RESPONSABILIDAD_SOCIAL", "VOLUNTARIADO");
        String objetivoOk = AppValidation.requiredText("Objetivo", objetivo, 10, 1000);
        String problemaOk = AppValidation.requiredText("Problema social", problemaSocial, 10, 1000);
        String odsOk = AppValidation.requiredText("ODS", ods, 3, 180);
        String responsableOk = AppValidation.requiredLetters("Responsable", responsable, 3, 120);
        String escuelaOk = AppValidation.optionalText("Escuela aliada", escuelaAliada, 180);
        LocalDate inicio = AppValidation.requiredDate("Fecha de inicio", fechaInicio);
        LocalDate fin = AppValidation.requiredDate("Fecha de fin", fechaFin);
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");

        jdbc.update("""
                INSERT INTO rsu_proyecto
                (nombre, tipo_proyecto, objetivo, problema_social, ods, responsable, estado_aprobacion, escuela_aliada, fecha_inicio, fecha_fin)
                VALUES (?, ?, ?, ?, ?, ?, 'SOLICITADO', ?, ?, ?)
                """, nombreOk, tipoOk, objetivoOk, problemaOk, odsOk, responsableOk, escuelaOk, Date.valueOf(inicio), Date.valueOf(fin));
    }

    public void aprobarProyecto(Long idProyecto) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        jdbc.update("UPDATE rsu_proyecto SET estado_aprobacion = 'APROBADO' WHERE id_proyecto = ?", idProyecto);
    }

    public void clausurarProyecto(Long idProyecto, String fechaClausura, String resultados) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        LocalDate fecha = AppValidation.requiredDate("Fecha de clausura", fechaClausura);
        String resultadosOk = AppValidation.requiredText("Resultados", resultados, 10, 1000);
        jdbc.update("UPDATE rsu_proyecto SET estado = 'CLAUSURADO', fecha_clausura = ?, resultados = ? WHERE id_proyecto = ?", Date.valueOf(fecha), resultadosOk, idProyecto);
    }

    public void registrarActividad(Long idProyecto, String fecha, String hora, String tema, String descripcion, Double duracionHoras, String laboratorio) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        LocalDate fechaOk = AppValidation.requiredDate("Fecha de actividad", fecha);
        String horaOk = AppValidation.requiredHour("Hora", hora);
        String temaOk = AppValidation.requiredText("Tema", tema, 5, 180);
        String descripcionOk = AppValidation.requiredText("Descripcion", descripcion, 10, 1000);
        Double horasOk = AppValidation.range("Duracion en horas", duracionHoras, 0.5, 12.0);
        String laboratorioOk = AppValidation.requiredText("Laboratorio o ambiente", laboratorio, 2, 120);
        jdbc.update("""
                INSERT INTO rsu_actividad (id_proyecto, fecha, hora, tema, descripcion, duracion_horas, laboratorio_ambiente)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, idProyecto, Date.valueOf(fechaOk), horaOk, temaOk, descripcionOk, horasOk, laboratorioOk);
    }

    public void registrarParticipante(Long idProyecto, String codigoEstudiante, String responsabilidad, Double horas) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        String codigoOk = AppValidation.requiredStudentCode(codigoEstudiante);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        String responsabilidadOk = AppValidation.requiredText("Responsabilidad", responsabilidad, 5, 120);
        Double horasOk = AppValidation.range("Horas cumplidas", horas == null ? 0.0 : horas, 0, 300);
        Integer duplicado = jdbc.queryForObject("SELECT COUNT(*) FROM rsu_participante WHERE id_proyecto = ? AND codigo_estudiante = ?", Integer.class, idProyecto, codigoOk);
        if (duplicado != null && duplicado > 0) throw new IllegalArgumentException("El estudiante ya esta registrado en este proyecto.");
        jdbc.update("""
                INSERT INTO rsu_participante (id_proyecto, codigo_estudiante, responsabilidad, horas_cumplidas)
                VALUES (?, ?, ?, ?)
                """, idProyecto, codigoOk, responsabilidadOk, horasOk);
    }

    public void registrarBeneficiario(Long idProyecto, String nombres, String tipo, String documento, String taller) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        String nombresOk = AppValidation.requiredText("Nombres del beneficiario", nombres, 3, 150);
        String tipoOk = AppValidation.requiredText("Tipo de beneficiario", tipo, 3, 120);
        String documentoOk = AppValidation.optionalDniOrRuc("Documento del beneficiario", documento);
        String tallerOk = AppValidation.optionalText("Taller inscrito", taller, 180);
        jdbc.update("""
                INSERT INTO rsu_beneficiario (id_proyecto, nombres, tipo_beneficiario, documento, taller_inscrito)
                VALUES (?, ?, ?, ?, ?)
                """, idProyecto, nombresOk, tipoOk, documentoOk, tallerOk);
    }

    public void registrarAsistencia(Long idActividad, String tipoAsistente, String identificador, Boolean asistio, Double horas) {
        AppValidation.positiveId("actividad", idActividad);
        AppValidation.ensureExists(jdbc, "rsu_actividad", "id_actividad", idActividad, "Actividad");
        String tipoOk = AppValidation.oneOf("tipo de asistente", tipoAsistente, "ESTUDIANTE", "BENEFICIARIO");
        String idOk;
        if ("ESTUDIANTE".equals(tipoOk)) {
            idOk = AppValidation.requiredStudentCode(identificador);
            AppValidation.ensureExists(jdbc, "estudiante", "codigo", idOk, "Estudiante");
        } else {
            idOk = AppValidation.requiredText("Identificador", identificador, 3, 100);
        }
        Double horasOk = AppValidation.range("Horas reconocidas", horas == null ? 0.0 : horas, 0, 12);
        jdbc.update("""
                INSERT INTO rsu_asistencia (id_actividad, tipo_asistente, identificador, asistio, horas_reconocidas)
                VALUES (?, ?, ?, ?, ?)
                """, idActividad, tipoOk, idOk, Boolean.TRUE.equals(asistio), horasOk);
    }

    public void registrarEvidencia(Long idProyecto, String tipo, String descripcion) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        String tipoOk = AppValidation.requiredText("Tipo de evidencia", tipo, 3, 80);
        String descripcionOk = AppValidation.requiredText("Descripcion de evidencia", descripcion, 5, 255);
        jdbc.update("INSERT INTO rsu_evidencia (id_proyecto, tipo_evidencia, descripcion) VALUES (?, ?, ?)", idProyecto, tipoOk, descripcionOk);
    }

    public void registrarConcurso(Long idProyecto, String nombre, String criterios, String fecha, String ganadores) {
        AppValidation.positiveId("proyecto", idProyecto);
        AppValidation.ensureExists(jdbc, "rsu_proyecto", "id_proyecto", idProyecto, "Proyecto");
        String nombreOk = AppValidation.requiredText("Nombre del concurso", nombre, 5, 150);
        String criteriosOk = AppValidation.requiredText("Criterios", criterios, 10, 1000);
        LocalDate fechaOk = AppValidation.requiredDate("Fecha del concurso", fecha);
        String ganadoresOk = AppValidation.requiredText("Ganadores", ganadores, 3, 1000);
        jdbc.update("INSERT INTO rsu_concurso (id_proyecto, nombre, criterios, fecha, ganadores) VALUES (?, ?, ?, ?, ?)", idProyecto, nombreOk, criteriosOk, Date.valueOf(fechaOk), ganadoresOk);
    }

    private long contar(String tabla) {
        String tablaSegura = switch (tabla) {
            case "rsu_proyecto" -> "rsu_proyecto";
            case "rsu_participante" -> "rsu_participante";
            case "rsu_beneficiario" -> "rsu_beneficiario";
            case "rsu_evidencia" -> "rsu_evidencia";
            default -> throw new IllegalArgumentException("Tabla no permitida: " + tabla);
        };
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM " + tablaSegura, Long.class);
        return total == null ? 0 : total;
    }

    private long contarHorasCumplidas() {
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM rsu_participante pa
                INNER JOIN rsu_proyecto p ON p.id_proyecto = pa.id_proyecto
                WHERE p.tipo_proyecto = 'SERVICIO_SOCIAL_UNIVERSITARIO'
                  AND pa.horas_cumplidas >= 30
                """, Long.class);
        return total == null ? 0 : total;
    }

    private String limpiarVacio(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
