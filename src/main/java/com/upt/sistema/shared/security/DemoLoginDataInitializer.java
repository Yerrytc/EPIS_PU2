package com.upt.sistema.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Asegura credenciales demo para ejecucion local.
 * Esto evita el error de login cuando la BD fue cargada parcialmente o cuando el hash fue editado manualmente.
 */
@Component
public class DemoLoginDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final boolean resetDemoPasswords;

    public DemoLoginDataInitializer(JdbcTemplate jdbcTemplate,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${app.local.reset-demo-passwords:true}") boolean resetDemoPasswords) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.resetDemoPasswords = resetDemoPasswords;
    }

    @Override
    public void run(String... args) {
        System.out.println("[EPIS LOCAL] reset-demo-passwords=" + resetDemoPasswords);
        if (!resetDemoPasswords || !tablaExiste("usuarios") || !tablaExiste("usuarios_login")) {
            System.out.println("[EPIS LOCAL] No se reiniciaron claves demo. Revise que existan las tablas usuarios y usuarios_login.");
            return;
        }

        String passwordHash = passwordEncoder.encode("123456");
        System.out.println("[EPIS LOCAL] Reiniciando usuarios demo con clave 123456...");
        asegurarUsuario(1L, "Ivan", "Quispe Huallpa", "ADMIN", null, "2023078707", "900000001", "iq2023078707@virtual.upt.pe", passwordHash);
        asegurarUsuario(2L, "Patricia", "Salas Gutierrez", "TUTOR", null, "45678892", "981222335", "tutor@virtual.upt.pe", passwordHash);
        asegurarUsuario(3L, "Ana", "Torres Mamani", "DOCENTE", null, "45678891", "981222334", "docente@virtual.upt.pe", passwordHash);
        asegurarUsuario(4L, "Sebastian", "Cortez Mendoza", "ALUMNO", "2023078888", "75677777", "999999999", "estudiante@virtual.upt.pe", passwordHash);
    }

    private boolean tablaExiste(String tabla) {
        try {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class,
                    tabla
            );
            return total != null && total > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private void asegurarUsuario(Long idUsuario,
                                 String nombre,
                                 String apellidos,
                                 String tipoUsuario,
                                 String codigoAlumno,
                                 String documento,
                                 String telefono,
                                 String email,
                                 String passwordHash) {
        jdbcTemplate.update("""
                INSERT INTO usuarios (id_usuario, nombre, apellidos, tipo_usuario, codigo_alumno, documento, telefono, email)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    nombre = VALUES(nombre),
                    apellidos = VALUES(apellidos),
                    tipo_usuario = VALUES(tipo_usuario),
                    codigo_alumno = VALUES(codigo_alumno),
                    documento = VALUES(documento),
                    telefono = VALUES(telefono),
                    email = VALUES(email)
                """, idUsuario, nombre, apellidos, tipoUsuario, codigoAlumno, documento, telefono, email);

        jdbcTemplate.update("""
                INSERT INTO usuarios_login (id_usuario, username, password_hash, activo)
                VALUES (?, ?, ?, 1)
                ON DUPLICATE KEY UPDATE
                    id_usuario = VALUES(id_usuario),
                    password_hash = VALUES(password_hash),
                    activo = 1
                """, idUsuario, email.toLowerCase().trim(), passwordHash);
        System.out.println("[EPIS LOCAL] Usuario listo: " + email.toLowerCase().trim());
    }
}
