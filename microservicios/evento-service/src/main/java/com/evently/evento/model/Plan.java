package com.evently.evento.model;

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
}
