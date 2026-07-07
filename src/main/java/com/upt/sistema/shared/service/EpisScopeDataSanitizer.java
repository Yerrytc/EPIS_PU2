package com.upt.sistema.shared.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EpisScopeDataSanitizer {

    private static final Logger log = LoggerFactory.getLogger(EpisScopeDataSanitizer.class);

    private final JdbcTemplate jdbc;

    public EpisScopeDataSanitizer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void sanitize() {
        try {
            jdbc.update("UPDATE rsu_proyecto SET escuela_aliada = 'EPIS' WHERE escuela_aliada IS NOT NULL AND escuela_aliada <> 'EPIS'");
            jdbc.update("UPDATE rsu_actividad SET laboratorio_ambiente = REPLACE(laboratorio_ambiente, 'Patio FAING', 'Patio EPIS') WHERE laboratorio_ambiente LIKE '%FAING%'");
            jdbc.update("UPDATE ambientes_generales SET nombre_ambiente = REPLACE(nombre_ambiente, 'FAING', 'EPIS') WHERE nombre_ambiente LIKE '%FAING%'");
            jdbc.update("UPDATE ambientes_generales SET nombre_ambiente = 'Cancha EPIS' WHERE nombre_ambiente IN ('Cancha FACSA', 'Cancha FADE')");
            jdbc.update("UPDATE objetos SET descripcion = REPLACE(descripcion, 'FAING', 'EPIS') WHERE descripcion LIKE '%FAING%'");
            log.info("Datos visibles acotados a EPIS.");
        } catch (Exception e) {
            log.warn("No se pudo aplicar limpieza EPIS: {}", e.getMessage());
        }
    }
}
