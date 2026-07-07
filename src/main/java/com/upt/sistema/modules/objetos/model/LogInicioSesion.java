package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_inicio_sesion")
public class LogInicioSesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "ip_acceso", length = 50)
    private String ipAcceso;

    @PrePersist
    public void prePersist() {
        if (fechaIngreso == null) fechaIngreso = LocalDateTime.now();
    }

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getIpAcceso() { return ipAcceso; }
    public void setIpAcceso(String ipAcceso) { this.ipAcceso = ipAcceso; }
}
