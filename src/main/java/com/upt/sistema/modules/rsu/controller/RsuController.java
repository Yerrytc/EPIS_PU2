package com.upt.sistema.modules.rsu.controller;

import com.upt.sistema.core.annotation.Modulo;
import com.upt.sistema.modules.rsu.service.RsuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rsu")
@Modulo(codigo = "RSU", nombre = "Responsabilidad Social Universitaria", rutaBase = "/rsu")
public class RsuController {

    private final RsuService service;

    public RsuController(RsuService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Model model) {
        cargarModelo(model);
        return "rsu/index";
    }

    @PostMapping("/proyecto")
    public String registrarProyecto(@RequestParam String nombre, @RequestParam String tipoProyecto,
                                    @RequestParam String objetivo, @RequestParam String problemaSocial,
                                    @RequestParam String ods, @RequestParam String responsable,
                                    @RequestParam(required = false) String escuelaAliada,
                                    @RequestParam String fechaInicio, @RequestParam String fechaFin,
                                    RedirectAttributes redirect) {
        return ejecutar(redirect, "Proyecto RSU registrado y enviado a aprobacion.", () ->
                service.registrarProyecto(nombre, tipoProyecto, objetivo, problemaSocial, ods, responsable, escuelaAliada, fechaInicio, fechaFin));
    }

    @PostMapping("/proyecto/aprobar")
    public String aprobarProyecto(@RequestParam Long idProyecto, RedirectAttributes redirect) {
        return ejecutar(redirect, "Proyecto aprobado.", () -> service.aprobarProyecto(idProyecto));
    }

    @PostMapping("/proyecto/clausurar")
    public String clausurarProyecto(@RequestParam Long idProyecto, @RequestParam String fechaClausura,
                                    @RequestParam String resultados, RedirectAttributes redirect) {
        return ejecutar(redirect, "Clausura registrada.", () -> service.clausurarProyecto(idProyecto, fechaClausura, resultados));
    }

    @PostMapping("/actividad")
    public String registrarActividad(@RequestParam Long idProyecto, @RequestParam String fecha,
                                     @RequestParam String hora, @RequestParam String tema,
                                     @RequestParam String descripcion, @RequestParam Double duracionHoras,
                                     @RequestParam String laboratorioAmbiente, RedirectAttributes redirect) {
        return ejecutar(redirect, "Actividad planificada.", () ->
                service.registrarActividad(idProyecto, fecha, hora, tema, descripcion, duracionHoras, laboratorioAmbiente));
    }

    @PostMapping("/participante")
    public String registrarParticipante(@RequestParam Long idProyecto, @RequestParam String codigoEstudiante,
                                        @RequestParam String responsabilidad, @RequestParam Double horasCumplidas,
                                        RedirectAttributes redirect) {
        return ejecutar(redirect, "Participante registrado.", () ->
                service.registrarParticipante(idProyecto, codigoEstudiante, responsabilidad, horasCumplidas));
    }

    @PostMapping("/beneficiario")
    public String registrarBeneficiario(@RequestParam Long idProyecto, @RequestParam String nombres,
                                        @RequestParam String tipoBeneficiario, @RequestParam(required = false) String documento,
                                        @RequestParam(required = false) String tallerInscrito, RedirectAttributes redirect) {
        return ejecutar(redirect, "Beneficiario o inscripcion registrada.", () ->
                service.registrarBeneficiario(idProyecto, nombres, tipoBeneficiario, documento, tallerInscrito));
    }

    @PostMapping("/asistencia")
    public String registrarAsistencia(@RequestParam Long idActividad, @RequestParam String tipoAsistente,
                                      @RequestParam String identificador, @RequestParam(defaultValue = "true") Boolean asistio,
                                      @RequestParam Double horasReconocidas, RedirectAttributes redirect) {
        return ejecutar(redirect, "Asistencia registrada.", () ->
                service.registrarAsistencia(idActividad, tipoAsistente, identificador, asistio, horasReconocidas));
    }

    @PostMapping("/evidencia")
    public String registrarEvidencia(@RequestParam Long idProyecto, @RequestParam String tipoEvidencia,
                                     @RequestParam String descripcion,
                                     RedirectAttributes redirect) {
        return ejecutar(redirect, "Evidencia registrada.", () -> service.registrarEvidencia(idProyecto, tipoEvidencia, descripcion));
    }

    @PostMapping("/concurso")
    public String registrarConcurso(@RequestParam Long idProyecto, @RequestParam String nombre,
                                    @RequestParam String criterios, @RequestParam String fecha,
                                    @RequestParam String ganadores, RedirectAttributes redirect) {
        return ejecutar(redirect, "Concurso o evaluacion registrada.", () -> service.registrarConcurso(idProyecto, nombre, criterios, fecha, ganadores));
    }

    private String ejecutar(RedirectAttributes redirect, String mensajeOk, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute("ok", mensajeOk);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/rsu";
    }

    private void cargarModelo(Model model) {
        model.addAttribute("resumen", service.resumen());
        model.addAttribute("proyectos", service.proyectos());
        model.addAttribute("actividades", service.actividades());
        model.addAttribute("participantes", service.participantes());
        model.addAttribute("beneficiarios", service.beneficiarios());
        model.addAttribute("asistencias", service.asistencias());
        model.addAttribute("evidencias", service.evidencias());
        model.addAttribute("concursos", service.concursos());
        model.addAttribute("estudiantes", service.estudiantes());
    }
}
