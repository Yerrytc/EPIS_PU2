package com.upt.sistema.modules.tutoria.service;

import com.upt.sistema.shared.validation.AppValidation;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TutoriaService {

    private final JdbcTemplate jdbc;

    public TutoriaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Long> resumen() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("Seguimientos", contar("tutoria_estudiante_seguimiento"));
        data.put("Riesgo académico", contarRiesgo());
        data.put("Alertas de faltas", contarAlertasFaltas());
        data.put("Derivaciones", contarWhere("tutoria_entrevista", "derivacion_area IS NOT NULL AND derivacion_area <> ''"));
        data.put("Charlas", contar("tutoria_charla"));
        return data;
    }

    public List<Map<String, Object>> estudiantesSeguimiento() {
        return jdbc.queryForList("""
                SELECT s.id_seguimiento, s.codigo_estudiante, e.nombres, e.apellidos,
                       s.motivo, s.tipo_seguimiento, s.unidad_academica, s.estado,
                       s.observacion_positiva, s.advertencia_academica
                FROM tutoria_estudiante_seguimiento s
                INNER JOIN estudiante e ON e.codigo = s.codigo_estudiante
                ORDER BY s.id_seguimiento DESC
                """);
    }

    public List<Map<String, Object>> estudiantesRiesgo() {
        return jdbc.queryForList("""
                SELECT e.codigo, e.nombres, e.apellidos,
                       SUM(CASE WHEN COALESCE(cal.promedio, 0) < 10.5 AND COALESCE(cal.promedio, 0) > 0 THEN 1 ELSE 0 END) AS cursos_desaprobados,
                       MAX(COALESCE(m.numero_matricula, 1)) AS mayor_matricula,
                       CASE
                           WHEN MAX(COALESCE(m.numero_matricula, 1)) >= 4 THEN 'RIESGO CRITICO'
                           WHEN MAX(COALESCE(m.numero_matricula, 1)) = 3 THEN 'RIESGO ALTO'
                           WHEN SUM(CASE WHEN COALESCE(cal.promedio, 0) < 10.5 AND COALESCE(cal.promedio, 0) > 0 THEN 1 ELSE 0 END) > 0 THEN 'RIESGO ACADEMICO'
                           ELSE 'SIN RIESGO'
                       END AS nivel_riesgo
                FROM estudiante e
                LEFT JOIN matricula_curso m ON m.codigo_estudiante = e.codigo
                LEFT JOIN calificacion cal ON cal.id_matricula_curso = m.id_matricula_curso
                GROUP BY e.codigo, e.nombres, e.apellidos
                HAVING SUM(CASE WHEN COALESCE(cal.promedio, 0) < 10.5 AND COALESCE(cal.promedio, 0) > 0 THEN 1 ELSE 0 END) > 0
                    OR MAX(COALESCE(m.numero_matricula, 1)) >= 3
                ORDER BY mayor_matricula DESC, cursos_desaprobados DESC, e.apellidos
                """);
    }

    public List<Map<String, Object>> notas() {
        return jdbc.queryForList("""
                SELECT n.id_nota, n.codigo_estudiante, e.nombres, e.apellidos, c.nombre AS curso,
                       n.unidad_academica, n.nota, n.observacion
                FROM tutoria_nota n
                INNER JOIN estudiante e ON e.codigo = n.codigo_estudiante
                INNER JOIN curso c ON c.id_curso = n.id_curso
                ORDER BY n.id_nota DESC
                """);
    }

    public List<Map<String, Object>> notasPorEstudiante(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT c.codigo AS codigo_curso, c.nombre AS curso,
                       COALESCE(cal.unidad1, 0) AS unidad1,
                       COALESCE(cal.unidad2, 0) AS unidad2,
                       COALESCE(cal.unidad3, 0) AS unidad3,
                       COALESCE(cal.promedio, 0) AS promedio,
                       COALESCE(m.estado, 'CURSANDO') AS estado,
                       COALESCE(m.numero_matricula, 1) AS numero_matricula
                FROM matricula_curso m
                INNER JOIN curso_docente cd ON cd.id_curso_docente = m.id_curso_docente
                INNER JOIN curso c ON c.id_curso = cd.id_curso
                LEFT JOIN calificacion cal ON cal.id_matricula_curso = m.id_matricula_curso
                WHERE m.codigo_estudiante = ?
                ORDER BY c.nombre
                """, codigo.trim());
    }

    public List<Map<String, Object>> historialNotas(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT h.periodo, c.nombre AS curso, h.unidad1, h.unidad2, h.unidad3,
                       h.promedio, h.estado, h.fecha_registro
                FROM historial_calificacion h
                INNER JOIN curso c ON c.id_curso = h.id_curso
                WHERE h.codigo_estudiante = ?
                ORDER BY h.fecha_registro DESC, h.periodo DESC, c.nombre
                """, codigo.trim());
    }

    public List<Map<String, Object>> citaciones() {
        return jdbc.queryForList("""
                SELECT c.id_citacion, c.codigo_estudiante, e.nombres, e.apellidos,
                       c.fecha_citacion, c.motivo, c.estado
                FROM tutoria_citacion c
                INNER JOIN estudiante e ON e.codigo = c.codigo_estudiante
                ORDER BY c.fecha_citacion DESC, c.id_citacion DESC
                """);
    }

    public List<Map<String, Object>> entrevistas() {
        return jdbc.queryForList("""
                SELECT en.id_entrevista, en.codigo_estudiante, e.nombres, e.apellidos,
                       en.fecha, en.problemas_detectados, en.recomendaciones, en.derivacion_area
                FROM tutoria_entrevista en
                INNER JOIN estudiante e ON e.codigo = en.codigo_estudiante
                ORDER BY en.fecha DESC, en.id_entrevista DESC
                """);
    }

    public List<Map<String, Object>> inasistenciasFrecuentes() {
        return jdbc.queryForList("""
                SELECT i.codigo_estudiante, e.nombres, e.apellidos, COUNT(*) AS total_faltas,
                       CASE WHEN COUNT(*) >= 4 THEN 'ALERTA 4 FALTAS' WHEN COUNT(*) >= 3 THEN 'ALERTA 3 FALTAS' ELSE 'OBSERVACION' END AS alerta
                FROM tutoria_inasistencia i
                INNER JOIN estudiante e ON e.codigo = i.codigo_estudiante
                GROUP BY i.codigo_estudiante, e.nombres, e.apellidos
                HAVING COUNT(*) >= 3
                ORDER BY total_faltas DESC, e.apellidos
                """);
    }

    public List<Map<String, Object>> justificaciones() {
        return jdbc.queryForList("""
                SELECT j.id_justificacion, j.codigo_estudiante, e.nombres, e.apellidos,
                        c.nombre AS curso, j.fecha_reincorporacion, j.fecha_presentacion,
                        j.dias_faltados, j.estado, j.estado_revision, j.observacion_revision, j.motivo
                FROM tutoria_justificacion j
                INNER JOIN estudiante e ON e.codigo = j.codigo_estudiante
                INNER JOIN curso c ON c.id_curso = j.id_curso
                ORDER BY j.id_justificacion DESC
                """);
    }

    public List<Map<String, Object>> justificacionesPorEstudiante(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT j.id_justificacion, j.codigo_estudiante, c.nombre AS curso,
                       j.fecha_reincorporacion, j.fecha_presentacion, j.dias_faltados,
                       j.estado, j.estado_revision, j.observacion_revision, j.motivo
                FROM tutoria_justificacion j
                INNER JOIN curso c ON c.id_curso = j.id_curso
                WHERE j.codigo_estudiante = ?
                ORDER BY j.id_justificacion DESC
                """, codigo.trim());
    }

    public List<Map<String, Object>> charlas() {
        return jdbc.queryForList("SELECT * FROM tutoria_charla ORDER BY fecha DESC, id_charla DESC");
    }

    public List<Map<String, Object>> comunicados() {
        return jdbc.queryForList("SELECT * FROM tutoria_comunicado ORDER BY fecha DESC, id_comunicado DESC");
    }

    public List<Map<String, Object>> estudiantes() {
        return jdbc.queryForList("SELECT codigo, CONCAT(nombres, ' ', apellidos) AS nombre FROM estudiante ORDER BY apellidos, nombres");
    }

    public Map<String, Object> estudiantePorLogin(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT e.codigo, CONCAT(e.nombres, ' ', e.apellidos) AS nombre
                FROM usuarios_login ul
                INNER JOIN usuarios u ON u.id_usuario = ul.id_usuario
                INNER JOIN estudiante e ON e.codigo = u.codigo_alumno
                WHERE ul.username = ?
                """, username.trim());

        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Map<String, Object>> cursos() {
        return jdbc.queryForList("SELECT id_curso, CONCAT(nombre, ' (', codigo, ')') AS nombre FROM curso ORDER BY nombre");
    }

    public List<Map<String, Object>> docentes() {
        return jdbc.queryForList("SELECT id_docente, CONCAT(nombres, ' ', apellidos) AS nombre FROM docente ORDER BY apellidos, nombres");
    }

    @Transactional
    public void registrarSeguimiento(String codigo, String motivo, String tipo, Integer unidad, String estado, String positivo, String advertencia) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        String motivoOk = AppValidation.requiredText("Motivo", motivo, 5, 255);
        String tipoOk = AppValidation.oneOf("tipo de seguimiento", tipo, "RIESGO_ACADEMICO", "BECADO", "TERCERA_MATRICULA", "CUARTA_MATRICULA", "SITUACION_ESPECIAL");
        Integer unidadOk = AppValidation.range("Unidad academica", unidad, 1, 3);
        String estadoOk = AppValidation.oneOf("estado", estado, "ABIERTO", "EN_SEGUIMIENTO", "CERRADO");
        String positivoOk = AppValidation.optionalText("Observacion positiva", positivo, 500);
        String advertenciaOk = AppValidation.optionalText("Advertencia academica", advertencia, 500);
        jdbc.update("""
                INSERT INTO tutoria_estudiante_seguimiento
                (codigo_estudiante, motivo, tipo_seguimiento, unidad_academica, estado, observacion_positiva, advertencia_academica)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, codigoOk, motivoOk, tipoOk, unidadOk, estadoOk, positivoOk, advertenciaOk);
    }

    public void registrarNota(String codigo, Integer idCurso, Integer unidad, Double nota, String observacion) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        Integer cursoOk = AppValidation.positiveId("curso", idCurso);
        AppValidation.ensureExists(jdbc, "curso", "id_curso", cursoOk, "Curso");
        Integer unidadOk = AppValidation.range("Unidad academica", unidad, 1, 3);
        Double notaOk = AppValidation.range("Nota", nota, 0, 20);
        String obsOk = AppValidation.optionalText("Observacion", observacion, 255);
        jdbc.update("""
                INSERT INTO tutoria_nota (codigo_estudiante, id_curso, unidad_academica, nota, observacion)
                VALUES (?, ?, ?, ?, ?)
                """, codigoOk, cursoOk, unidadOk, notaOk, obsOk);
    }

    public void registrarCitacion(String codigo, String fecha, String motivo, String estado) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        LocalDate fechaOk = AppValidation.requiredDate("Fecha de citacion", fecha);
        String motivoOk = AppValidation.requiredText("Motivo", motivo, 5, 255);
        String estadoOk = AppValidation.oneOf("estado", estado, "PENDIENTE", "ATENDIDO", "REPROGRAMADO", "REALIZADA");
        jdbc.update("INSERT INTO tutoria_citacion (codigo_estudiante, fecha_citacion, motivo, estado) VALUES (?, ?, ?, ?)", codigoOk, Date.valueOf(fechaOk), motivoOk, estadoOk);
    }

    public void registrarEntrevista(String codigo, String fecha, String problemas, String observaciones, String recomendaciones, String derivacionArea) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        LocalDate fechaOk = AppValidation.dateNotFuture("Fecha de entrevista", fecha);
        String problemasOk = AppValidation.requiredText("Problemas detectados", problemas, 10, 1000);
        String observacionesOk = AppValidation.requiredText("Observaciones", observaciones, 10, 1000);
        String recomendacionesOk = AppValidation.requiredText("Recomendaciones", recomendaciones, 10, 1000);
        String derivacionOk = limpiarVacio(derivacionArea);
        if (derivacionOk != null) {
            derivacionOk = AppValidation.oneOf("derivacion", derivacionOk, "PSICOLOGIA", "MEDICO", "ASISTENTE_SOCIAL", "BIENESTAR_UNIVERSITARIO", "ORIENTACION_VOCACIONAL");
        }
        jdbc.update("""
                INSERT INTO tutoria_entrevista
                (codigo_estudiante, fecha, problemas_detectados, observaciones, recomendaciones, derivacion_area)
                VALUES (?, ?, ?, ?, ?, ?)
                """, codigoOk, Date.valueOf(fechaOk), problemasOk, observacionesOk, recomendacionesOk, derivacionOk);
    }

    public void registrarInasistencia(String codigo, Integer idCurso, Integer idDocente, String fecha, String motivo) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        Integer cursoOk = AppValidation.positiveId("curso", idCurso);
        Integer docenteOk = AppValidation.positiveId("docente", idDocente);
        AppValidation.ensureExists(jdbc, "curso", "id_curso", cursoOk, "Curso");
        AppValidation.ensureExists(jdbc, "docente", "id_docente", docenteOk, "Docente");
        LocalDate fechaOk = AppValidation.dateNotFuture("Fecha de inasistencia", fecha);
        String motivoOk = AppValidation.optionalText("Motivo", motivo, 255);
        jdbc.update("""
                INSERT INTO tutoria_inasistencia (codigo_estudiante, id_curso, id_docente, fecha, motivo)
                VALUES (?, ?, ?, ?, ?)
                """, codigoOk, cursoOk, docenteOk, Date.valueOf(fechaOk), motivoOk);
    }

    public void registrarJustificacion(String codigo, Integer idCurso, Integer idDocente, String fechaReincorporacion, String fechaPresentacion, Integer dias, String motivo, String evidencia) {
        String codigoOk = AppValidation.requiredStudentCode(codigo);
        AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
        Integer cursoOk = AppValidation.positiveId("curso", idCurso);
        Integer docenteOk = AppValidation.positiveId("docente", idDocente);
        AppValidation.ensureExists(jdbc, "curso", "id_curso", cursoOk, "Curso");
        AppValidation.ensureExists(jdbc, "docente", "id_docente", docenteOk, "Docente");
        LocalDate reincorporacion = AppValidation.requiredDate("Fecha de reincorporacion", fechaReincorporacion);
        LocalDate presentacion = AppValidation.requiredDate("Fecha de presentacion", fechaPresentacion);
        if (presentacion.isBefore(reincorporacion)) {
            throw new IllegalArgumentException("La fecha de presentacion no puede ser anterior a la reincorporacion.");
        }
        Integer diasOk = AppValidation.range("Dias faltados", dias, 1, 30);
        String motivoOk = AppValidation.requiredText("Motivo", motivo, 10, 1000);
        String evidenciaOk = AppValidation.optionalText("Sustento textual", evidencia, 255);
        String estado = ChronoUnit.DAYS.between(reincorporacion, presentacion) <= 2 ? "EN_PLAZO" : "FUERA_PLAZO";

        jdbc.update("""
                INSERT INTO tutoria_justificacion
                (codigo_estudiante, id_curso, id_docente, fecha_reincorporacion, fecha_presentacion, dias_faltados, motivo, evidencia, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, codigoOk, cursoOk, docenteOk, Date.valueOf(reincorporacion), Date.valueOf(presentacion), diasOk, motivoOk, evidenciaOk, estado);

    }

    public void registrarCharla(String titulo, String tema, String ciclos, String fecha, String responsable) {
        String tituloOk = AppValidation.requiredText("Titulo", titulo, 5, 180);
        String temaOk = AppValidation.requiredText("Tema", tema, 3, 120);
        String ciclosOk = AppValidation.requiredText("Ciclos dirigidos", ciclos, 3, 120);
        LocalDate fechaOk = AppValidation.requiredDate("Fecha", fecha);
        String responsableOk = AppValidation.requiredLetters("Responsable", responsable, 3, 120);
        jdbc.update("INSERT INTO tutoria_charla (titulo, tema, ciclos_dirigidos, fecha, responsable) VALUES (?, ?, ?, ?, ?)", tituloOk, temaOk, ciclosOk, Date.valueOf(fechaOk), responsableOk);
    }

    public void registrarComunicado(String titulo, String mensaje, String fecha, String destinatarios) {
        String tituloOk = AppValidation.requiredText("Titulo", titulo, 5, 180);
        String mensajeOk = AppValidation.requiredText("Mensaje", mensaje, 10, 2000);
        LocalDate fechaOk = AppValidation.requiredDate("Fecha", fecha);
        String destinatariosOk = AppValidation.requiredText("Destinatarios", destinatarios, 3, 120);
        jdbc.update("INSERT INTO tutoria_comunicado (titulo, mensaje, fecha, destinatarios) VALUES (?, ?, ?, ?)", tituloOk, mensajeOk, Date.valueOf(fechaOk), destinatariosOk);
    }

    public void revisarJustificacion(Long idJustificacion, String estadoRevision, String observacion) {
        AppValidation.positiveId("justificacion", idJustificacion);
        AppValidation.ensureExists(jdbc, "tutoria_justificacion", "id_justificacion", idJustificacion, "Justificacion");
        String estado = AppValidation.oneOf("estado de revision", estadoRevision, "APROBADO", "RECHAZADO");
        String obsOk = AppValidation.optionalText("Observacion de revision", observacion, 255);
        if ("RECHAZADO".equals(estado) && obsOk == null) {
            throw new IllegalArgumentException("Debe ingresar una observacion cuando rechaza una justificacion.");
        }
        jdbc.update("""
                UPDATE tutoria_justificacion
                SET estado_revision = ?, observacion_revision = ?
                WHERE id_justificacion = ?
                """, estado, obsOk, idJustificacion);
    }

    private long contar(String tabla) {
        String tablaSegura = switch (tabla) {
            case "tutoria_estudiante_seguimiento" -> "tutoria_estudiante_seguimiento";
            case "tutoria_charla" -> "tutoria_charla";
            default -> throw new IllegalArgumentException("Tabla no permitida: " + tabla);
        };
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM " + tablaSegura, Long.class);
        return total == null ? 0 : total;
    }

    private long contarWhere(String tabla, String where) {
        if (!"tutoria_entrevista".equals(tabla)
                || !"derivacion_area IS NOT NULL AND derivacion_area <> ''".equals(where)) {
            throw new IllegalArgumentException("Consulta no permitida");
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM tutoria_entrevista WHERE derivacion_area IS NOT NULL AND derivacion_area <> ''", Long.class);
        return total == null ? 0 : total;
    }

    private long contarRiesgo() {
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT e.codigo
                    FROM estudiante e
                    LEFT JOIN matricula_curso m ON m.codigo_estudiante = e.codigo
                    LEFT JOIN calificacion cal ON cal.id_matricula_curso = m.id_matricula_curso
                    GROUP BY e.codigo
                    HAVING SUM(CASE WHEN COALESCE(cal.promedio, 0) < 10.5 AND COALESCE(cal.promedio, 0) > 0 THEN 1 ELSE 0 END) > 0
                       OR MAX(COALESCE(m.numero_matricula, 1)) >= 3
                ) riesgo
                """, Long.class);
        return total == null ? 0 : total;
    }

    private long contarAlertasFaltas() {
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT codigo_estudiante FROM tutoria_inasistencia GROUP BY codigo_estudiante HAVING COUNT(*) >= 3
                ) alertas
                """, Long.class);
        return total == null ? 0 : total;
    }

    private String obtenerConfiguracion(String clave, String valorPorDefecto) {
        List<String> rows = jdbc.queryForList("SELECT valor FROM sistema_configuracion WHERE clave = ?", String.class, clave);
        return rows.isEmpty() || !StringUtils.hasText(rows.get(0)) ? valorPorDefecto : rows.get(0);
    }

    private String limpiarVacio(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
