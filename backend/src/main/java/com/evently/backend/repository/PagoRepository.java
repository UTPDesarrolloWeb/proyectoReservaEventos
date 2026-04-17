package com.evently.backend.repository;

import com.evently.backend.model.Pago;
import com.evently.backend.model.Reserva;
import com.evently.backend.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Busca el pago por la reserva
    Optional<Pago> findByReserva(Reserva reserva);

    // Pagos completados de un organizador para estadísticas
    List<Pago> findByEstado(EstadoPago estado);

    // Suma los ingresos totales por comisiones
    @Query("SELECT SUM(p.comisionPlataforma) FROM Pago p WHERE p.estado = 'COMPLETADO'")
    Double sumComisionesTotal();

    // Suma los ingresos por suscripciones
    @Query("SELECT SUM(pl.precio) FROM Organizador o JOIN o.plan pl")
    Double sumIngresosSuscripciones();
}
