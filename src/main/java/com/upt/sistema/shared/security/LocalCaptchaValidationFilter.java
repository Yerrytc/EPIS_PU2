package com.upt.sistema.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LocalCaptchaValidationFilter extends OncePerRequestFilter {

    private final LocalCaptchaService localCaptchaService;

    public LocalCaptchaValidationFilter(LocalCaptchaService localCaptchaService) {
        this.localCaptchaService = localCaptchaService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod()) || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!localCaptchaService.validate(request)) {
            response.sendRedirect(request.getContextPath() + "/login?captcha=true");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
