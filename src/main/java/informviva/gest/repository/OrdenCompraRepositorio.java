package informviva.gest.repository;

import informviva.gest.model.OrdenCompra;
import informviva.gest.model.OrdenCompra.EstadoOrden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad OrdenCompra siguiendo el patrón Repository
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para acceso a datos de órdenes de compra
 * - I: Interface segregada específica para órdenes
 * - D: Abstracción para el acceso a datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Repository
public interface OrdenCompraRepositorio extends JpaRepository<OrdenCompra, Long> {

    // ===== BÚSQUEDAS POR PROVEEDOR =====

    /**
     * Busca órdenes de compra de un proveedor específico
     *
     * @param proveedorId ID del proveedor
     * @return Lista de órdenes del proveedor
     */
    List<OrdenCompra> findByProveedorId(Long proveedorId);

    /**
     * Busca órdenes de compra de un proveedor con paginación
     *
     * @param proveedorId ID del proveedor
     * @param pageable Información de paginación
     * @return Página de órdenes del proveedor
     */
    Page<OrdenCompra> findByProveedorId(Long proveedorId, Pageable pageable);

    // ===== BÚSQUEDAS POR ESTADO =====

    /**
     * Busca órdenes por estado
     *
     * @param estado Estado de la orden
     * @return Lista de órdenes en ese estado
     */
    List<OrdenCompra> findByEstado(EstadoOrden estado);

    /**
     * Busca órdenes por estado con paginación
     *
     * @param estado Estado de la orden
     * @param pageable Información de paginación
     * @return Página de órdenes en ese estado
     */
    Page<OrdenCompra> findByEstado(EstadoOrden estado, Pageable pageable);

    /**
     * Busca órdenes por estado y proveedor
     *
     * @param estado Estado de la orden
     * @param proveedorId ID del proveedor
     * @param pageable Información de paginación
     * @return Página de órdenes que cumplen
     */
    Page<OrdenCompra> findByEstadoAndProveedorId(EstadoOrden estado, Long proveedorId, Pageable pageable);

    // ===== BÚSQUEDAS POR FECHA =====

    /**
     * Busca órdenes entre dos fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de órdenes en el rango
     */
    List<OrdenCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca órdenes entre dos fechas con paginación
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de órdenes en el rango
     */
    Page<OrdenCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    /**
     * Busca órdenes por fecha de entrega estimada entre dos fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de órdenes con entrega en el rango
     */
    Page<OrdenCompra> findByFechaEntregaEstimadaBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    // ===== BÚSQUEDAS POR NÚMERO DE ORDEN =====

    /**
     * Busca una orden por su número único
     *
     * @param numeroOrden Número de la orden
     * @return Optional con la orden si existe
     */
    Optional<OrdenCompra> findByNumeroOrden(String numeroOrden);

    /**
     * Verifica si existe una orden con el número dado
     *
     * @param numeroOrden Número de la orden
     * @return true si existe
     */
    boolean existsByNumeroOrden(String numeroOrden);

    // ===== BÚSQUEDAS POR USUARIO =====

    /**
     * Busca órdenes creadas por un usuario
     *
     * @param usuarioId ID del usuario comprador
     * @param pageable Información de paginación
     * @return Página de órdenes del usuario
     */
    Page<OrdenCompra> findByUsuarioCompradorId(Long usuarioId, Pageable pageable);

    /**
     * Busca órdenes aprobadas por un usuario
     *
     * @param usuarioId ID del usuario aprobador
     * @param pageable Información de paginación
     * @return Página de órdenes aprobadas por el usuario
     */
    Page<OrdenCompra> findByUsuarioAprobadorId(Long usuarioId, Pageable pageable);

    // ===== CONTADORES =====

    /**
     * Cuenta órdenes por estado
     *
     * @param estado Estado de la orden
     * @return Número de órdenes en ese estado
     */
    long countByEstado(EstadoOrden estado);

    /**
     * Cuenta órdenes de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @return Número de órdenes del proveedor
     */
    long countByProveedorId(Long proveedorId);

    /**
     * Cuenta órdenes entre dos fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Número de órdenes en el rango
     */
    long countByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta órdenes por estado en un rango de fechas
     *
     * @param estado Estado de la orden
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Número de órdenes que cumplen
     */
    long countByEstadoAndFechaOrdenBetween(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaFin);

