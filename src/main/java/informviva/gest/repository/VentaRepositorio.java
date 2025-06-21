package informviva.gest.repository;

import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    /**
     * Buscar ventas por rango de fechas
     */
    List<Venta> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Buscar ventas por rango de fechas con paginación
     */
    Page<Venta> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    /**
     * Buscar ventas por cliente
     */
    List<Venta> findByClienteId(Long clienteId);

    /**
     * Buscar ventas por vendedor y fechas
     */
    List<Venta> findByVendedorIdAndFechaBetween(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Contar ventas por rango de fechas
     */
    Long countByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Contar ventas por cliente
     */
    Long countByClienteId(Long clienteId);

    /**
     * Verificar si existen ventas por cliente
     */
    boolean existsByClienteId(Long clienteId);

    /**
     * Sumar total de ventas por cliente
     */
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.cliente.id = :clienteId")
    Double sumTotalByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Sumar total de ventas por rango de fechas
     */
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double sumTotalByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                  @Param("fechaFin") LocalDateTime fechaFin);
}