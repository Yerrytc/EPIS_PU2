package com.upt.sistema.core.config;

import com.upt.sistema.shared.interceptor.ModuloInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ModuloInterceptor moduloInterceptor;

    public WebConfig(ModuloInterceptor moduloInterceptor) {
        this.moduloInterceptor = moduloInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduloInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/login");
    }

}
