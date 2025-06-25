package informviva.gest.repository;

import informviva.gest.model.Venta;
import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la gestión de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    // ========== MÉTODOS REQUERIDOS POR ClienteServicioImpl ==========

    /**
     * Busca ventas por ID del cliente
     * 🔥 MÉTODO REQUERIDO para corregir error 381 y 496
     */
    List<Venta> findByClienteId(Long clienteId);

    /**
     * Busca ventas por cliente (método alternativo)
     */
    List<Venta> findByCliente(Cliente cliente);

    // ========== OTROS MÉTODOS ÚTILES QUE PROBABLEMENTE NECESITES ==========

    /**
     * Busca ventas por vendedor
     */
    List<Venta> findByVendedor(Usuario vendedor);

    /**
     * Busca ventas por vendedor ID
     */
    List<Venta> findByVendedorId(Long vendedorId);

    /**
     * Busca ventas por rango de fechas
     */
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta ventas por rango de fechas
     */
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca ventas por estado
     */
    List<Venta> findByEstado(String estado);

    /**
     * Busca ventas por método de pago
     */
    List<Venta> findByMetodoPago(String metodoPago);

    /**
     * Cuenta ventas de un cliente
     */
    long countByClienteId(Long clienteId);


    /**
     * Encuentra las ventas más recientes de un cliente
     */
    List<Venta> findTop10ByClienteIdOrderByFechaDesc(Long clienteId);

    /**
     * Busca ventas por rango de fechas con paginación
     * CRÍTICO: Requerido por VentaServicioImpl línea ~89
     */
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    Page<Venta> findByFechaBetween(@Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin,
                                   Pageable pageable);

    /**
     * Busca ventas por vendedor ID y rango de fechas
     * CRÍTICO: Requerido por VentaServicioImpl
     */
    @Query("SELECT v FROM Venta v WHERE v.vendedor.id = :vendedorId " +
            "AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findByVendedorIdAndFechaBetween(@Param("vendedorId") Long vendedorId,
                                                @Param("inicio") LocalDateTime inicio,
                                                @Param("fin") LocalDateTime fin);

    /**
     * Suma total de ventas por cliente
     * CRÍTICO: Requerido por VentaServicioImpl línea ~367
     */
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v WHERE v.cliente.id = :clienteId AND v.estado != 'ANULADA'")
    Double sumTotalByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Suma total de ventas por rango de fechas
     * CRÍTICO: Requerido por VentaServicioImpl línea ~379
     */
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v " +
            "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA'")
    Double sumTotalByFechaBetween(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    /**
     * Verifica si existen ventas para un cliente
     * CRÍTICO: Requerido por VentaServicioImpl
     */
    boolean existsByClienteId(Long clienteId);

    /**
     * Busca ventas por múltiples estados
     */
    @Query("SELECT v FROM Venta v WHERE v.estado IN :estados")
    List<Venta> findByEstadoIn(@Param("estados") List<String> estados);

    /**
     * Obtiene ventas ordenadas por fecha descendente
     */
    List<Venta> findAllByOrderByFechaDesc();

    /**
     * Busca ventas por total mayor a un valor
     */
    List<Venta> findByTotalGreaterThan(Double total);

    /**
     * Busca ventas por total menor a un valor
     */
    List<Venta> findByTotalLessThan(Double total);

    /**
     * Busca ventas por rango de totales
     */
    @Query("SELECT v FROM Venta v WHERE v.total BETWEEN :min AND :max")
    List<Venta> findByTotalBetween(@Param("min") Double min, @Param("max") Double max);

    /**
     * Obtiene estadísticas básicas de ventas
     */
    @Query("SELECT COUNT(v) as totalVentas, " +
            "COALESCE(SUM(v.total), 0) as montoTotal, " +
            "COALESCE(AVG(v.total), 0) as promedioVenta " +
            "FROM Venta v WHERE v.estado != 'ANULADA'")
    Map<String, Object> obtenerEstadisticasBasicas();

    /**
     * Obtiene estadísticas por período
     */
    @Query("SELECT COUNT(v) as totalVentas, " +
            "COALESCE(SUM(v.total), 0) as montoTotal, " +
            "COALESCE(AVG(v.total), 0) as promedioVenta, " +
            "COALESCE(MIN(v.total), 0) as ventaMinima, " +
            "COALESCE(MAX(v.total), 0) as ventaMaxima " +
            "FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA'")
    Map<String, Object> obtenerEstadisticasPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin);

    /**
     * Cuenta ventas por método de pago
     */
    @Query("SELECT v.metodoPago, COUNT(v) FROM Venta v " +
            "WHERE v.metodoPago IS NOT NULL " +
            "GROUP BY v.metodoPago")
    List<Object[]> contarPorMetodoPago();

    /**
     * Obtiene ventas con cliente y vendedor (para evitar N+1)
     */
    @Query("SELECT v FROM Venta v " +
            "LEFT JOIN FETCH v.cliente " +
            "LEFT JOIN FETCH v.vendedor " +
            "ORDER BY v.fecha DESC")
    List<Venta> findAllWithClienteAndVendedor();

    /**
     * Busca ventas con observaciones que contengan texto
     */
    @Query("SELECT v FROM Venta v WHERE v.observaciones IS NOT NULL " +
            "AND LOWER(v.observaciones) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Venta> findByObservacionesContaining(@Param("texto") String texto);

    /**
     * Obtiene ventas sin vendedor asignado
     */
    @Query("SELECT v FROM Venta v WHERE v.vendedor IS NULL")
    List<Venta> findVentasSinVendedor();

    /**
     * Obtiene el total de ingresos (excluyendo anuladas)
     */
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v WHERE v.estado != 'ANULADA'")
    Double obtenerTotalIngresos();

    /**
     * Obtiene ventas recientes de un cliente (limitadas)
     */
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId " +
            "ORDER BY v.fecha DESC")
    List<Venta> findRecentByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    /**
     * Cuenta ventas del día actual
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fecha) = CURRENT_DATE")
    Long countVentasHoy();

    /**
     * Suma ventas del día actual
     */
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v " +
            "WHERE DATE(v.fecha) = CURRENT_DATE AND v.estado != 'ANULADA'")
    Double sumVentasHoy();

    /**
     * Obtiene ventas por cliente ordenadas por fecha
     */
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId " +
            "ORDER BY v.fecha DESC")
    List<Venta> findByClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId);

    /**
     * Encuentra las ventas más recientes de un producto
     */
    @Query("SELECT v FROM Venta v JOIN v.detalles d WHERE d.producto.id = :productoId ORDER BY v.fecha DESC")
    List<Venta> findVentasRecentesByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT v FROM Venta v WHERE v.vendedor = :vendedor AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findByVendedorAndFechaBetween(
            @Param("vendedor") Usuario vendedor,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}