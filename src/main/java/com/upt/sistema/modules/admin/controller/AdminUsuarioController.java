package com.upt.sistema.modules.admin.controller;

import com.upt.sistema.modules.objetos.model.Usuario;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {
    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "admin/usuarios-lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("modo", "nuevo");
        model.addAttribute("formAction", "/admin/usuarios/guardar");
        return "admin/usuarios-formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario,
                          @RequestParam(required = false) String username,
                          @RequestParam String password,
                          RedirectAttributes redirect) {
        try {
            usuarioService.guardarUsuarioConLogin(usuario, username, password);
            redirect.addFlashAttribute("ok", "Usuario registrado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("usuario", usuario);
            return "redirect:/admin/usuarios/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuarioService.buscarUsuario(id).orElseThrow());
        }
        model.addAttribute("modo", "editar");
        model.addAttribute("formAction", "/admin/usuarios/actualizar/" + id);
        return "admin/usuarios-formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute Usuario usuario,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) String password,
                             RedirectAttributes redirect) {
        try {
            usuarioService.actualizarUsuario(id, usuario, username, password);
            redirect.addFlashAttribute("ok", "Usuario actualizado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("usuario", usuario);
            return "redirect:/admin/usuarios/editar/" + id;
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            usuarioService.eliminarUsuario(id);
            redirect.addFlashAttribute("ok", "Usuario eliminado correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo eliminar: " + ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/estado/{idLogin}")
    public String cambiarEstado(@PathVariable Long idLogin, RedirectAttributes redirect) {
        usuarioService.cambiarEstadoLogin(idLogin);
        redirect.addFlashAttribute("ok", "Estado de acceso actualizado.");
        return "redirect:/admin/usuarios";
    }
}
