package com.upt.sistema.modules.sista.controller;

import com.upt.sistema.modules.sista.entity.SistaRegistro;
import com.upt.sistema.modules.sista.service.SistaRegistroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sista/registros")
public class SistaRegistroController {

    private final SistaRegistroService service;

    public SistaRegistroController(SistaRegistroService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tituloModulo", "Registros SISTA");
        model.addAttribute("registros", service.listarTodos());
        return "sista/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tituloModulo", "Registros SISTA");
        model.addAttribute("registro", new SistaRegistro());
        model.addAttribute("modo", "nuevo");
        model.addAttribute("formAction", "/sista/registros/guardar");
        return "sista/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("tituloModulo", "Registros SISTA");
        model.addAttribute("registro", service.obtenerPorId(id));
        model.addAttribute("modo", "editar");
        model.addAttribute("formAction", "/sista/registros/guardar");
        return "sista/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute SistaRegistro registro, RedirectAttributes redirect) {
        try {
            service.guardar(registro);
            redirect.addFlashAttribute("ok", "Registro SISTA guardado correctamente.");
            return "redirect:/sista/registros";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            if (registro.getId() == null) return "redirect:/sista/registros/nuevo";
            return "redirect:/sista/registros/editar/" + registro.getId();
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("ok", "Registro desactivado correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/sista/registros";
    }
}
