package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "laboratorios")
public class Laboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_laboratorio")
    private Long idLaboratorio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_escuela", nullable = false)
    private Escuela escuela;

    @Column(name = "codigo_laboratorio", nullable = false, unique = true, length = 20)
    private String codigoLaboratorio;

    @Column(length = 150)
    private String descripcion;

    public Long getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(Long idLaboratorio) { this.idLaboratorio = idLaboratorio; }
    public Escuela getEscuela() { return escuela; }
    public void setEscuela(Escuela escuela) { this.escuela = escuela; }
    public String getCodigoLaboratorio() { return codigoLaboratorio; }
    public void setCodigoLaboratorio(String codigoLaboratorio) { this.codigoLaboratorio = codigoLaboratorio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNombreMostrar() { return codigoLaboratorio + (descripcion != null && !descripcion.isBlank() ? " - " + descripcion : ""); }
}
