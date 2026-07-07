package com.upt.sistema.core.config;

public class ModuloInfo {
    private String codigo;
    private String nombre;
    private String descripcion;
    private String rutaBase;
    private boolean activo;

    public ModuloInfo() {
    }

    public ModuloInfo(String codigo, String nombre, String descripcion, String rutaBase, boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.rutaBase = rutaBase;
        this.activo = activo;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRutaBase() { return rutaBase; }
    public void setRutaBase(String rutaBase) { this.rutaBase = rutaBase; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
