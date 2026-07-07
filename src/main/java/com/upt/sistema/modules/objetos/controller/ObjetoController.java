package com.upt.sistema.modules.objetos.controller;

import com.upt.sistema.modules.objetos.model.Objeto;
import com.upt.sistema.modules.objetos.model.Usuario;
import java.util.List;
import com.upt.sistema.modules.objetos.service.CatalogoService;
import com.upt.sistema.modules.objetos.service.ObjetoService;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/objetos")
public class ObjetoController {
    private final ObjetoService objetoService;
    private final CatalogoService catalogoService;
    private final UsuarioService usuarioService;

    public ObjetoController(ObjetoService objetoService, CatalogoService catalogoService, UsuarioService usuarioService) {
        this.objetoService = objetoService;
        this.catalogoService = catalogoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(required = false) String estado,
                         Model model) {
        List<Objeto> objetos = objetoService.listar(buscar, estado);
        model.addAttribute("objetos", objetos);
        model.addAttribute("noEntregados", objetos.stream().filter(o -> "No entregado".equals(o.getEstado())).count());
        model.addAttribute("entregados", objetos.stream().filter(o -> "Entregado".equals(o.getEstado())).count());
        model.addAttribute("totalObjetos", objetos.size());
        model.addAttribute("buscar", buscar);
        model.addAttribute("estado", estado);
        return "objetos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        cargarCatalogos(model);
        model.addAttribute("objeto", objetoService.nuevoObjeto());
        model.addAttribute("modo", "nuevo");
        model.addAttribute("formAction", "/objetos/guardar");
        return "objetos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Objeto objeto,
                           @RequestParam String tipoUbicacion,
                            @RequestParam Long idUbicacion,
                            @RequestParam Long idClaseObjeto,
                            Authentication authentication,
                           RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioService.usuarioActual(authentication);
            objetoService.registrar(objeto, tipoUbicacion, idUbicacion, idClaseObjeto, usuario);
            redirect.addFlashAttribute("ok", "Objeto registrado correctamente.");
            return "redirect:/objetos";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/objetos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        cargarCatalogos(model);
        model.addAttribute("objeto", objetoService.buscar(id));
        model.addAttribute("modo", "editar");
        model.addAttribute("formAction", "/objetos/actualizar/" + id);
        return "objetos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute Objeto objeto,
                             @RequestParam String tipoUbicacion,
                               @RequestParam Long idUbicacion,
                               @RequestParam Long idClaseObjeto,
                               Authentication authentication,
                              RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioService.usuarioActual(authentication);
            boolean puedeCambiarEstado = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            objetoService.actualizar(id, objeto, tipoUbicacion, idUbicacion, idClaseObjeto, usuario, puedeCambiarEstado);
            redirect.addFlashAttribute("ok", "Objeto modificado correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/objetos";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("clases", catalogoService.listarClases());
        model.addAttribute("aulas", catalogoService.listarAulas());
        model.addAttribute("laboratorios", catalogoService.listarLaboratorios());
        model.addAttribute("ambientes", catalogoService.listarAmbientes());
    }
}
