package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "escuelas")
public class Escuela {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escuela")
    private Long idEscuela;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_facultad", nullable = false)
    private Facultad facultad;

    @Column(name = "nombre_escuela", nullable = false, length = 100)
    private String nombreEscuela;

    public Long getIdEscuela() { return idEscuela; }
    public void setIdEscuela(Long idEscuela) { this.idEscuela = idEscuela; }
    public Facultad getFacultad() { return facultad; }
    public void setFacultad(Facultad facultad) { this.facultad = facultad; }
    public String getNombreEscuela() { return nombreEscuela; }
    public void setNombreEscuela(String nombreEscuela) { this.nombreEscuela = nombreEscuela; }
}
