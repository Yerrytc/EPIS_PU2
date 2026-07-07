package com.upt.sistema.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModuloRegistry {

    private final List<ModuloInfo> modulos = new ArrayList<>();

    public ModuloRegistry() {
        modulos.add(new ModuloInfo("SISTA", "SISTA", "Sistema de Seguimiento y Asesoria para estudiantes, docentes, apoderados e intervenciones.", "/sista", true));
        modulos.add(new ModuloInfo("OBJ", "Objetos Perdidos", "Registro, entrega e historial de objetos encontrados.", "/objetos", true));
        modulos.add(new ModuloInfo("TUT", "Tutoria", "Gestion de solicitudes y acompanamiento tutorial.", "/tutoria", true));
        modulos.add(new ModuloInfo("RSU", "RSU", "Proyectos sociales, voluntariado, beneficiarios, evidencias y horas de servicio.", "/rsu", true));
        modulos.add(new ModuloInfo("ADM", "Administracion EPIS", "Gestion independiente de usuarios, roles y accesos institucionales.", "/admin/usuarios", true));
    }

    public List<ModuloInfo> listarModulos() {
        return Collections.unmodifiableList(modulos);
    }
}
