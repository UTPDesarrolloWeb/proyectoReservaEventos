package com.evently.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(unique = true, columnDefinition = "TEXT")
    private String codigoQR;

    @Column(nullable = false)
    private Integer cantidadEntradas = 1;

    @Column(nullable = false)
    private Double montoTotal;

    @PrePersist
    public void prePersist() {
        this.fechaReserva = LocalDateTime.now();
        this.montoTotal = this.evento.getPrecio() * this.cantidadEntradas;
    }
}
