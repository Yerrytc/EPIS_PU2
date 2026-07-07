package com.upt.sistema.shared.interceptor;

import com.upt.sistema.core.config.ModuloRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ModuloInterceptor implements HandlerInterceptor {

    private final ModuloRegistry moduloRegistry;

    public ModuloInterceptor(ModuloRegistry moduloRegistry) {
        this.moduloRegistry = moduloRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("modulosSistema", moduloRegistry.listarModulos());
        return true;
    }
}
