package com.upt.sistema.modules.sista.controller;

import com.upt.sistema.core.annotation.Modulo;
import com.upt.sistema.modules.sista.service.SistaMigradoService;
import com.upt.sistema.shared.service.NotificationService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sista")
@Modulo(codigo = "SISTA", nombre = "SISTA - Sistema de Seguimiento y Asesoria", rutaBase = "/sista")
public class SistaMigradoController {

    private final SistaMigradoService service;
    private final NotificationService notificationService;

    public SistaMigradoController(SistaMigradoService service, NotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
    }

    // ============================================================
    // PANEL PRINCIPAL SISTA
    // URL: http://localhost:8087/sista
    // Vista: templates/sista/index.html
    // ============================================================
    @GetMapping({"", "/"})
    public String panel(Model model) {
        model.addAttribute("stats", service.obtenerDashboard());
        model.addAttribute("correoAlertas", service.obtenerConfiguracion("correo_alertas_inasistencia", "tutoria@upt.edu.pe"));
        model.addAttribute("alertasInasistencia", service.listarAlertasInasistencia());
        return "sista/index";
    }

    @PostMapping("/configuracion/correo-alertas")
    public String guardarCorreoAlertas(@RequestParam String correoAlertas, RedirectAttributes redirect) {
        try {
            service.guardarConfiguracion("correo_alertas_inasistencia", correoAlertas);
            redirect.addFlashAttribute("ok", "Correo de alertas actualizado correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/sista";
    }

    @GetMapping("/enviar-alertas")
    public String enviarAlertas(RedirectAttributes redirect) {
        try {
            var alertas = service.listarAlertasInasistencia();
            if (alertas.isEmpty()) {
                redirect.addFlashAttribute("ok", "No hay alertas de inasistencia que enviar.");
                return "redirect:/sista";
            }
            String destinatario = service.obtenerConfiguracion("correo_alertas_inasistencia", "");
            if (destinatario.isBlank()) {
                redirect.addFlashAttribute("error", "Configure un correo destino primero.");
                return "redirect:/sista";
            }
            StringBuilder html = new StringBuilder();
            html.append("<h2>Alertas de inasistencia - EPIS</h2>");
            html.append("<p>Estudiantes con 3 o mas inasistencias consecutivas:</p><table border='1' cellpadding='6' style='border-collapse:collapse'><tr><th>Estudiante</th><th>Inasistencias</th><th>Periodo</th></tr>");
            for (var a : alertas) {
                html.append("<tr><td>").append(a.get("nombre_estudiante")).append("</td><td>").append(a.get("inasistencias_consecutivas")).append("</td><td>").append(a.get("fecha_inicio")).append(" a ").append(a.get("fecha_fin")).append("</td></tr>");
            }
            html.append("</table>");
            notificationService.sendHtmlMessage(destinatario, "[EPIS] Alertas de inasistencia", html.toString());
            service.guardarConfiguracion("ultima_alerta_inasistencia_enviada", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            redirect.addFlashAttribute("ok", "Alerta enviada correctamente a " + destinatario);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al enviar alerta: " + e.getMessage());
        }
        return "redirect:/sista";
    }

    // ============================================================
    // ESTUDIANTES
    // URL: http://localhost:8087/sista/estudiantes
    // ============================================================
    @GetMapping("/estudiantes")
    public String estudiantes(@RequestParam(required = false) String buscar,
                              Model model) {

        model.addAttribute("buscar", buscar);
        model.addAttribute("estudiantes", service.listarEstudiantes(buscar));

        return "sista/estudiantes";
    }

    // ============================================================
    // FICHA DEL ESTUDIANTE
    // URL: http://localhost:8087/sista/estudiantes/{codigo}
    // ============================================================
    @GetMapping("/estudiantes/{codigo}")
    public String fichaEstudiante(@PathVariable String codigo,
                                  Model model,
                                  RedirectAttributes redirect) {

        Map<String, Object> estudiante = service.obtenerEstudiante(codigo);

        if (estudiante == null) {
            redirect.addFlashAttribute("error", "No se encontró el estudiante solicitado.");
            return "redirect:/sista/estudiantes";
        }

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("calificaciones", service.listarCalificaciones(codigo));
        model.addAttribute("historialCalificaciones", service.listarHistorialCalificaciones(codigo));
        model.addAttribute("academico", service.calcularFichaAcademica(codigo));

        return "sista/estudiante-ficha";
    }

    // ============================================================
    // DOCENTES
    // URL: http://localhost:8087/sista/docentes
    // ============================================================
    @GetMapping("/docentes")
    public String docentes(@RequestParam(required = false) Integer idCiclo,
                           @RequestParam(required = false) Integer idCurso,
                           @RequestParam(required = false) Integer idDocente,
                           Model model) {

        model.addAttribute("idCiclo", idCiclo);
        model.addAttribute("idCurso", idCurso);
        model.addAttribute("idDocente", idDocente);

        model.addAttribute("ciclos", service.listarCiclos());
        model.addAttribute("cursos", service.listarCursosPorCiclo(idCiclo));
        model.addAttribute("docentes", service.listarDocentes(idCiclo, idCurso));
        model.addAttribute("estudiantesDocente", service.listarEstudiantesPorDocenteCurso(idDocente, idCurso));

        return "sista/docentes";
    }

    // ============================================================
    // APODERADOS
    // URL: http://localhost:8087/sista/apoderados
    // ============================================================
    @GetMapping("/apoderados")
    public String apoderados(@RequestParam(required = false) String dni,
                             @RequestParam(required = false) String parentesco,
                             Model model) {

        model.addAttribute("dni", dni);
        model.addAttribute("parentesco", parentesco);
        model.addAttribute("parentescos", service.listarParentescos());
        model.addAttribute("apoderados", service.listarApoderados(dni, parentesco));

        return "sista/apoderados";
    }

    @GetMapping("/apoderados/nuevo")
    public String nuevoApoderado(Model model) {

        model.addAttribute("apoderado", new HashMap<String, Object>());
        model.addAttribute("parentescos", service.listarParentescos());
        model.addAttribute("formAction", "/sista/apoderados/guardar");
        model.addAttribute("modo", "nuevo");

        return "sista/apoderado-formulario";
    }

    @GetMapping("/apoderados/editar/{id}")
    public String editarApoderado(@PathVariable Integer id,
                                  Model model,
                                  RedirectAttributes redirect) {

        Map<String, Object> apoderado = service.buscarApoderado(id);

        if (apoderado == null) {
            redirect.addFlashAttribute("error", "No se encontró el apoderado solicitado.");
            return "redirect:/sista/apoderados";
        }

        model.addAttribute("apoderado", apoderado);
        model.addAttribute("parentescos", service.listarParentescos());
        model.addAttribute("formAction", "/sista/apoderados/guardar");
        model.addAttribute("modo", "editar");

        return "sista/apoderado-formulario";
    }

    @PostMapping("/apoderados/guardar")
    public String guardarApoderado(@RequestParam(required = false) Integer idApoderado,
                                   @RequestParam String dni,
                                   @RequestParam String nombres,
                                   @RequestParam String apellidos,
                                   @RequestParam String telefono,
                                   @RequestParam(required = false) String email,
                                   @RequestParam String direccion,
                                   @RequestParam String parentesco,
                                   RedirectAttributes redirect) {

        try {
            Map<String, Object> existe = service.buscarApoderadoPorDni(dni);

            if ((idApoderado == null || idApoderado <= 0) && existe != null) {
                redirect.addFlashAttribute("error", "Ya existe un apoderado con el DNI " + dni);
                return "redirect:/sista/apoderados/nuevo";
            }

            service.guardarApoderado(
                    idApoderado,
                    dni,
                    nombres,
                    apellidos,
                    telefono,
                    email,
                    direccion,
                    parentesco
            );

            redirect.addFlashAttribute("ok", "Apoderado guardado correctamente.");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar apoderado: " + e.getMessage());

            if (idApoderado == null || idApoderado <= 0) {
                return "redirect:/sista/apoderados/nuevo";
            }

            return "redirect:/sista/apoderados/editar/" + idApoderado;
        }

        return "redirect:/sista/apoderados";
    }

    // ============================================================
    // INTERVENCIONES
    // URL: http://localhost:8087/sista/intervenciones
    // ============================================================
    @GetMapping("/intervenciones")
    public String intervenciones(@RequestParam(required = false) String estado,
                                 @RequestParam(required = false) Integer idTipo,
                                 Model model) {

        model.addAttribute("estado", estado);
        model.addAttribute("idTipo", idTipo);

        // Estados desde la tabla estado_intervencion:
        // PENDIENTE, EN PROCESO, COMPLETADA
        model.addAttribute("estados", service.listarEstadosIntervencion());

        model.addAttribute("tipos", service.listarTiposIntervencion());
        model.addAttribute("intervenciones", service.listarIntervenciones(estado, idTipo));

        return "sista/intervenciones";
    }

    @GetMapping("/intervenciones/nuevo")
    public String nuevaIntervencion(Model model) {

        Map<String, Object> intervencion = new HashMap<>();
        intervencion.put("fecha", LocalDate.now().toString());
        intervencion.put("estado", "PENDIENTE");

        model.addAttribute("intervencion", intervencion);
        model.addAttribute("estudiantes", service.listarEstudiantes(null));
        model.addAttribute("tipos", service.listarTiposIntervencion());
        model.addAttribute("estados", service.listarEstadosIntervencion());
        model.addAttribute("formAction", "/sista/intervenciones/guardar");
        model.addAttribute("modo", "nuevo");

        return "sista/intervencion-formulario";
    }

    @GetMapping("/intervenciones/editar/{id}")
    public String editarIntervencion(@PathVariable Integer id,
                                     Model model,
                                     RedirectAttributes redirect) {

        Map<String, Object> intervencion = service.buscarIntervencion(id);

        if (intervencion == null) {
            redirect.addFlashAttribute("error", "No se encontró la intervención solicitada.");
            return "redirect:/sista/intervenciones";
        }

        model.addAttribute("intervencion", intervencion);
        model.addAttribute("estudiantes", service.listarEstudiantes(null));
        model.addAttribute("tipos", service.listarTiposIntervencion());
        model.addAttribute("estados", service.listarEstadosIntervencion());
        model.addAttribute("formAction", "/sista/intervenciones/guardar");
        model.addAttribute("modo", "editar");

        return "sista/intervencion-formulario";
    }

    @PostMapping("/intervenciones/guardar")
    public String guardarIntervencion(@RequestParam(required = false) Integer idIntervencion,
                                      @RequestParam(required = false) String codigoEstudiante,
                                      @RequestParam(required = false) Integer idTipo,
                                      @RequestParam(required = false) String fecha,
                                      @RequestParam(required = false) String motivo,
                                      @RequestParam(required = false) String responsable,
                                      @RequestParam(required = false) String estado,
                                      @RequestParam(required = false) String derivacion,
                                      @RequestParam(required = false) String observaciones,
                                      RedirectAttributes redirect) {

        try {
            if (idTipo == null || idTipo <= 0) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de intervención.");
            }

            if (fecha == null || fecha.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la fecha de la intervención.");
            }

            if (motivo == null || motivo.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el motivo de la intervención.");
            }

            if (responsable == null || responsable.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el responsable de la intervención.");
            }

            if (estado == null || estado.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar el estado de la intervención.");
            }

            if ((idIntervencion == null || idIntervencion <= 0)
                    && (codigoEstudiante == null || codigoEstudiante.trim().isEmpty())) {
                throw new IllegalArgumentException("Debe seleccionar un estudiante.");
            }

            service.guardarIntervencion(
                    idIntervencion,
                    codigoEstudiante,
                    idTipo,
                    fecha,
                    motivo,
                    responsable,
                    estado,
                    derivacion,
                    observaciones
            );

            redirect.addFlashAttribute("ok", "Intervención guardada correctamente.");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar intervención: " + e.getMessage());

            if (idIntervencion == null || idIntervencion <= 0) {
                return "redirect:/sista/intervenciones/nuevo";
            }

            return "redirect:/sista/intervenciones/editar/" + idIntervencion;
        }

        return "redirect:/sista/intervenciones";
    }

    // ============================================================
    // SEGUIMIENTO DE INTERVENCIONES
    // URL: http://localhost:8087/sista/intervenciones/{id}/seguimiento
    // ============================================================
    @GetMapping("/intervenciones/{id}/seguimiento")
    public String seguimiento(@PathVariable Integer id,
                              @RequestParam(required = false) Integer idRegistro,
                              Model model,
                              RedirectAttributes redirect) {

        Map<String, Object> intervencion = service.buscarIntervencion(id);

        if (intervencion == null) {
            redirect.addFlashAttribute("error", "No se encontró la intervención solicitada.");
            return "redirect:/sista/intervenciones";
        }

        Map<String, Object> seguimiento;

        if (idRegistro == null || idRegistro <= 0) {
            seguimiento = new HashMap<>();
            seguimiento.put("fecha_seguimiento", LocalDate.now().toString());
        } else {
            seguimiento = service.buscarSeguimiento(idRegistro);

            if (seguimiento == null) {
                seguimiento = new HashMap<>();
                seguimiento.put("fecha_seguimiento", LocalDate.now().toString());
            }
        }

        model.addAttribute("intervencion", intervencion);
        model.addAttribute("seguimientos", service.listarSeguimientos(id));
        model.addAttribute("seguimiento", seguimiento);

        return "sista/seguimiento";
    }

    @PostMapping("/intervenciones/{id}/seguimiento/guardar")
    public String guardarSeguimiento(@PathVariable Integer id,
                                     @RequestParam(required = false) Integer idRegistro,
                                     @RequestParam(required = false) String fechaSeguimiento,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) String acuerdos,
                                     RedirectAttributes redirect) {

        try {
            if (fechaSeguimiento == null || fechaSeguimiento.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la fecha del seguimiento.");
            }

            if (descripcion == null || descripcion.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la descripción del seguimiento.");
            }

            service.guardarSeguimiento(
                    idRegistro,
                    id,
                    fechaSeguimiento,
                    descripcion,
                    acuerdos
            );

            redirect.addFlashAttribute("ok", "Seguimiento guardado correctamente.");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar seguimiento: " + e.getMessage());
        }

        return "redirect:/sista/intervenciones/" + id + "/seguimiento";
    }
}
