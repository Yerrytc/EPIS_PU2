package com.upt.sistema.shared.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime fecha;
    private String mensaje;
    private int estado;

    public ErrorResponse() {
    }

    public ErrorResponse(LocalDateTime fecha, String mensaje, int estado) {
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.estado = estado;
    }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }
}
