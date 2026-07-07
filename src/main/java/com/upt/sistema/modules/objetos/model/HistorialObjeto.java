package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_objeto")
public class HistorialObjeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_objeto", nullable = false)
    private Objeto objeto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String accion;

    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }
    public Objeto getObjeto() { return objeto; }
    public void setObjeto(Objeto objeto) { this.objeto = objeto; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
}
