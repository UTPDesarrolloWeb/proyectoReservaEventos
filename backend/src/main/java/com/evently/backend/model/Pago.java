package com.evently.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    // ID que devuelve Stripe/PayPal para rastrear la transacción
    @Column(unique = true)
    private String transaccionId;

    // Cuánto se quedó la plataforma
    @Column(nullable = false)
    private Double comisionPlataforma;

    // Cuánto recibe el organizador
    @Column(nullable = false)
    private Double montoOrganizador;

    @PrePersist
    public void prePersist() {
        this.fechaPago = LocalDateTime.now();

        // Calcula comisión automáticamente
        Double comisionPorcentaje = this.reserva.getEvento()
                .getOrganizador().getPlan().getComisionPorcentaje();

        this.comisionPlataforma = this.monto * (comisionPorcentaje / 100);
        this.montoOrganizador = this.monto - this.comisionPlataforma;
    }
}
