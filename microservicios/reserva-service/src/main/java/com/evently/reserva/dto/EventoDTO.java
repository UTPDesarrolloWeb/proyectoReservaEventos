package com.evently.reserva.dto;

public class EventoDTO {
    private Long id;
    private String titulo;
    private String estado;
    private int aforoDisponible;
    private double precio;
    private Long organizadorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getAforoDisponible() { return aforoDisponible; }
    public void setAforoDisponible(int aforoDisponible) { this.aforoDisponible = aforoDisponible; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public Long getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(Long organizadorId) { this.organizadorId = organizadorId; }
}
