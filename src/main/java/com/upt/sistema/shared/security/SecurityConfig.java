package com.upt.sistema.shared.security;

import com.upt.sistema.modules.objetos.model.UsuarioLogin;
import com.upt.sistema.modules.objetos.repository.UsuarioLoginRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LocalCaptchaValidationFilter localCaptchaValidationFilter) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sista", "/sista/**", "/rsu", "/rsu/**")
                        .hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers("/tutoria", "/tutoria/**").hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers("/objetos/nuevo", "/objetos/guardar")
                        .hasAnyRole("ADMIN", "DOCENTE", "TUTOR", "ALUMNO")
                        .requestMatchers("/objetos/editar/**", "/objetos/actualizar/**", "/entregas/**", "/historial/**")
                        .hasAnyRole("ADMIN", "DOCENTE", "TUTOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler((request, response, exception) -> {
                            System.out.println("[LOGIN EPIS] FALLO: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
                            response.sendRedirect(request.getContextPath() + "/login?error=true");
                        })
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
                .addFilterBefore(localCaptchaValidationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioLoginRepository usuarioLoginRepository) {
        return username -> {
            String usernameNormalizado = username == null ? "" : username.trim().toLowerCase();
            if (!usernameNormalizado.endsWith("@virtual.upt.pe")) {
                throw new UsernameNotFoundException("Debe ingresar con su correo institucional @virtual.upt.pe");
            }

            UsuarioLogin login = usuarioLoginRepository.findByUsernameIgnoreCase(usernameNormalizado)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usernameNormalizado));

            String rol = login.getUsuario().getTipoUsuario().trim().toUpperCase();
            String password = login.getPasswordHash() == null ? "" : login.getPasswordHash().trim();

            System.out.println("[LOGIN EPIS] Usuario encontrado: " + login.getUsername() + " | Rol: " + rol + " | Activo: " + login.getActivo());

            return User.withUsername(login.getUsername().trim().toLowerCase())
                    .password(password)
                    .roles(rol)
                    .accountLocked(!Boolean.TRUE.equals(login.getActivo()))
                    .disabled(!Boolean.TRUE.equals(login.getActivo()))
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }
                String stored = encodedPassword.trim();

                // Para pruebas locales: permite texto plano si la BD fue editada manualmente en HeidiSQL.
                if (stored.contentEquals(rawPassword)) {
                    return true;
                }

                // BCrypt generado por PHP/HeidiSQL puede venir como $2y$; Spring trabaja mejor con $2a$/$2b$.
                if (stored.startsWith("$2y$")) {
                    stored = "$2a$" + stored.substring(4);
                }

                if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
                    try {
                        return bcrypt.matches(rawPassword, stored);
                    } catch (Exception ex) {
                        return false;
                    }
                }

                return false;
            }
        };
    }
}
