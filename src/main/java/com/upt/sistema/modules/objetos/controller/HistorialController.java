package com.upt.sistema.modules.objetos.controller;

import com.upt.sistema.modules.objetos.service.HistorialService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/historial")
public class HistorialController {
    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("historial", historialService.listarTodo());
        return "historial/lista";
    }
}
