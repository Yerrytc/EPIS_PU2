package com.upt.sistema.modules.admin.controller;

import com.upt.sistema.modules.admin.service.AdminCatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/catalogos")
public class AdminCatalogoController {
    private final AdminCatalogoService adminCatalogoService;

    public AdminCatalogoController(AdminCatalogoService adminCatalogoService) {
        this.adminCatalogoService = adminCatalogoService;
    }

    @GetMapping
    public String catalogos(Model model) {
        model.addAttribute("escuelas", adminCatalogoService.listarEscuelas());
        model.addAttribute("aulas", adminCatalogoService.listarAulas());
        model.addAttribute("laboratorios", adminCatalogoService.listarLaboratorios());
        model.addAttribute("ambientes", adminCatalogoService.listarAmbientes());
        model.addAttribute("clases", adminCatalogoService.listarClases());
        return "admin/catalogos";
    }

    @PostMapping("/clase")
    public String guardarClase(@RequestParam String nombreClase, RedirectAttributes redirect) {
        try {
            adminCatalogoService.guardarClase(nombreClase);
            redirect.addFlashAttribute("ok", "Clase de catalogo registrada.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/aula")
    public String guardarAula(@RequestParam Long idEscuela,
                              @RequestParam String codigoAula,
                              @RequestParam(required = false) String descripcion,
                              RedirectAttributes redirect) {
        try {
            adminCatalogoService.guardarAula(idEscuela, codigoAula, descripcion);
            redirect.addFlashAttribute("ok", "Zona de aula registrada.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/laboratorio")
    public String guardarLaboratorio(@RequestParam Long idEscuela,
                                     @RequestParam String codigoLaboratorio,
                                     @RequestParam(required = false) String descripcion,
                                     RedirectAttributes redirect) {
        try {
            adminCatalogoService.guardarLaboratorio(idEscuela, codigoLaboratorio, descripcion);
            redirect.addFlashAttribute("ok", "Zona de laboratorio registrada.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/catalogos";
    }

    @PostMapping("/ambiente")
    public String guardarAmbiente(@RequestParam String nombreAmbiente,
                                  @RequestParam String tipoAmbiente,
                                  RedirectAttributes redirect) {
        try {
            adminCatalogoService.guardarAmbiente(nombreAmbiente, tipoAmbiente);
            redirect.addFlashAttribute("ok", "Zona institucional registrada.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/catalogos";
    }
}
