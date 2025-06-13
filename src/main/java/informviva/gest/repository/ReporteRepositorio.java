package informviva.gest.repository;

import informviva.gest.dto.*;
import informviva.gest.model.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@PersistenceContext
public interface ReporteRepositorio extends JpaRepository<Venta, Long> {


    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    BigDecimal sumarTotalVentasEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    Long contarVentasEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(vd.cantidad) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    Long sumarCantidadArticulosVendidosEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaRegistro BETWEEN :startDate AND :endDate")
    Long contarClientesNuevosEntreFechas(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT new informviva.gest.dto.VentaPorPeriodoDTO(FUNCTION('DATE', v.fecha), SUM(v.total)) " +
            "FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE', v.fecha)")
    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new informviva.gest.dto.VentaPorCategoriaDTO(cat.nombre, SUM(vd.total)) " +
            "FROM VentaDetalle vd JOIN vd.producto p JOIN p.categoria cat JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate GROUP BY cat.nombre")
    List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new informviva.gest.dto.VentaPorVendedorDTO(u.username, SUM(v.total)) " +
            "FROM Venta v JOIN v.usuario u WHERE v.fecha BETWEEN :startDate AND :endDate GROUP BY u.username")
    List<VentaPorVendedorDTO> obtenerVentasPorVendedorEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(v.total) FROM Venta v")
    BigDecimal sumarTotalVentas();

    @Query("SELECT (SUM(v.total) - SUM(vd.total)) FROM Venta v JOIN v.detalles vd")
    BigDecimal calcularVariacionVentas();

    @Query("SELECT AVG(v.total) FROM Venta v")
    BigDecimal calcularTicketPromedio();

    @Query("SELECT COUNT(p) FROM Producto p")
    Long contarTotalProductos();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    Long contarProductosActivos();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock < p.stockMinimo")
    Long contarProductosBajoStock();

    @Query("SELECT COUNT(c) FROM Cliente c")
    Long contarTotalClientes();

    @Query("SELECT new informviva.gest.dto.ProductoVendidoDTO(p.nombre, SUM(vd.cantidad), SUM(vd.total), SUM(vd.total)) " +
            "FROM VentaDetalle vd JOIN vd.producto p JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY p.nombre ORDER BY SUM(vd.cantidad) DESC")
    List<ProductoVendidoDTO> obtenerProductosMasVendidosEntreFechas(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    // Métodos existentes (mantener para compatibilidad)
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    BigDecimal sumarTotalVentasEntreFechas(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    Long contarVentasEntreFechas(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(vd.cantidad), 0L) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    Long sumarCantidadArticulosVendidosEntreFechas(LocalDate startDate, LocalDate endDate);


    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Suma total de ventas con rango de fecha y hora específica
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :startDateTime AND :endDateTime AND v.estado != 'ANULADA'")
    BigDecimal sumarTotalVentasEntreFechasConHora(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Cuenta ventas con rango de fecha y hora específica
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :startDateTime AND :endDateTime AND v.estado != 'ANULADA'")
    Long contarVentasEntreFechasConHora(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Suma cantidad de artículos vendidos con hora específica
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0L) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :startDateTime AND :endDateTime AND v.estado != 'ANULADA'")
    Long sumarCantidadArticulosVendidosConHora(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Obtiene ventas por período con granularidad especificada
     */
    @Query(value = "SELECT " +
            "CASE " +
            "    WHEN :granularidad = 'HORA' THEN DATE_FORMAT(v.fecha, '%Y-%m-%d %H:00:00') " +
            "    WHEN :granularidad = 'DIA' THEN DATE_FORMAT(v.fecha, '%Y-%m-%d') " +
            "    WHEN :granularidad = 'MES' THEN DATE_FORMAT(v.fecha, '%Y-%m') " +
            "    ELSE DATE_FORMAT(v.fecha, '%Y-%m-%d') " +
            "END AS periodo, " +
            "SUM(v.total) AS total " +
            "FROM ventas v " +
            "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA' " +
            "GROUP BY periodo " +
            "ORDER BY periodo ASC", nativeQuery = true)
    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoConGranularidad(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("granularidad") String granularidad
    );

    /**
     * Obtiene análisis de tendencias por hora
     */
    @Query(value = "SELECT " +
            "HOUR(v.fecha) as hora, " +
            "COUNT(v.id) as transacciones, " +
            "SUM(v.total) as total, " +
            "AVG(v.total) as promedio " +
            "FROM ventas v " +
            "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA' " +
            "GROUP BY HOUR(v.fecha) " +
            "ORDER BY hora", nativeQuery = true)
    List<Object[]> analizarTendenciasPorHora(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT new informviva.gest.dto.ProductoVendidoDTO(p.nombre, SUM(vd.cantidad), SUM(vd.total), 0) " +
            "FROM VentaDetalle vd JOIN vd.producto p JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY p.nombre " +
            "ORDER BY SUM(vd.cantidad) DESC")
    List<ProductoVendidoDTO> obtenerProductosMasVendidosEntreFechas(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT DATE_FORMAT(v.fecha, '%Y-%m-%d') AS periodo, SUM(v.total) AS total " +
            "FROM ventas v WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE_FORMAT(v.fecha, '%Y-%m-%d') " +
            "ORDER BY DATE_FORMAT(v.fecha, '%Y-%m-%d') ASC", nativeQuery = true)
    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new informviva.gest.dto.VentaPorCategoriaDTO(CAST(cat.nombre AS string), COALESCE(SUM(vd.total), 0.0)) " +
            "FROM VentaDetalle vd JOIN vd.producto p JOIN p.categoria cat JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY cat.nombre " +
            "ORDER BY cat.nombre ASC")
    List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new informviva.gest.dto.VentaPorVendedorDTO(CAST(u.username AS string), COALESCE(SUM(v.total), 0.0)) " +
            "FROM Venta v JOIN v.vendedor u " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY u.username " +
            "ORDER BY u.username ASC")
    List<VentaPorVendedorDTO> obtenerVentasPorVendedorEntreFechas(LocalDate startDate, LocalDate endDate);

    List<Object[]> obtenerVentasPorFranjaHoraria(LocalDateTime fecha);

}