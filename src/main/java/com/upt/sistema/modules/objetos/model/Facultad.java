package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facultades")
public class Facultad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facultad")
    private Long idFacultad;

    @Column(name = "nombre_facultad", nullable = false, unique = true, length = 100)
    private String nombreFacultad;

    @OneToMany(mappedBy = "facultad")
    private List<Escuela> escuelas = new ArrayList<>();

    public Long getIdFacultad() { return idFacultad; }
    public void setIdFacultad(Long idFacultad) { this.idFacultad = idFacultad; }
    public String getNombreFacultad() { return nombreFacultad; }
    public void setNombreFacultad(String nombreFacultad) { this.nombreFacultad = nombreFacultad; }
    public List<Escuela> getEscuelas() { return escuelas; }
    public void setEscuelas(List<Escuela> escuelas) { this.escuelas = escuelas; }
}
