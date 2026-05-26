package com.evently.pago.model;

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

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

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
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getTransaccionId() { return transaccionId; }
    public void setTransaccionId(String transaccionId) { this.transaccionId = transaccionId; }
    public Double getComisionPlataforma() { return comisionPlataforma; }
    public void setComisionPlataforma(Double comisionPlataforma) { this.comisionPlataforma = comisionPlataforma; }
    public Double getMontoOrganizador() { return montoOrganizador; }
    public void setMontoOrganizador(Double montoOrganizador) { this.montoOrganizador = montoOrganizador; }
}
