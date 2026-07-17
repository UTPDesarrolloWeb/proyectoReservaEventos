package com.evently.pago.dto;

public class EventoDTO {
    private Long id;
    private Long organizadorId;
    private String titulo;
    private java.time.LocalDateTime fechaEvento;
    private String lugar;
    private Double precio;
    private String estado;
    private Integer aforoDisponible;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(Long organizadorId) { this.organizadorId = organizadorId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public java.time.LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(java.time.LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getAforoDisponible() { return aforoDisponible; }
    public void setAforoDisponible(Integer aforoDisponible) { this.aforoDisponible = aforoDisponible; }
}

