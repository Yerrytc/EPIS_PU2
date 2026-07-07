package com.upt.sistema.modules.objetos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(name = "tipo_usuario", nullable = false, length = 30)
    private String tipoUsuario;

    @Column(name = "codigo_alumno", length = 20)
    private String codigoAlumno;

    @Column(length = 20)
    private String documento;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.EAGER)
    private UsuarioLogin login;

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public String getCodigoAlumno() { return codigoAlumno; }
    public void setCodigoAlumno(String codigoAlumno) { this.codigoAlumno = codigoAlumno; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UsuarioLogin getLogin() { return login; }
    public void setLogin(UsuarioLogin login) { this.login = login; }

    public String getNombreCompleto() { return (nombre == null ? "" : nombre) + " " + (apellidos == null ? "" : apellidos); }
    public String getRolEtiqueta() {
        if (tipoUsuario == null) return "";
        return switch (tipoUsuario) {
            case "ADMIN" -> "Administrador";
            case "DOCENTE" -> "Docente";
            case "TUTOR" -> "Tutor";
            case "ALUMNO" -> "Alumno";
            default -> tipoUsuario;
        };
    }
}
