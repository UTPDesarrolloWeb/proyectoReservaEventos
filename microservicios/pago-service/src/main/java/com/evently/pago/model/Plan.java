package com.evently.pago.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "planes")

public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TipoPlan nombre;

    @NotNull
    @Column(nullable = false)
    private Double precio;

    @NotNull
    @Column(nullable = false)
    private Integer limiteEventos;

    @NotNull
    @Column(nullable = false)
    private Double comisionPorcentaje;

    @Column(nullable = false)
    private Boolean activo = true;

    @NotBlank
    @Column(nullable = false)
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoPlan getNombre() { return nombre; }
    public void setNombre(TipoPlan nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getLimiteEventos() { return limiteEventos; }
    public void setLimiteEventos(Integer limiteEventos) { this.limiteEventos = limiteEventos; }
    public Double getComisionPorcentaje() { return comisionPorcentaje; }
    public void setComisionPorcentaje(Double comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
