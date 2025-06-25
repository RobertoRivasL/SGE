package informviva.gest.repository;

import informviva.gest.model.VentaDetalle;
import informviva.gest.model.Venta;
import informviva.gest.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la gestión de detalles de venta
 * CRÍTICO: Requerido por VentaServicioImpl
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Repository
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {

    // ===== MÉTODOS CRÍTICOS REQUERIDOS POR VentaServicioImpl =====

    /**
     * Busca detalles por ID de venta
     * CRÍTICO: Requerido por VentaServicioImpl
     */
    List<VentaDetalle> findByVentaId(Long ventaId);

    /**
     * Busca detalles por venta
     */
    List<VentaDetalle> findByVenta(Venta venta);

    /**
     * Busca detalles por producto
     */
    List<VentaDetalle> findByProducto(Producto producto);

    /**
     * Busca detalles por ID de producto
     */
    List<VentaDetalle> findByProductoId(Long productoId);

    /**
     * Verifica si existen detalles para un producto
     * CRÍTICO: Requerido por VentaServicioImpl línea ~430
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Suma cantidad de productos vendidos en un período
     * CRÍTICO: Requerido por VentaServicioImpl línea ~444
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd " +
            "WHERE vd.venta.fecha BETWEEN :inicio AND :fin")
    Long sumCantidadByFechaBetween(@Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);

    /**
     * Cuenta detalles por producto
     */
    long countByProductoId(Long productoId);

    /**
     * Suma cantidad vendida por producto
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd WHERE vd.producto.id = :productoId")
    Long sumCantidadByProductoId(@Param("productoId") Long productoId);

    /**
     * Calcula ingresos por producto
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad * vd.precioUnitario), 0.0) FROM VentaDetalle vd " +
            "WHERE vd.producto.id = :productoId")
    Double sumIngresosByProductoId(@Param("productoId") Long productoId);

    /**
     * Busca detalles por rango de fechas
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :inicio AND :fin")
    List<VentaDetalle> findByVentaFechaBetween(@Param("inicio") LocalDateTime inicio,
                                               @Param("fin") LocalDateTime fin);

    /**
     * Obtiene productos más vendidos
     */
    @Query("SELECT vd.producto.id, SUM(vd.cantidad) as totalVendido " +
            "FROM VentaDetalle vd " +
            "GROUP BY vd.producto.id " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidos();

    /**
     * Obtiene productos más vendidos con límite
     */
    @Query("SELECT vd.producto.id, vd.producto.nombre, SUM(vd.cantidad) as totalVendido " +
            "FROM VentaDetalle vd " +
            "GROUP BY vd.producto.id, vd.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findTopProductosMasVendidos(Pageable pageable);

    /**
     * Calcula total de ingresos por producto en un período
     */
    @Query("SELECT vd.producto.id, vd.producto.nombre, " +
            "SUM(vd.cantidad * vd.precioUnitario) as totalIngresos " +
            "FROM VentaDetalle vd " +
            "WHERE vd.venta.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY vd.producto.id, vd.producto.nombre " +
            "ORDER BY totalIngresos DESC")
    List<Object[]> findIngresosPorProductoEnPeriodo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin);

    /**
     * Busca detalles con precio unitario mayor a un valor
     */
    List<VentaDetalle> findByPrecioUnitarioGreaterThan(Double precio);

    /**
     * Busca detalles con cantidad mayor a un valor
     */
    List<VentaDetalle> findByCantidadGreaterThan(Integer cantidad);

    /**
     * Obtiene el total de artículos vendidos
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd")
    Long countTotalArticulosVendidos();

    /**
     * Obtiene el promedio de precio unitario
     */
    @Query("SELECT COALESCE(AVG(vd.precioUnitario), 0.0) FROM VentaDetalle vd")
    Double findPromedioPrecioUnitario();

    /**
     * Busca detalles sin producto (datos huérfanos)
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.producto IS NULL")
    List<VentaDetalle> findDetallesSinProducto();

    /**
     * Busca detalles sin venta (datos huérfanos)
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.venta IS NULL")
    List<VentaDetalle> findDetallesSinVenta();

    /**
     * Obtiene estadísticas de detalles por venta
     */
    @Query("SELECT vd.venta.id, COUNT(vd), SUM(vd.cantidad) " +
            "FROM VentaDetalle vd " +
            "GROUP BY vd.venta.id")
    List<Object[]> findEstadisticasPorVenta();

    /**
     * Busca detalles por múltiples productos
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.producto.id IN :productosIds")
    List<VentaDetalle> findByProductoIdIn(@Param("productosIds") List<Long> productosIds);

    /**
     * Busca detalles por múltiples ventas
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.venta.id IN :ventasIds")
    List<VentaDetalle> findByVentaIdIn(@Param("ventasIds") List<Long> ventasIds);

    /**
     * Obtiene el detalle con mayor cantidad de un producto
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.producto.id = :productoId " +
            "ORDER BY vd.cantidad DESC")
    List<VentaDetalle> findTopByProductoIdOrderByCantidadDesc(@Param("productoId") Long productoId);

    /**
     * Obtiene el detalle con mayor valor de un producto
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.producto.id = :productoId " +
            "ORDER BY (vd.cantidad * vd.precioUnitario) DESC")
    List<VentaDetalle> findTopByProductoIdOrderByValorDesc(@Param("productoId") Long productoId);

    /**
     * Cuenta detalles por rango de fechas
     */
    @Query("SELECT COUNT(vd) FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :inicio AND :fin")
    Long countByVentaFechaBetween(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    /**
     * Elimina detalles por venta (para operaciones de limpieza)
     */
    void deleteByVentaId(Long ventaId);

    /**
     * Elimina detalles por producto (para operaciones de limpieza)
     */
    void deleteByProductoId(Long productoId);
}