package com.upt.sistema.modules.admin.controller;

import com.upt.sistema.shared.validation.AppValidation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/smtp")
public class AdminSmtpController {

    private final JdbcTemplate jdbc;

    public AdminSmtpController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private String getConfig(String clave, String defecto) {
        var rows = jdbc.queryForList("SELECT valor FROM sistema_configuracion WHERE clave = ?", String.class, clave);
        return rows.isEmpty() || rows.get(0) == null || rows.get(0).isBlank() ? defecto : rows.get(0);
    }

    private void guardarConfig(String clave, String valor) {
        jdbc.update("""
                INSERT INTO sistema_configuracion (clave, valor)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE valor = VALUES(valor), fecha_actualizacion = CURRENT_TIMESTAMP
                """, clave, valor == null ? "" : valor.trim());
    }

    @GetMapping
    public String smtpConfig(Model model) {
        model.addAttribute("smtpHost", getConfig("smtp_host", ""));
        model.addAttribute("smtpPort", getConfig("smtp_port", "587"));
        model.addAttribute("smtpUsername", getConfig("smtp_username", ""));
        model.addAttribute("smtpFrom", getConfig("smtp_from", ""));
        model.addAttribute("smtpTls", getConfig("smtp_tls", "true"));
        return "admin/smtp-config";
    }

    @PostMapping
    public String guardarSmtp(@RequestParam String smtpHost,
                              @RequestParam String smtpPort,
                              @RequestParam String smtpUsername,
                              @RequestParam(required = false) String smtpPassword,
                              @RequestParam String smtpFrom,
                              @RequestParam String smtpTls,
                              RedirectAttributes redirect) {
        try {
            String hostOk = AppValidation.requiredText("Servidor SMTP", smtpHost, 5, 120);
            Integer portOk;
            try {
                portOk = Integer.parseInt(smtpPort.trim());
            } catch (Exception ex) {
                throw new IllegalArgumentException("El puerto SMTP debe ser numerico.");
            }
            AppValidation.range("Puerto SMTP", portOk, 1, 65535);
            String usernameOk = AppValidation.requiredEmail("Usuario SMTP", smtpUsername);
            String fromOk = AppValidation.requiredEmail("Correo remitente", smtpFrom);
            String tlsOk = AppValidation.oneOf("TLS", smtpTls, "true", "false");
            String passOk = AppValidation.optionalText("Contrasena SMTP", smtpPassword, 255);

            guardarConfig("smtp_host", hostOk);
            guardarConfig("smtp_port", String.valueOf(portOk));
            guardarConfig("smtp_username", usernameOk);
            guardarConfig("smtp_from", fromOk);
            guardarConfig("smtp_tls", tlsOk);
            if (passOk != null) guardarConfig("smtp_password", passOk);
            redirect.addFlashAttribute("ok", "Configuracion SMTP guardada correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Error al guardar: " + ex.getMessage());
        }
        return "redirect:/admin/smtp";
    }
}