    // ===== QUERIES PERSONALIZADAS =====

    /**
     * Calcula el monto total de órdenes por estado
     *
     * @param estado Estado de las órdenes
     * @return Monto total
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o WHERE o.estado = :estado")
    BigDecimal calcularMontoTotalPorEstado(@Param("estado") EstadoOrden estado);

    /**
     * Calcula el monto total de órdenes en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Monto total
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o " +
            "WHERE o.fechaOrden BETWEEN :fechaInicio AND :fechaFin " +
            "AND o.estado <> 'CANCELADA'")
    BigDecimal calcularMontoTotalPeriodo(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca órdenes próximas a entregar (en los próximos N días)
     *
     * @param fechaActual Fecha actual
     * @param fechaLimite Fecha límite
     * @param estados Estados válidos
     * @return Lista de órdenes próximas a entregar
     */
    @Query("SELECT o FROM OrdenCompra o WHERE " +
            "o.fechaEntregaEstimada BETWEEN :fechaActual AND :fechaLimite " +
            "AND o.estado IN :estados " +
            "ORDER BY o.fechaEntregaEstimada ASC")
    List<OrdenCompra> findOrdenesProximas(
            @Param("fechaActual") LocalDate fechaActual,
            @Param("fechaLimite") LocalDate fechaLimite,
            @Param("estados") List<EstadoOrden> estados);

    /**
     * Busca órdenes atrasadas (fecha estimada pasada y no recibidas)
     *
     * @param fechaActual Fecha actual
     * @return Lista de órdenes atrasadas
     */
    @Query("SELECT o FROM OrdenCompra o WHERE " +
            "o.fechaEntregaEstimada < :fechaActual " +
            "AND o.estado IN ('ENVIADA', 'CONFIRMADA', 'RECIBIDA_PARCIAL') " +
            "ORDER BY o.fechaEntregaEstimada ASC")
    List<OrdenCompra> findOrdenesAtrasadas(@Param("fechaActual") LocalDate fechaActual);

    /**
     * Busca órdenes con criterios múltiples
     *
     * @param proveedorId ID del proveedor (puede ser null)
     * @param estado Estado de la orden (puede ser null)
     * @param fechaInicio Fecha inicial (puede ser null)
     * @param fechaFin Fecha final (puede ser null)
     * @param usuarioId ID del usuario comprador (puede ser null)
     * @param pageable Información de paginación
     * @return Página de órdenes que cumplen los criterios
     */
    @Query("SELECT o FROM OrdenCompra o WHERE " +
            "(:proveedorId IS NULL OR o.proveedor.id = :proveedorId) AND " +
            "(:estado IS NULL OR o.estado = :estado) AND " +
            "(:fechaInicio IS NULL OR o.fechaOrden >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR o.fechaOrden <= :fechaFin) AND " +
            "(:usuarioId IS NULL OR o.usuarioComprador.id = :usuarioId)")
    Page<OrdenCompra> buscarConCriterios(
            @Param("proveedorId") Long proveedorId,
            @Param("estado") EstadoOrden estado,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    /**
     * Obtiene órdenes con sus estadísticas
     *
     * @param pageable Información de paginación
     * @return Página de órdenes con estadísticas
     */
    @Query("SELECT o, COUNT(d) as numLineas, SUM(d.cantidad) as totalUnidades " +
            "FROM OrdenCompra o LEFT JOIN o.detalles d " +
            "GROUP BY o.id " +
            "ORDER BY o.fechaOrden DESC")
    Page<Object[]> findOrdenesConEstadisticas(Pageable pageable);

    /**
     * Obtiene el promedio de días entre orden y recepción
     *
     * @return Promedio de días
     */
    @Query("SELECT AVG(DATEDIFF(o.fechaEntregaReal, o.fechaOrden)) " +
            "FROM OrdenCompra o WHERE o.fechaEntregaReal IS NOT NULL")
    Double calcularPromedioTiempoEntrega();

    /**
     * Cuenta órdenes completadas a tiempo (recibidas antes o en la fecha estimada)
     *
     * @return Número de órdenes a tiempo
     */
    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE " +
            "o.fechaEntregaReal IS NOT NULL AND " +
            "o.fechaEntregaEstimada IS NOT NULL AND " +
            "o.fechaEntregaReal <= o.fechaEntregaEstimada")
    Long contarOrdenesATiempo();

