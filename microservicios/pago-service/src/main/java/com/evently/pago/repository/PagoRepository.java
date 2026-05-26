package com.evently.pago.repository;

import com.evently.pago.model.EstadoPago;
import com.evently.pago.model.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);

    List<Pago> findByEstado(EstadoPago estado);

    @Query("SELECT SUM(p.comisionPlataforma) FROM Pago p WHERE p.estado = 'COMPLETADO'")
    Double sumComisionesTotal();

    // Suma de ingresos por suscripciones - En la migracin, si los planes cambian, esto se ajusta
    // Por simplicidad en microservicios, asumo que tenemos una forma de guardarlo,
    // O delegamos esto a otra tabla si es necesario. Por ahora comento para que compile.
    // @Query("SELECT SUM(pl.precio) FROM Organizador o JOIN o.plan pl")
    // Double sumIngresosSuscripciones();

    // Pagos del cliente, necesita los ids de reservas
    List<Pago> findByReservaIdIn(List<Long> reservaIds);
    Page<Pago> findByReservaIdIn(List<Long> reservaIds, Pageable pageable);

    Page<Pago> findAll(Pageable pageable);

    @Query("SELECT p FROM Pago p WHERE p.estado = 'COMPLETADO' " +
            "AND p.fechaPago >= :inicio AND p.fechaPago <= :fin")
    List<Pago> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
