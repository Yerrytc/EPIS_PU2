package com.upt.sistema.modules.tutoria.controller;

import com.upt.sistema.core.annotation.Modulo;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import com.upt.sistema.modules.tutoria.service.TutoriaService;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tutoria")
@Modulo(codigo = "TUTORIA", nombre = "Tutoría", rutaBase = "/tutoria")
public class TutoriaController {

    private final TutoriaService service;
    private final UsuarioService usuarioService;

    public TutoriaController(TutoriaService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        cargarModelo(model, authentication);
        return "tutoria/index";
    }

    @PostMapping("/seguimiento")
    public String registrarSeguimiento(@RequestParam String codigoEstudiante, @RequestParam String motivo,
                                       @RequestParam String tipoSeguimiento, @RequestParam Integer unidadAcademica,
                                       @RequestParam String estado, @RequestParam(required = false) String observacionPositiva,
                                       @RequestParam(required = false) String advertenciaAcademica, RedirectAttributes redirect) {
        return ejecutar(redirect, "Estudiante registrado para seguimiento.", () ->
                service.registrarSeguimiento(codigoEstudiante, motivo, tipoSeguimiento, unidadAcademica, estado, observacionPositiva, advertenciaAcademica));
    }

    @PostMapping("/citacion")
    public String registrarCitacion(@RequestParam String codigoEstudiante, @RequestParam String fechaCitacion,
                                    @RequestParam String motivo, @RequestParam String estado, RedirectAttributes redirect) {
        return ejecutar(redirect, "Citacion registrada.", () -> service.registrarCitacion(codigoEstudiante, fechaCitacion, motivo, estado));
    }

    @PostMapping("/entrevista")
    public String registrarEntrevista(@RequestParam String codigoEstudiante, @RequestParam String fecha,
                                      @RequestParam String problemasDetectados, @RequestParam String observaciones,
                                      @RequestParam String recomendaciones, @RequestParam(required = false) String derivacionArea,
                                      RedirectAttributes redirect) {
        return ejecutar(redirect, "Entrevista y ficha registradas.", () ->
                service.registrarEntrevista(codigoEstudiante, fecha, problemasDetectados, observaciones, recomendaciones, derivacionArea));
    }

    @PostMapping("/inasistencia")
    public String registrarInasistencia(@RequestParam String codigoEstudiante, @RequestParam Integer idCurso,
                                        @RequestParam Integer idDocente, @RequestParam String fecha,
                                        @RequestParam(required = false) String motivo, RedirectAttributes redirect) {
        return ejecutar(redirect, "Inasistencia registrada.", () -> service.registrarInasistencia(codigoEstudiante, idCurso, idDocente, fecha, motivo));
    }

    @PostMapping("/justificacion")
    public String registrarJustificacion(@RequestParam String codigoEstudiante, @RequestParam Integer idCurso,
                                          @RequestParam Integer idDocente, @RequestParam String fechaReincorporacion,
                                          @RequestParam String fechaPresentacion, @RequestParam Integer diasFaltados,
                                          @RequestParam String motivo, @RequestParam(required = false) String evidencia,
                                          Authentication authentication, RedirectAttributes redirect) {
        try {
            var usuario = usuarioService.usuarioActual(authentication);
            if (usuario != null && "ALUMNO".equalsIgnoreCase(usuario.getTipoUsuario())) {
                Map<String, Object> alumno = service.estudiantePorLogin(authentication.getName());
                if (alumno == null) {
                    redirect.addFlashAttribute("error", "No se encontro un estudiante asociado a su cuenta.");
                    return "redirect:/tutoria";
                }
                codigoEstudiante = String.valueOf(alumno.get("codigo"));
            }
            service.registrarJustificacion(codigoEstudiante, idCurso, idDocente, fechaReincorporacion, fechaPresentacion, diasFaltados, motivo, evidencia);
            redirect.addFlashAttribute("ok", "Justificacion registrada y plazo validado.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tutoria";
    }

    @PostMapping("/justificacion/revisar")
    public String revisarJustificacion(@RequestParam Long idJustificacion,
                                       @RequestParam String estadoRevision,
                                       @RequestParam(required = false) String observacionRevision,
                                       RedirectAttributes redirect) {
        return ejecutar(redirect, "Justificacion revisada correctamente.", () -> service.revisarJustificacion(idJustificacion, estadoRevision, observacionRevision));
    }

    @PostMapping("/charla")
    public String registrarCharla(@RequestParam String titulo, @RequestParam String tema,
                                  @RequestParam String ciclosDirigidos, @RequestParam String fecha,
                                  @RequestParam String responsable, RedirectAttributes redirect) {
        return ejecutar(redirect, "Charla de induccion/tutoria registrada.", () -> service.registrarCharla(titulo, tema, ciclosDirigidos, fecha, responsable));
    }

    @PostMapping("/comunicado")
    public String registrarComunicado(@RequestParam String titulo, @RequestParam String mensaje,
                                      @RequestParam String fecha, @RequestParam String destinatarios,
                                      RedirectAttributes redirect) {
        return ejecutar(redirect, "Comunicado registrado.", () -> service.registrarComunicado(titulo, mensaje, fecha, destinatarios));
    }

    private String ejecutar(RedirectAttributes redirect, String mensajeOk, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute("ok", mensajeOk);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tutoria";
    }

    private void cargarModelo(Model model, Authentication authentication) {
        var usuario = usuarioService.usuarioActual(authentication);
        boolean alumno = usuario != null && "ALUMNO".equalsIgnoreCase(usuario.getTipoUsuario());
        Map<String, Object> alumnoActual = alumno ? service.estudiantePorLogin(authentication.getName()) : null;

        model.addAttribute("resumen", service.resumen());
        model.addAttribute("estudiantes", service.estudiantes());
        model.addAttribute("cursos", service.cursos());
        model.addAttribute("docentes", service.docentes());
        model.addAttribute("seguimientos", service.estudiantesSeguimiento());
        model.addAttribute("riesgos", service.estudiantesRiesgo());
        model.addAttribute("notas", service.notas());
        model.addAttribute("citaciones", service.citaciones());
        model.addAttribute("entrevistas", service.entrevistas());
        model.addAttribute("alertas", service.inasistenciasFrecuentes());
        model.addAttribute("justificaciones", service.justificaciones());
        model.addAttribute("charlas", service.charlas());
        model.addAttribute("comunicados", service.comunicados());
        model.addAttribute("esAlumno", alumno);
        model.addAttribute("alumnoActual", alumnoActual);
        model.addAttribute("misJustificaciones", alumnoActual == null ? java.util.List.of() : service.justificacionesPorEstudiante(String.valueOf(alumnoActual.get("codigo"))));
    }

    @GetMapping("/consulta-notas")
    public String consultarNotas(@RequestParam(required = false) String codigoEstudiante, Model model, Authentication authentication) {
        cargarModelo(model, authentication);
        model.addAttribute("codigoConsulta", codigoEstudiante);
        model.addAttribute("notasConsulta", service.notasPorEstudiante(codigoEstudiante));
        model.addAttribute("historialNotas", service.historialNotas(codigoEstudiante));
        return "tutoria/index";
    }
}