    /**
     * Obtiene órdenes por proveedor con estadísticas
     *
     * @param proveedorId ID del proveedor
     * @return Lista de objetos con estadísticas
     */
    @Query("SELECT o.estado, COUNT(o), COALESCE(SUM(o.total), 0) " +
            "FROM OrdenCompra o WHERE o.proveedor.id = :proveedorId " +
            "GROUP BY o.estado")
    List<Object[]> obtenerEstadisticasPorProveedor(@Param("proveedorId") Long proveedorId);

    /**
     * Obtiene el monto total de compras de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @return Monto total
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o " +
            "WHERE o.proveedor.id = :proveedorId AND o.estado <> 'CANCELADA'")
    BigDecimal calcularMontoTotalProveedor(@Param("proveedorId") Long proveedorId);

    /**
     * Obtiene proveedores más frecuentes (con más órdenes)
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de proveedores con número de órdenes
     */
    @Query("SELECT o.proveedor, COUNT(o) as numOrdenes " +
            "FROM OrdenCompra o " +
            "GROUP BY o.proveedor " +
            "ORDER BY numOrdenes DESC")
    List<Object[]> findProveedoresMasFrecuentes(Pageable pageable);

    /**
     * Obtiene proveedores con mayor monto de compras
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de proveedores con monto total
     */
    @Query("SELECT o.proveedor, COALESCE(SUM(o.total), 0) as montoTotal " +
            "FROM OrdenCompra o WHERE o.estado <> 'CANCELADA' " +
            "GROUP BY o.proveedor " +
            "ORDER BY montoTotal DESC")
    List<Object[]> findProveedoresConMayorMonto(Pageable pageable);

    /**
     * Busca órdenes por término de búsqueda en número de orden, proveedor, etc.
     *
     * @param termino Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de órdenes que coinciden
     */
    @Query("SELECT o FROM OrdenCompra o WHERE " +
            "LOWER(o.numeroOrden) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(o.proveedor.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(o.observaciones) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<OrdenCompra> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    /**
     * Obtiene estadísticas por mes
     *
     * @param año Año a analizar
     * @return Lista de estadísticas por mes
     */
    @Query("SELECT MONTH(o.fechaOrden), COUNT(o), COALESCE(SUM(o.total), 0) " +
            "FROM OrdenCompra o WHERE YEAR(o.fechaOrden) = :año " +
            "AND o.estado <> 'CANCELADA' " +
            "GROUP BY MONTH(o.fechaOrden) " +
            "ORDER BY MONTH(o.fechaOrden)")
    List<Object[]> obtenerEstadisticasPorMes(@Param("año") int año);

    /**
     * Obtiene últimas órdenes de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de últimas órdenes
     */
    @Query("SELECT o FROM OrdenCompra o WHERE o.proveedor.id = :proveedorId " +
            "ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findUltimasOrdenesPorProveedor(
            @Param("proveedorId") Long proveedorId,
            Pageable pageable);

    /**
     * Calcula el total de unidades compradas en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de unidades
     */
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) " +
            "FROM OrdenCompra o JOIN o.detalles d " +
            "WHERE o.fechaOrden BETWEEN :fechaInicio AND :fechaFin " +
            "AND o.estado <> 'CANCELADA'")
    Long calcularTotalUnidadesPeriodo(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene productos más comprados
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de productos con cantidad total comprada
     */
    @Query("SELECT d.producto, COALESCE(SUM(d.cantidad), 0) as totalComprado " +
            "FROM OrdenCompra o JOIN o.detalles d " +
            "WHERE o.estado <> 'CANCELADA' " +
            "GROUP BY d.producto " +
            "ORDER BY totalComprado DESC")
    List<Object[]> findProductosMasComprados(Pageable pageable);

    /**
     * Verifica si un usuario tiene órdenes como comprador
     *
     * @param usuarioId ID del usuario
     * @return true si tiene órdenes
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM OrdenCompra o WHERE o.usuarioComprador.id = :usuarioId")
    boolean tieneOrdenesComoComprador(@Param("usuarioId") Long usuarioId);
}
