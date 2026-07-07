package com.upt.sistema.modules.sista.service;

import com.upt.sistema.shared.validation.AppValidation;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SistaMigradoService {

    private final JdbcTemplate jdbc;

    public SistaMigradoService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private int count(String sql, Object... params) {
        Integer value = jdbc.queryForObject(sql, Integer.class, params);
        return value == null ? 0 : value;
    }

    private String sqlEstadoNormalizado(String campoEstado) {
        String base = "UPPER(REPLACE(TRIM(COALESCE(" + campoEstado + ", '')), ' ', '_'))";

        return """
                CASE
                    WHEN %s IN ('EN_PROCESO', 'ENPROCESO') THEN 'EN_PROCESO'
                    WHEN %s IN ('COMPLETO', 'COMPLETADO', 'COMPLETADA') THEN 'COMPLETO'
                    WHEN %s = 'PENDIENTE' THEN 'PENDIENTE'
                    ELSE 'PENDIENTE'
                END
                """.formatted(base, base, base);
    }

    public Map<String, Object> obtenerDashboard() {
        Map<String, Object> data = new HashMap<>();

        String estado = sqlEstadoNormalizado("estado");

        data.put("totalEstudiantes", count("SELECT COUNT(*) FROM estudiante"));
        data.put("totalDocentes", count("SELECT COUNT(*) FROM docente"));
        data.put("totalCursos", count("SELECT COUNT(*) FROM curso"));
        data.put("totalIntervenciones", count("SELECT COUNT(*) FROM intervencion"));

        data.put("intervencionesPendientes", count("""
                SELECT COUNT(*)
                FROM intervencion
                WHERE %s IN ('PENDIENTE', 'EN_PROCESO')
                """.formatted(estado)));

        data.put("intervencionesCompletadas", count("""
                SELECT COUNT(*)
                FROM intervencion
                WHERE %s = 'COMPLETO'
                """.formatted(estado)));

        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM estudiante", Integer.class);

        Integer aprobados = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT mc.codigo_estudiante)
                FROM matricula_curso mc
                INNER JOIN calificacion c ON mc.id_matricula_curso = c.id_matricula_curso
                WHERE COALESCE(c.promedio, 0) >= 13
                """, Integer.class);

        double porcentaje = (total != null && total > 0 && aprobados != null)
                ? (aprobados * 100.0 / total)
                : 0.0;

        data.put("porcentajeAprobados", porcentaje);
        data.put("fechaActual", LocalDate.now().toString());

        return data;
    }

    public String obtenerConfiguracion(String clave, String valorPorDefecto) {
        List<String> rows = jdbc.queryForList("SELECT valor FROM sistema_configuracion WHERE clave = ?", String.class, clave);
        return rows.isEmpty() || !StringUtils.hasText(rows.get(0)) ? valorPorDefecto : rows.get(0);
    }

    @Transactional
    public void guardarConfiguracion(String clave, String valor) {
        String claveOk = AppValidation.oneOf("clave de configuracion", clave, "correo_alertas_inasistencia", "ultima_alerta_inasistencia_enviada");
        String valorOk;
        if ("correo_alertas_inasistencia".equals(claveOk)) {
            valorOk = AppValidation.requiredEmail("Correo de alertas", valor);
        } else {
            valorOk = AppValidation.requiredText("Valor de configuracion", valor, 1, 255);
        }
        jdbc.update("""
                INSERT INTO sistema_configuracion (clave, valor)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE valor = VALUES(valor), fecha_actualizacion = CURRENT_TIMESTAMP
                """, claveOk, valorOk);
    }

    public List<Map<String, Object>> listarAlertasInasistencia() {
        return jdbc.queryForList("""
                WITH ordered AS (
                    SELECT i.codigo_estudiante,
                           CONCAT(e.nombres, ' ', e.apellidos) AS nombre_estudiante,
                           i.fecha,
                           ROW_NUMBER() OVER (PARTITION BY i.codigo_estudiante ORDER BY i.fecha) AS rn
                    FROM tutoria_inasistencia i
                    JOIN estudiante e ON i.codigo_estudiante = e.codigo
                ),
                groups AS (
                    SELECT codigo_estudiante, nombre_estudiante, fecha,
                           DATE_SUB(fecha, INTERVAL rn DAY) AS grp
                    FROM ordered
                )
                SELECT codigo_estudiante,
                       MAX(nombre_estudiante) AS nombre_estudiante,
                       COUNT(*) AS inasistencias_consecutivas,
                       MIN(fecha) AS fecha_inicio,
                       MAX(fecha) AS fecha_fin
                FROM groups
                GROUP BY codigo_estudiante, grp
                HAVING COUNT(*) >= 3
                ORDER BY fecha_inicio DESC
                """);
    }

    public List<Map<String, Object>> listarEstudiantes(String buscar) {
        String baseSql = """
                SELECT e.codigo,
                       e.dni,
                       e.nombres,
                       e.apellidos,
                       e.fecha_nacimiento,
                       e.telefono,
                       e.email,
                       e.direccion,
                       e.id_apoderado,
                       COALESCE(CONCAT(a.nombres, ' ', a.apellidos), 'Sin apoderado') AS nombre_apoderado
                FROM estudiante e
                LEFT JOIN apoderado a ON e.id_apoderado = a.id_apoderado
                """;

        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(buscar)) {
            baseSql += """
                    WHERE LOWER(COALESCE(e.codigo, '')) LIKE LOWER(?)
                       OR LOWER(COALESCE(e.dni, '')) LIKE LOWER(?)
                       OR LOWER(COALESCE(e.nombres, '')) LIKE LOWER(?)
                       OR LOWER(COALESCE(e.apellidos, '')) LIKE LOWER(?)
                    """;

            String q = "%" + buscar.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        baseSql += " ORDER BY e.apellidos, e.nombres";

        return jdbc.queryForList(baseSql, params.toArray());
    }

    public Map<String, Object> obtenerEstudiante(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            return null;
        }

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT e.codigo,
                       e.dni,
                       e.nombres,
                       e.apellidos,
                       e.fecha_nacimiento,
                       e.telefono,
                       e.email,
                       e.direccion,
                       e.id_apoderado,
                       COALESCE(CONCAT(a.nombres, ' ', a.apellidos), 'No asignado') AS nombre_apoderado
                FROM estudiante e
                LEFT JOIN apoderado a ON e.id_apoderado = a.id_apoderado
                WHERE e.codigo = ?
                """, codigo.trim());

        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Map<String, Object>> listarCalificaciones(String codigoEstudiante) {
        if (!StringUtils.hasText(codigoEstudiante)) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT c.nombre AS curso,
                       COALESCE(cal.unidad1, 0) AS unidad1,
                       COALESCE(cal.unidad2, 0) AS unidad2,
                       COALESCE(cal.unidad3, 0) AS unidad3,
                       COALESCE(cal.promedio, 0) AS promedio,
                       COALESCE(mc.estado, 'CURSANDO') AS estado
                FROM matricula_curso mc
                INNER JOIN curso_docente cd ON mc.id_curso_docente = cd.id_curso_docente
                INNER JOIN curso c ON cd.id_curso = c.id_curso
                LEFT JOIN calificacion cal ON mc.id_matricula_curso = cal.id_matricula_curso
                WHERE mc.codigo_estudiante = ?
                ORDER BY c.nombre
                """, codigoEstudiante.trim());
    }

    public List<Map<String, Object>> listarHistorialCalificaciones(String codigoEstudiante) {
        if (!StringUtils.hasText(codigoEstudiante)) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT h.periodo, c.nombre AS curso, h.unidad1, h.unidad2, h.unidad3,
                       h.promedio, h.estado, h.fecha_registro
                FROM historial_calificacion h
                INNER JOIN curso c ON c.id_curso = h.id_curso
                WHERE h.codigo_estudiante = ?
                ORDER BY h.fecha_registro DESC, h.periodo DESC, c.nombre
                """, codigoEstudiante.trim());
    }

    public Map<String, Object> calcularFichaAcademica(String codigoEstudiante) {
        List<Map<String, Object>> notas = listarCalificaciones(codigoEstudiante);

        int totalCursos = notas.size();
        int aprobados = 0;
        int desaprobados = 0;
        int cursando = 0;
        double suma = 0;
        int conNota = 0;

        for (Map<String, Object> row : notas) {
            double promedio = row.get("promedio") == null
                    ? 0
                    : ((Number) row.get("promedio")).doubleValue();

            String estado = normalizarTexto(String.valueOf(row.getOrDefault("estado", "")));

            if (promedio > 0) {
                suma += promedio;
                conNota++;
            }

            if (estado.contains("APROBADO") || promedio >= 13) {
                aprobados++;
            } else if (estado.contains("DESAPROBADO")
                    || estado.contains("REPROBADO")
                    || (promedio > 0 && promedio < 10.5)) {
                desaprobados++;
            } else {
                cursando++;
            }
        }

        double promedioGeneral = conNota > 0 ? suma / conNota : 0.0;
        double porcentaje = totalCursos > 0 ? aprobados * 100.0 / totalCursos : 0.0;

        int inasistencias = 0;
        if (StringUtils.hasText(codigoEstudiante)) {
            inasistencias = count("""
                    SELECT COUNT(*) * 3
                    FROM intervencion i
                    LEFT JOIN tipo_intervencion ti ON i.id_tipo = ti.id_tipo
                    WHERE i.codigo_estudiante = ?
                      AND LOWER(COALESCE(ti.nombre,'')) LIKE '%asistencia%'
                    """, codigoEstudiante.trim());
        }

        String semaforo;
        String semaforoClase;

        if (desaprobados >= 3 || promedioGeneral < 10) {
            semaforo = "RIESGO ALTO";
            semaforoClase = "riesgo-alto";
        } else if (desaprobados >= 1 || promedioGeneral < 13) {
            semaforo = "RIESGO MEDIO";
            semaforoClase = "riesgo-medio";
        } else {
            semaforo = "SIN RIESGO";
            semaforoClase = "riesgo-bajo";
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalCursos", totalCursos);
        data.put("aprobados", aprobados);
        data.put("desaprobados", desaprobados);
        data.put("cursando", cursando);
        data.put("promedioGeneral", promedioGeneral);
        data.put("porcentajeAprobacion", porcentaje);
        data.put("inasistencias", inasistencias);
        data.put("reprobados", desaprobados);
        data.put("semaforo", semaforo);
        data.put("semaforoClase", semaforoClase);

        return data;
    }

    public List<Map<String, Object>> listarCiclos() {
        return jdbc.queryForList("""
                SELECT id_ciclo,
                       numero_ciclo,
                       nombre,
                       CONCAT(nombre, ' - Ciclo ', numero_ciclo) AS nombre_ciclo
                FROM ciclo
                ORDER BY numero_ciclo
                """);
    }

    public List<Map<String, Object>> listarCursosPorCiclo(Integer idCiclo) {
        if (idCiclo == null || idCiclo <= 0) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT c.id_curso,
                       c.codigo,
                       c.nombre,
                       CONCAT(c.nombre, ' (', c.codigo, ')') AS nombre_curso
                FROM curso c
                INNER JOIN ciclo_curso cc ON c.id_curso = cc.id_curso
                WHERE cc.id_ciclo = ?
                ORDER BY c.nombre
                """, idCiclo);
    }

    public List<Map<String, Object>> listarDocentes(Integer idCiclo, Integer idCurso) {
        if (idCiclo != null && idCiclo > 0 && idCurso != null && idCurso > 0) {
            return jdbc.queryForList("""
                    SELECT DISTINCT d.id_docente,
                           d.codigo,
                           d.dni,
                           d.nombres,
                           d.apellidos,
                           d.telefono,
                           d.email,
                           d.especialidad
                    FROM docente d
                    INNER JOIN curso_docente cd ON d.id_docente = cd.id_docente
                    INNER JOIN curso c ON cd.id_curso = c.id_curso
                    INNER JOIN ciclo_curso cc ON c.id_curso = cc.id_curso
                    WHERE cc.id_ciclo = ?
                      AND c.id_curso = ?
                    ORDER BY d.apellidos, d.nombres
                    """, idCiclo, idCurso);
        }

        return jdbc.queryForList("""
                SELECT id_docente,
                       codigo,
                       dni,
                       nombres,
                       apellidos,
                       telefono,
                       email,
                       especialidad
                FROM docente
                ORDER BY apellidos, nombres
                """);
    }

    public List<Map<String, Object>> listarEstudiantesPorDocenteCurso(Integer idDocente, Integer idCurso) {
        if (idDocente == null || idDocente <= 0 || idCurso == null || idCurso <= 0) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT e.codigo,
                       e.nombres,
                       e.apellidos,
                       COALESCE(cal.promedio, 0) AS promedio,
                       COALESCE(mc.estado, 'CURSANDO') AS estado
                FROM estudiante e
                INNER JOIN matricula_curso mc ON e.codigo = mc.codigo_estudiante
                INNER JOIN curso_docente cd ON mc.id_curso_docente = cd.id_curso_docente
                LEFT JOIN calificacion cal ON mc.id_matricula_curso = cal.id_matricula_curso
                WHERE cd.id_docente = ?
                  AND cd.id_curso = ?
                ORDER BY e.apellidos, e.nombres
                """, idDocente, idCurso);
    }

    public List<String> listarParentescos() {
        return List.of("MADRE", "PADRE", "APODERADO_LEGAL");
    }

    public List<Map<String, Object>> listarApoderados(String dni, String parentesco) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id_apoderado,
                       a.dni,
                       a.nombres,
                       a.apellidos,
                       a.telefono,
                       a.email,
                       a.direccion,
                       UPPER(TRIM(a.parentesco)) AS parentesco,
                       COUNT(DISTINCT e.codigo) AS total_estudiantes,
                       COUNT(DISTINCT i.id_intervencion) AS total_intervenciones,
                       COALESCE(
                              GROUP_CONCAT(CONCAT(e.nombres, ' ', e.apellidos) SEPARATOR ', '),
                           'Sin estudiantes'
                       ) AS estudiantes_asociados
                FROM apoderado a
                LEFT JOIN estudiante e ON a.id_apoderado = e.id_apoderado
                LEFT JOIN intervencion i ON e.codigo = i.codigo_estudiante
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(dni)) {
            sql.append(" AND a.dni = ? ");
            params.add(dni.trim());
        }

        if (StringUtils.hasText(parentesco)) {
            sql.append(" AND UPPER(TRIM(a.parentesco)) = UPPER(TRIM(?)) ");
            params.add(parentesco.trim());
        }

        sql.append("""
                GROUP BY a.id_apoderado,
                         a.dni,
                         a.nombres,
                         a.apellidos,
                         a.telefono,
                         a.email,
                         a.direccion,
                         UPPER(TRIM(a.parentesco))
                ORDER BY a.id_apoderado
                """);

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> buscarApoderado(Integer id) {
        if (id == null) {
            return null;
        }

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id_apoderado,
                       dni,
                       nombres,
                       apellidos,
                       telefono,
                       email,
                       direccion,
                       UPPER(TRIM(parentesco)) AS parentesco
                FROM apoderado
                WHERE id_apoderado = ?
                """, id);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> buscarApoderadoPorDni(String dni) {
        if (!StringUtils.hasText(dni)) {
            return null;
        }

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id_apoderado,
                       dni,
                       nombres,
                       apellidos,
                       telefono,
                       email,
                       direccion,
                       UPPER(TRIM(parentesco)) AS parentesco
                FROM apoderado
                WHERE dni = ?
                """, dni.trim());

        return rows.isEmpty() ? null : rows.get(0);
    }

    @Transactional
    public void guardarApoderado(Integer idApoderado,
                                 String dni,
                                 String nombres,
                                 String apellidos,
                                 String telefono,
                                 String email,
                                 String direccion,
                                 String parentesco) {

        String dniLimpio = AppValidation.requiredDni(dni);
        String nombresOk = AppValidation.requiredLetters("Nombres", nombres, 2, 100).toUpperCase();
        String apellidosOk = AppValidation.requiredLetters("Apellidos", apellidos, 2, 100).toUpperCase();
        String telefonoOk = AppValidation.requiredPhone(telefono);
        String emailOk = AppValidation.optionalEmail("Email", email);
        String direccionOk = AppValidation.requiredText("Direccion", direccion, 5, 200).toUpperCase();
        String parentescoLimpio = AppValidation.oneOf("parentesco", parentesco, "MADRE", "PADRE", "APODERADO_LEGAL");
        Integer idActual = idApoderado == null || idApoderado <= 0 ? null : idApoderado;

        if (existeApoderadoCampo("dni", dniLimpio, idActual)) {
            throw new IllegalArgumentException("Ya existe un apoderado con ese DNI.");
        }
        if (existeApoderadoCampo("telefono", telefonoOk, idActual)) {
            throw new IllegalArgumentException("Ya existe un apoderado con ese telefono.");
        }
        if (emailOk != null && existeApoderadoCampo("email", emailOk, idActual)) {
            throw new IllegalArgumentException("Ya existe un apoderado con ese email.");
        }

        if (idActual == null) {
            jdbc.update("""
                    INSERT INTO apoderado
                    (dni, nombres, apellidos, telefono, email, direccion, parentesco)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    dniLimpio,
                    nombresOk,
                    apellidosOk,
                    telefonoOk,
                    emailOk,
                    direccionOk,
                    parentescoLimpio
            );
        } else {
            jdbc.update("""
                    UPDATE apoderado
                    SET dni = ?,
                        nombres = ?,
                        apellidos = ?,
                        telefono = ?,
                        email = ?,
                        direccion = ?,
                        parentesco = ?
                    WHERE id_apoderado = ?
                    """,
                    dniLimpio,
                    nombresOk,
                    apellidosOk,
                    telefonoOk,
                    emailOk,
                    direccionOk,
                    parentescoLimpio,
                    idActual
            );
        }
    }

    private boolean existeApoderadoCampo(String campo, String valor, Integer idActual) {
        if (!StringUtils.hasText(valor)) return false;
        String campoSeguro = switch (campo) {
            case "dni" -> "dni";
            case "telefono" -> "telefono";
            case "email" -> "email";
            default -> throw new IllegalArgumentException("Campo de apoderado no permitido.");
        };
        Integer total;
        if (idActual == null) {
            total = jdbc.queryForObject("SELECT COUNT(*) FROM apoderado WHERE LOWER(" + campoSeguro + ") = LOWER(?)", Integer.class, valor);
        } else {
            total = jdbc.queryForObject("SELECT COUNT(*) FROM apoderado WHERE LOWER(" + campoSeguro + ") = LOWER(?) AND id_apoderado <> ?", Integer.class, valor, idActual);
        }
        return total != null && total > 0;
    }

    public List<Map<String, Object>> listarTiposIntervencion() {
        return jdbc.queryForList("""
                SELECT id_tipo,
                       nombre
                FROM tipo_intervencion
                ORDER BY nombre
                """);
    }

    public List<Map<String, Object>> listarEstadosIntervencion() {
        return jdbc.queryForList("""
                SELECT codigo,
                       nombre
                FROM estado_intervencion
                ORDER BY orden
                """);
    }

    public List<Map<String, Object>> listarIntervenciones(String estado, Integer idTipo) {
        String estadoExpr = sqlEstadoNormalizado("i.estado");

        StringBuilder sql = new StringBuilder("""
                SELECT i.id_intervencion,
                       i.codigo_estudiante,
                       i.id_tipo,
                       i.fecha,
                       i.motivo,
                       i.responsable,
                """);

        sql.append(estadoExpr).append(" AS estado, ");
        sql.append("COALESCE(ei.nombre, ").append(estadoExpr).append(") AS estado_nombre, ");

        sql.append("""
                       CONCAT(e.nombres, ' ', e.apellidos) AS nombre_estudiante,
                       ti.nombre AS nombre_tipo
                FROM intervencion i
                INNER JOIN estudiante e ON i.codigo_estudiante = e.codigo
                INNER JOIN tipo_intervencion ti ON i.id_tipo = ti.id_tipo
                LEFT JOIN estado_intervencion ei ON ei.codigo =
                """);

        sql.append(estadoExpr);

        sql.append("""
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(estado)) {
            sql.append(" AND ").append(estadoExpr).append(" = ? ");
            params.add(normalizarEstadoIntervencion(estado));
        }

        if (idTipo != null && idTipo > 0) {
            sql.append(" AND i.id_tipo = ? ");
            params.add(idTipo);
        }

        sql.append(" ORDER BY i.id_intervencion ASC ");

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> buscarIntervencion(Integer id) {
        if (id == null) {
            return null;
        }

        String estadoExpr = sqlEstadoNormalizado("i.estado");

        String sql = """
                SELECT i.id_intervencion,
                       i.codigo_estudiante,
                       i.id_tipo,
                       i.fecha,
                       i.motivo,
                       i.responsable,
                       i.derivacion,
                       i.observaciones,
                       %s AS estado,
                       COALESCE(ei.nombre, %s) AS estado_nombre,
                       CONCAT(e.nombres, ' ', e.apellidos) AS nombre_estudiante,
                       ti.nombre AS nombre_tipo
                FROM intervencion i
                INNER JOIN estudiante e ON i.codigo_estudiante = e.codigo
                INNER JOIN tipo_intervencion ti ON i.id_tipo = ti.id_tipo
                LEFT JOIN estado_intervencion ei ON ei.codigo = %s
                WHERE i.id_intervencion = ?
                """.formatted(estadoExpr, estadoExpr, estadoExpr);

        List<Map<String, Object>> rows = jdbc.queryForList(sql, id);

        return rows.isEmpty() ? null : rows.get(0);
    }

    @Transactional
    public void guardarIntervencion(Integer idIntervencion,
                                    String codigoEstudiante,
                                    Integer idTipo,
                                    String fecha,
                                    String motivo,
                                    String responsable,
                                    String estado,
                                    String derivacion,
                                    String observaciones) {

        Integer idTipoOk = AppValidation.positiveId("tipo de intervencion", idTipo);
        AppValidation.ensureExists(jdbc, "tipo_intervencion", "id_tipo", idTipoOk, "Tipo de intervencion");
        Date fechaSql = Date.valueOf(AppValidation.dateNotFuture("Fecha de intervencion", fecha));
        String motivoOk = AppValidation.requiredText("Motivo", motivo, 10, 1000).toUpperCase();
        String responsableOk = AppValidation.requiredLetters("Responsable", responsable, 3, 120).toUpperCase();
        String estadoLimpio = normalizarEstadoIntervencion(estado);
        String derivacionOk = AppValidation.optionalText("Derivacion", derivacion, 120);
        String observacionesOk = AppValidation.optionalText("Observaciones", observaciones, 1000);
        if (derivacionOk != null) derivacionOk = derivacionOk.toUpperCase();
        if (observacionesOk != null) observacionesOk = observacionesOk.toUpperCase();

        if (idIntervencion == null || idIntervencion <= 0) {
            String codigoOk = AppValidation.requiredStudentCode(codigoEstudiante);
            AppValidation.ensureExists(jdbc, "estudiante", "codigo", codigoOk, "Estudiante");
            jdbc.update("""
                    INSERT INTO intervencion
                    (codigo_estudiante, id_tipo, fecha, motivo, responsable, estado, derivacion, observaciones)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    codigoOk,
                    idTipoOk,
                    fechaSql,
                    motivoOk,
                    responsableOk,
                    estadoLimpio,
                    derivacionOk,
                    observacionesOk
            );
        } else {
            AppValidation.ensureExists(jdbc, "intervencion", "id_intervencion", idIntervencion, "Intervencion");
            jdbc.update("""
                    UPDATE intervencion
                    SET id_tipo = ?,
                        fecha = ?,
                        motivo = ?,
                        responsable = ?,
                        estado = ?,
                        derivacion = ?,
                        observaciones = ?
                    WHERE id_intervencion = ?
                    """,
                    idTipoOk,
                    fechaSql,
                    motivoOk,
                    responsableOk,
                    estadoLimpio,
                    derivacionOk,
                    observacionesOk,
                    idIntervencion
            );
        }
    }

    public List<Map<String, Object>> listarSeguimientos(Integer idIntervencion) {
        if (idIntervencion == null || idIntervencion <= 0) {
            return List.of();
        }

        return jdbc.queryForList("""
                SELECT id_registro,
                       id_intervencion,
                       fecha_seguimiento,
                       descripcion,
                       acuerdos
                FROM registro_seguimiento
                WHERE id_intervencion = ?
                ORDER BY fecha_seguimiento DESC, id_registro DESC
                """, idIntervencion);
    }

    public Map<String, Object> buscarSeguimiento(Integer idRegistro) {
        if (idRegistro == null) {
            return null;
        }

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT *
                FROM registro_seguimiento
                WHERE id_registro = ?
                """, idRegistro);

        return rows.isEmpty() ? null : rows.get(0);
    }

    @Transactional
    public void guardarSeguimiento(Integer idRegistro,
                                   Integer idIntervencion,
                                   String fechaSeguimiento,
                                   String descripcion,
                                   String acuerdos) {

        Integer idIntervencionOk = AppValidation.positiveId("intervencion", idIntervencion);
        AppValidation.ensureExists(jdbc, "intervencion", "id_intervencion", idIntervencionOk, "Intervencion");
        Date fecha = Date.valueOf(AppValidation.dateNotFuture("Fecha de seguimiento", fechaSeguimiento));
        String descripcionOk = AppValidation.requiredText("Descripcion", descripcion, 10, 1000).toUpperCase();
        String acuerdosOk = AppValidation.optionalText("Acuerdos", acuerdos, 1000);
        if (acuerdosOk != null) acuerdosOk = acuerdosOk.toUpperCase();

        if (idRegistro == null || idRegistro <= 0) {
            jdbc.update("""
                    INSERT INTO registro_seguimiento
                    (id_intervencion, fecha_seguimiento, descripcion, acuerdos)
                    VALUES (?, ?, ?, ?)
                    """,
                    idIntervencionOk,
                    fecha,
                    descripcionOk,
                    acuerdosOk
            );
        } else {
            jdbc.update("""
                    UPDATE registro_seguimiento
                    SET fecha_seguimiento = ?,
                        descripcion = ?,
                        acuerdos = ?
                    WHERE id_registro = ?
                    """,
                    fecha,
                    descripcionOk,
                    acuerdosOk,
                    idRegistro
            );
        }
    }

    private String mayus(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String normalizarTexto(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .toUpperCase()
                .replace(" ", "_");
    }

    private String normalizarEstadoIntervencion(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Debe seleccionar el estado de la intervencion.");
        }

        String estado = normalizarTexto(value);

        if ("ENPROCESO".equals(estado) || "EN_PROCESO".equals(estado)) {
            return "EN_PROCESO";
        }

        if ("COMPLETADO".equals(estado) || "COMPLETADA".equals(estado) || "COMPLETO".equals(estado)) {
            return "COMPLETO";
        }

        if ("PENDIENTE".equals(estado)) {
            return "PENDIENTE";
        }

        throw new IllegalArgumentException("Estado de intervencion invalido.");
    }

    private String limpiarVacio(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String limpiarObligatorio(String value) {
        return value == null ? "" : value.trim();
    }
}
