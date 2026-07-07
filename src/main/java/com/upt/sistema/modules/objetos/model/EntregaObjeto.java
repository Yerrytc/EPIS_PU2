package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrega_objeto")
public class EntregaObjeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_objeto", nullable = false)
    private Objeto objeto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_entrego", nullable = false)
    private Usuario usuarioEntrego;

    @Column(name = "tipo_receptor", nullable = false, length = 30)
    private String tipoReceptor;

    @Column(name = "nombre_receptor", nullable = false, length = 100)
    private String nombreReceptor;

    @Column(name = "apellidos_receptor", nullable = false, length = 100)
    private String apellidosReceptor;

    @Column(name = "documento_receptor", length = 20)
    private String documentoReceptor;

    @Column(name = "codigo_estudiante", length = 20)
    private String codigoEstudiante;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @PrePersist
    public void prePersist() {
        if (fechaEntrega == null) fechaEntrega = LocalDateTime.now();
    }

    public Long getIdEntrega() { return idEntrega; }
    public void setIdEntrega(Long idEntrega) { this.idEntrega = idEntrega; }
    public Objeto getObjeto() { return objeto; }
    public void setObjeto(Objeto objeto) { this.objeto = objeto; }
    public Usuario getUsuarioEntrego() { return usuarioEntrego; }
    public void setUsuarioEntrego(Usuario usuarioEntrego) { this.usuarioEntrego = usuarioEntrego; }
    public String getTipoReceptor() { return tipoReceptor; }
    public void setTipoReceptor(String tipoReceptor) { this.tipoReceptor = tipoReceptor; }
    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }
    public String getApellidosReceptor() { return apellidosReceptor; }
    public void setApellidosReceptor(String apellidosReceptor) { this.apellidosReceptor = apellidosReceptor; }
    public String getDocumentoReceptor() { return documentoReceptor; }
    public void setDocumentoReceptor(String documentoReceptor) { this.documentoReceptor = documentoReceptor; }
    public String getCodigoEstudiante() { return codigoEstudiante; }
    public void setCodigoEstudiante(String codigoEstudiante) { this.codigoEstudiante = codigoEstudiante; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}
