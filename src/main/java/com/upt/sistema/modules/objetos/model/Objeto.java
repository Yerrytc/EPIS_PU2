package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "objetos")
public class Objeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_objeto")
    private Long idObjeto;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre_objeto", nullable = false, length = 100)
    private String nombreObjeto;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aula")
    private Aula aula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_laboratorio")
    private Laboratorio laboratorio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ambiente_general")
    private AmbienteGeneral ambienteGeneral;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_encontrado", nullable = false)
    private LocalDate fechaEncontrado;

    @Column(name = "persona_encontro", nullable = false, length = 120)
    private String personaEncontro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_clase_objeto", nullable = false)
    private ClaseObjeto claseObjeto;

    @Column(nullable = false, length = 20)
    private String estado = "No entregado";

    public Long getIdObjeto() { return idObjeto; }
    public void setIdObjeto(Long idObjeto) { this.idObjeto = idObjeto; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreObjeto() { return nombreObjeto; }
    public void setNombreObjeto(String nombreObjeto) { this.nombreObjeto = nombreObjeto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }
    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }
    public AmbienteGeneral getAmbienteGeneral() { return ambienteGeneral; }
    public void setAmbienteGeneral(AmbienteGeneral ambienteGeneral) { this.ambienteGeneral = ambienteGeneral; }
    public LocalDate getFechaEncontrado() { return fechaEncontrado; }
    public void setFechaEncontrado(LocalDate fechaEncontrado) { this.fechaEncontrado = fechaEncontrado; }
    public String getPersonaEncontro() { return personaEncontro; }
    public void setPersonaEncontro(String personaEncontro) { this.personaEncontro = personaEncontro; }
    public ClaseObjeto getClaseObjeto() { return claseObjeto; }
    public void setClaseObjeto(ClaseObjeto claseObjeto) { this.claseObjeto = claseObjeto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUbicacionTexto() {
        if (aula != null) return "Aula: " + aula.getCodigoAula();
        if (laboratorio != null) return "Laboratorio: " + laboratorio.getCodigoLaboratorio();
        if (ambienteGeneral != null) return ambienteGeneral.getTipoAmbiente() + ": " + ambienteGeneral.getNombreAmbiente();
        return "Sin ubicacion";
    }

    public boolean isEntregado() { return "Entregado".equalsIgnoreCase(estado); }
}
