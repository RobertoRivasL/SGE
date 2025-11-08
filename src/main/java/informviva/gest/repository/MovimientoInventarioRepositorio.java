package informviva.gest.repository;

import informviva.gest.model.MovimientoInventario;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import informviva.gest.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad MovimientoInventario
 * Proporciona operaciones de acceso a datos para movimientos de inventario
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para acceso a datos de movimientos
 * - I: Interface segregada específica para movimientos
 * - D: Abstracción para el acceso a datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Repository
public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Busca todos los movimientos de un producto específico
     *
     * @param producto Producto a buscar
     * @param pageable Información de paginación
     * @return Página de movimientos del producto
     */
    Page<MovimientoInventario> findByProducto(Producto producto, Pageable pageable);

    /**
     * Busca movimientos por ID de producto
     *
     * @param productoId ID del producto
     * @param pageable Información de paginación
     * @return Página de movimientos
     */
    Page<MovimientoInventario> findByProductoId(Long productoId, Pageable pageable);

    /**
     * Busca movimientos por tipo
     *
     * @param tipo Tipo de movimiento
     * @param pageable Información de paginación
     * @return Página de movimientos del tipo especificado
     */
    Page<MovimientoInventario> findByTipo(TipoMovimiento tipo, Pageable pageable);

    /**
     * Busca movimientos en un rango de fechas
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @param pageable Información de paginación
     * @return Página de movimientos en el rango
     */
    Page<MovimientoInventario> findByFechaBetween(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Busca movimientos por producto y rango de fechas
     *
     * @param productoId ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de movimientos
     */
    Page<MovimientoInventario> findByProductoIdAndFechaBetween(
            Long productoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Busca movimientos por tipo y rango de fechas
     *
     * @param tipo Tipo de movimiento
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de movimientos
     */
    Page<MovimientoInventario> findByTipoAndFechaBetween(
            TipoMovimiento tipo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Busca movimientos por usuario
     *
     * @param usuarioId ID del usuario que registró el movimiento
     * @param pageable Información de paginación
     * @return Página de movimientos del usuario
     */
    Page<MovimientoInventario> findByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Busca movimientos por referencia externa
     *
     * @param referenciaExterna Referencia externa (ID de venta, compra, etc.)
     * @return Lista de movimientos con esa referencia
     */
    List<MovimientoInventario> findByReferenciaExterna(String referenciaExterna);

    /**
     * Cuenta movimientos de un producto
     *
     * @param productoId ID del producto
     * @return Número de movimientos del producto
     */
    long countByProductoId(Long productoId);

    /**
     * Cuenta movimientos por tipo
     *
     * @param tipo Tipo de movimiento
     * @return Número de movimientos del tipo
     */
    long countByTipo(TipoMovimiento tipo);

    /**
     * Cuenta movimientos en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Número de movimientos en el rango
     */
    long countByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene los últimos N movimientos de un producto
     *
     * @param productoId ID del producto
     * @param pageable Información de paginación (con límite)
     * @return Lista de últimos movimientos
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.producto.id = :productoId ORDER BY m.fecha DESC")
    List<MovimientoInventario> findUltimosMovimientosPorProducto(
            @Param("productoId") Long productoId,
            Pageable pageable);

    /**
     * Obtiene el total de unidades entradas de un producto
     *
     * @param productoId ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de unidades entradas
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto.id = :productoId " +
           "AND m.tipo IN ('COMPRA', 'DEVOLUCION_ENTRADA', 'AJUSTE_POSITIVO', 'TRANSFERENCIA_ENTRADA', 'INVENTARIO_INICIAL') " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long calcularTotalEntradas(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene el total de unidades salidas de un producto
     *
     * @param productoId ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de unidades salidas
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto.id = :productoId " +
           "AND m.tipo IN ('VENTA', 'DEVOLUCION_SALIDA', 'AJUSTE_NEGATIVO', 'TRANSFERENCIA_SALIDA') " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long calcularTotalSalidas(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene los productos con más movimientos
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Lista de productos con más movimientos
     */
    @Query("SELECT m.producto, COUNT(m) as total FROM MovimientoInventario m " +
           "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY m.producto " +
           "ORDER BY total DESC")
    List<Object[]> findProductosConMasMovimientos(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Calcula el costo total de entradas en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Costo total de entradas
     */
    @Query("SELECT COALESCE(SUM(m.costoTotal), 0) FROM MovimientoInventario m " +
           "WHERE m.tipo IN ('COMPRA', 'DEVOLUCION_ENTRADA', 'AJUSTE_POSITIVO', 'TRANSFERENCIA_ENTRADA', 'INVENTARIO_INICIAL') " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double calcularCostoTotalEntradas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Calcula el costo total de salidas en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Costo total de salidas
     */
    @Query("SELECT COALESCE(SUM(m.costoTotal), 0) FROM MovimientoInventario m " +
           "WHERE m.tipo IN ('VENTA', 'DEVOLUCION_SALIDA', 'AJUSTE_NEGATIVO', 'TRANSFERENCIA_SALIDA') " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double calcularCostoTotalSalidas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca movimientos con criterios múltiples
     *
     * @param productoId ID del producto (puede ser null)
     * @param tipo Tipo de movimiento (puede ser null)
     * @param fechaInicio Fecha inicial (puede ser null)
     * @param fechaFin Fecha final (puede ser null)
     * @param usuarioId ID del usuario (puede ser null)
     * @param pageable Información de paginación
     * @return Página de movimientos que cumplen los criterios
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE " +
           "(:productoId IS NULL OR m.producto.id = :productoId) AND " +
           "(:tipo IS NULL OR m.tipo = :tipo) AND " +
           "(:fechaInicio IS NULL OR m.fecha >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR m.fecha <= :fechaFin) AND " +
           "(:usuarioId IS NULL OR m.usuario.id = :usuarioId) " +
           "ORDER BY m.fecha DESC")
    Page<MovimientoInventario> buscarConCriterios(
            @Param("productoId") Long productoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    /**
     * Obtiene estadísticas de movimientos por tipo
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de arrays con [tipo, cantidad_movimientos, total_unidades]
     */
    @Query("SELECT m.tipo, COUNT(m), SUM(m.cantidad) FROM MovimientoInventario m " +
           "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY m.tipo")
    List<Object[]> obtenerEstadisticasPorTipo(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}
