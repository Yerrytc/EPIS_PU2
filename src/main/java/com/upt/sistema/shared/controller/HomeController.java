package com.upt.sistema.shared.controller;

import com.upt.sistema.core.config.ModuloRegistry;
import com.upt.sistema.shared.security.LocalCaptchaService;
import com.upt.sistema.shared.service.DashboardService;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ModuloRegistry moduloRegistry;
    private final DashboardService dashboardService;
    private final LocalCaptchaService localCaptchaService;
    private final UsuarioService usuarioService;

    public HomeController(ModuloRegistry moduloRegistry, DashboardService dashboardService, LocalCaptchaService localCaptchaService, UsuarioService usuarioService) {
        this.moduloRegistry = moduloRegistry;
        this.dashboardService = dashboardService;
        this.localCaptchaService = localCaptchaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        LocalCaptchaService.CaptchaChallenge captcha = localCaptchaService.generate(session);
        model.addAttribute("captchaId", captcha.id());
        model.addAttribute("captchaQuestion", captcha.question());
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        boolean alumno = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ALUMNO".equals(a.getAuthority()));
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean docente = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCENTE".equals(a.getAuthority()));
        boolean tutor = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TUTOR".equals(a.getAuthority()));

        model.addAttribute("modulos", moduloRegistry.listarModulos().stream()
                .filter(m -> admin
                        || (tutor && !"/admin/usuarios".equals(m.getRutaBase()))
                        || (docente && "/objetos".equals(m.getRutaBase()))
                        || (alumno && "/objetos".equals(m.getRutaBase())))
                .toList());
        if (alumno) {
            model.addAttribute("estudianteActual", usuarioService.usuarioActual(authentication));
        } else {
            model.addAttribute("metricas", dashboardService.obtenerMetricasResumen());
            model.addAttribute("stats", dashboardService.obtenerEstadisticas());
            model.addAttribute("actividadOperativa", dashboardService.obtenerActividadOperativa());
            model.addAttribute("ultimosMovimientos", dashboardService.obtenerUltimosMovimientos());
            model.addAttribute("correoAlertas", dashboardService.obtenerCorreoAlertas());
        }
        return "dashboard";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }
}
