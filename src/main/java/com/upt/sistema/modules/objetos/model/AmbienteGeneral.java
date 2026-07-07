package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambientes_generales")
public class AmbienteGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ambiente")
    private Long idAmbiente;

    @Column(name = "nombre_ambiente", nullable = false, length = 100)
    private String nombreAmbiente;

    @Column(name = "tipo_ambiente", nullable = false, length = 50)
    private String tipoAmbiente;

    public Long getIdAmbiente() { return idAmbiente; }
    public void setIdAmbiente(Long idAmbiente) { this.idAmbiente = idAmbiente; }
    public String getNombreAmbiente() { return nombreAmbiente; }
    public void setNombreAmbiente(String nombreAmbiente) { this.nombreAmbiente = nombreAmbiente; }
    public String getTipoAmbiente() { return tipoAmbiente; }
    public void setTipoAmbiente(String tipoAmbiente) { this.tipoAmbiente = tipoAmbiente; }

    public String getNombreMostrar() { return tipoAmbiente + " - " + nombreAmbiente; }
}
