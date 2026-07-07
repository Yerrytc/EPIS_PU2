package com.upt.sistema.shared.dto;

public class DashboardStatsDTO {
    private long totalSista;
    private long totalObjetos;
    private long totalTutorias;
    private long totalRsu;
    private long totalIntervenciones;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long totalSista, long totalObjetos, long totalTutorias, long totalRsu, long totalIntervenciones) {
        this.totalSista = totalSista;
        this.totalObjetos = totalObjetos;
        this.totalTutorias = totalTutorias;
        this.totalRsu = totalRsu;
        this.totalIntervenciones = totalIntervenciones;
    }

    public long getTotalSista() { return totalSista; }
    public void setTotalSista(long totalSista) { this.totalSista = totalSista; }
    public long getTotalObjetos() { return totalObjetos; }
    public void setTotalObjetos(long totalObjetos) { this.totalObjetos = totalObjetos; }
    public long getTotalTutorias() { return totalTutorias; }
    public void setTotalTutorias(long totalTutorias) { this.totalTutorias = totalTutorias; }
    public long getTotalRsu() { return totalRsu; }
    public void setTotalRsu(long totalRsu) { this.totalRsu = totalRsu; }
    public long getTotalIntervenciones() { return totalIntervenciones; }
    public void setTotalIntervenciones(long totalIntervenciones) { this.totalIntervenciones = totalIntervenciones; }
}
