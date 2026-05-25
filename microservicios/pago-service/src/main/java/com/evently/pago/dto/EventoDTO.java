package com.evently.pago.dto;

public class EventoDTO {
    private Long id;
    private Long organizadorId;

    private String titulo;
    private java.time.LocalDateTime fechaEvento;
    private String lugar;

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
}
