package com.upt.sistema.modules.objetos.controller;

import com.upt.sistema.modules.objetos.model.EntregaObjeto;
import com.upt.sistema.modules.objetos.model.Usuario;
import com.upt.sistema.modules.objetos.service.EntregaService;
import com.upt.sistema.modules.objetos.service.ObjetoService;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/entregas")
public class EntregaController {
    private final EntregaService entregaService;
    private final ObjetoService objetoService;
    private final UsuarioService usuarioService;

    public EntregaController(EntregaService entregaService, ObjetoService objetoService, UsuarioService usuarioService) {
        this.entregaService = entregaService;
        this.objetoService = objetoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("entregas", entregaService.listar());
            model.addAttribute("objetosPendientes", objetoService.listarNoEntregados());
        } catch (Exception ex) {
            model.addAttribute("entregas", java.util.List.of());
            model.addAttribute("objetosPendientes", java.util.List.of());
            model.addAttribute("error", "No se pudieron cargar las entregas. Intente nuevamente.");
        }
        return "entregas/lista";
    }

    @GetMapping("/nuevo/{idObjeto}")
    public String nuevo(@PathVariable Long idObjeto, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("objeto", objetoService.buscar(idObjeto));
            model.addAttribute("entrega", new EntregaObjeto());
            return "entregas/formulario";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Objeto no encontrado.");
            return "redirect:/entregas";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long idObjeto,
                          @ModelAttribute EntregaObjeto entrega,
                          Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioService.usuarioActual(authentication);
            objetoService.entregar(idObjeto, entrega, usuario);
            redirect.addFlashAttribute("ok", "Entrega registrada correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/entregas";
    }
}
