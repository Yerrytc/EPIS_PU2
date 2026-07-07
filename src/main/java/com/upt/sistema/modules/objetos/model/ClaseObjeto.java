package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clase_objeto")
public class ClaseObjeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clase_objeto")
    private Long idClaseObjeto;

    @Column(name = "nombre_clase", nullable = false, unique = true, length = 50)
    private String nombreClase;

    public Long getIdClaseObjeto() { return idClaseObjeto; }
    public void setIdClaseObjeto(Long idClaseObjeto) { this.idClaseObjeto = idClaseObjeto; }
    public String getNombreClase() { return nombreClase; }
    public void setNombreClase(String nombreClase) { this.nombreClase = nombreClase; }
}
