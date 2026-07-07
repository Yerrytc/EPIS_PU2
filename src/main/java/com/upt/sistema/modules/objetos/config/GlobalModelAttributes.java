package com.upt.sistema.modules.objetos.config;

import com.upt.sistema.modules.objetos.model.Usuario;
import com.upt.sistema.modules.objetos.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    private final UsuarioService usuarioService;

    public GlobalModelAttributes(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Authentication authentication) {
        return usuarioService.usuarioActual(authentication);
    }
}
