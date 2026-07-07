package com.upt.sistema.shared.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocalDatabaseVerifier implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public LocalDatabaseVerifier(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            String bd = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            Integer totalLogin = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios_login", Integer.class);
            Integer totalUsuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
            System.out.println("[EPIS BD] Base conectada: " + bd);
            System.out.println("[EPIS BD] usuarios=" + totalUsuarios + " | usuarios_login=" + totalLogin);
            jdbcTemplate.query("SELECT username, activo FROM usuarios_login ORDER BY id_login", rs -> {
                System.out.println("[EPIS BD] login cargado: " + rs.getString("username") + " | activo=" + rs.getString("activo"));
            });
        } catch (Exception ex) {
            System.out.println("[EPIS BD] ERROR verificando BD local: " + ex.getMessage());
        }
    }
}
