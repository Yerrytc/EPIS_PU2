package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aulas")
public class Aula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private Long idAula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_escuela", nullable = false)
    private Escuela escuela;

    @Column(name = "codigo_aula", nullable = false, unique = true, length = 20)
    private String codigoAula;

    @Column(length = 150)
    private String descripcion;

    public Long getIdAula() { return idAula; }
    public void setIdAula(Long idAula) { this.idAula = idAula; }
    public Escuela getEscuela() { return escuela; }
    public void setEscuela(Escuela escuela) { this.escuela = escuela; }
    public String getCodigoAula() { return codigoAula; }
    public void setCodigoAula(String codigoAula) { this.codigoAula = codigoAula; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNombreMostrar() { return codigoAula + (descripcion != null && !descripcion.isBlank() ? " - " + descripcion : ""); }
}
