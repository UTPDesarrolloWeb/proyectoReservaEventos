package com.evently.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaEvento;

    @NotBlank
    @Column(nullable = false)
    private String lugar;

    @NotNull
    @Column(nullable = false)
    private Integer aforo;

    @Column(nullable = false)
    private Integer aforoDisponible;

    @NotNull
    @Column(nullable = false)
    private Double precio;

    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.aforoDisponible = this.aforo;
    }
}
