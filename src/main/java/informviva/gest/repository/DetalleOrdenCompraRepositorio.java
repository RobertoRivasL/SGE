package informviva.gest.repository;

import informviva.gest.model.DetalleOrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad DetalleOrdenCompra siguiendo el patrón Repository
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para acceso a datos de detalles de orden
 * - I: Interface segregada específica para detalles
 * - D: Abstracción para el acceso a datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Repository
public interface DetalleOrdenCompraRepositorio extends JpaRepository<DetalleOrdenCompra, Long> {

    // ===== BÚSQUEDAS POR ORDEN DE COMPRA =====

    /**
     * Busca todos los detalles de una orden de compra
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Lista de detalles de la orden
     */
    List<DetalleOrdenCompra> findByOrdenCompraId(Long ordenCompraId);

    /**
     * Busca todos los detalles de una orden de compra ordenados por número de línea
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Lista de detalles ordenados
     */
    List<DetalleOrdenCompra> findByOrdenCompraIdOrderByNumeroLineaAsc(Long ordenCompraId);

    /**
     * Cuenta detalles de una orden de compra
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Número de detalles
     */
    long countByOrdenCompraId(Long ordenCompraId);

    // ===== BÚSQUEDAS POR PRODUCTO =====

    /**
     * Busca detalles de un producto específico
     *
     * @param productoId ID del producto
     * @return Lista de detalles del producto
     */
    List<DetalleOrdenCompra> findByProductoId(Long productoId);

    /**
     * Busca detalles de un producto con paginación
     *
     * @param productoId ID del producto
     * @param pageable Información de paginación
     * @return Página de detalles del producto
     */
    Page<DetalleOrdenCompra> findByProductoId(Long productoId, Pageable pageable);

    /**
     * Busca un detalle específico por orden y producto
     *
     * @param ordenCompraId ID de la orden de compra
     * @param productoId ID del producto
     * @return Optional con el detalle si existe
     */
    Optional<DetalleOrdenCompra> findByOrdenCompraIdAndProductoId(Long ordenCompraId, Long productoId);

    /**
     * Verifica si existe un detalle para orden y producto
     *
     * @param ordenCompraId ID de la orden de compra
     * @param productoId ID del producto
     * @return true si existe
     */
    boolean existsByOrdenCompraIdAndProductoId(Long ordenCompraId, Long productoId);

    // ===== BÚSQUEDAS POR ESTADO DE RECEPCIÓN =====

    /**
     * Busca detalles completamente recibidos de una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Lista de detalles completamente recibidos
     */
    @Query("SELECT d FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId " +
            "AND d.cantidadRecibida = d.cantidad")
    List<DetalleOrdenCompra> findDetallesCompletosOrden(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Busca detalles pendientes (no completamente recibidos) de una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Lista de detalles pendientes
     */
    @Query("SELECT d FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId " +
            "AND d.cantidadRecibida < d.cantidad")
    List<DetalleOrdenCompra> findDetallesPendientesOrden(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Verifica si todos los detalles de una orden están completamente recibidos
     *
     * @param ordenCompraId ID de la orden de compra
     * @return true si todos están completos
     */
    @Query("SELECT CASE WHEN COUNT(d) = 0 THEN true ELSE false END " +
            "FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId " +
            "AND d.cantidadRecibida < d.cantidad")
    boolean todosLosDetallesRecibidos(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Verifica si hay al menos un detalle parcialmente recibido en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return true si hay recepción parcial
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId " +
            "AND d.cantidadRecibida > 0 AND d.cantidadRecibida < d.cantidad")
    boolean tieneRecepcionParcial(@Param("ordenCompraId") Long ordenCompraId);

    // ===== CÁLCULOS Y ESTADÍSTICAS =====

    /**
     * Calcula el total de unidades ordenadas en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Total de unidades
     */
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.ordenCompra.id = :ordenCompraId")
    Long calcularTotalUnidadesOrden(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Calcula el total de unidades recibidas en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Total de unidades recibidas
     */
    @Query("SELECT COALESCE(SUM(d.cantidadRecibida), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.ordenCompra.id = :ordenCompraId")
    Long calcularTotalUnidadesRecibidas(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Calcula el total de unidades pendientes en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Total de unidades pendientes
     */
    @Query("SELECT COALESCE(SUM(d.cantidad - d.cantidadRecibida), 0) " +
            "FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId")
    Long calcularTotalUnidadesPendientes(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Calcula el subtotal de una orden (suma de subtotales de detalles)
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Subtotal de la orden
     */
    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.ordenCompra.id = :ordenCompraId")
    BigDecimal calcularSubtotalOrden(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Calcula el total de descuentos en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Total de descuentos
     */
    @Query("SELECT COALESCE(SUM(d.montoDescuento), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.ordenCompra.id = :ordenCompraId")
    BigDecimal calcularTotalDescuentosOrden(@Param("ordenCompraId") Long ordenCompraId);

    // ===== ESTADÍSTICAS DE PRODUCTOS =====

    /**
     * Calcula el total de unidades compradas de un producto
     *
     * @param productoId ID del producto
     * @return Total de unidades compradas
     */
    @Query("SELECT COALESCE(SUM(d.cantidadRecibida), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId " +
            "AND d.ordenCompra.estado IN ('RECIBIDA_COMPLETA', 'COMPLETADA')")
    Long calcularTotalUnidadesCompradasProducto(@Param("productoId") Long productoId);

    /**
     * Calcula el monto total comprado de un producto
     *
     * @param productoId ID del producto
     * @return Monto total
     */
    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId " +
            "AND d.ordenCompra.estado IN ('RECIBIDA_COMPLETA', 'COMPLETADA')")
    BigDecimal calcularMontoTotalCompradoProducto(@Param("productoId") Long productoId);

    /**
     * Calcula el precio promedio de compra de un producto
     *
     * @param productoId ID del producto
     * @return Precio promedio
     */
    @Query("SELECT AVG(d.precioUnitario) FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId")
    BigDecimal calcularPrecioPromedioCompraProducto(@Param("productoId") Long productoId);

    /**
     * Obtiene el último precio de compra de un producto
     *
     * @param productoId ID del producto
     * @return Último precio de compra
     */
    @Query("SELECT d.precioUnitario FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId " +
            "ORDER BY d.ordenCompra.fechaOrden DESC")
    List<BigDecimal> findUltimoPrecioCompraProducto(@Param("productoId") Long productoId);

    /**
     * Cuenta el número de veces que se ha comprado un producto
     *
     * @param productoId ID del producto
     * @return Número de órdenes que incluyen el producto
     */
    @Query("SELECT COUNT(DISTINCT d.ordenCompra.id) FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId")
    Long contarOrdenesConProducto(@Param("productoId") Long productoId);

    // ===== BÚSQUEDAS AVANZADAS =====

    /**
     * Busca detalles con descuento mayor a cierto porcentaje
     *
     * @param porcentaje Porcentaje mínimo de descuento
     * @return Lista de detalles con descuento alto
     */
    @Query("SELECT d FROM DetalleOrdenCompra d " +
            "WHERE d.porcentajeDescuento >= :porcentaje")
    List<DetalleOrdenCompra> findDetallesConDescuentoMayorA(@Param("porcentaje") BigDecimal porcentaje);

    /**
     * Busca detalles con precio unitario en un rango
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de detalles en el rango
     */
    @Query("SELECT d FROM DetalleOrdenCompra d " +
            "WHERE d.precioUnitario BETWEEN :precioMin AND :precioMax")
    List<DetalleOrdenCompra> findDetallesPorRangoPrecio(
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax);

    /**
     * Obtiene productos distintos en una orden
     *
     * @param ordenCompraId ID de la orden de compra
     * @return Número de productos distintos
     */
    @Query("SELECT COUNT(DISTINCT d.producto.id) FROM DetalleOrdenCompra d " +
            "WHERE d.ordenCompra.id = :ordenCompraId")
    Long contarProductosDistintosOrden(@Param("ordenCompraId") Long ordenCompraId);

    /**
     * Obtiene proveedores de un producto (a través de sus órdenes)
     *
     * @param productoId ID del producto
     * @return Lista de proveedores que han suministrado el producto
     */
    @Query("SELECT DISTINCT d.ordenCompra.proveedor FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId")
    List<Object> findProveedoresDelProducto(@Param("productoId") Long productoId);

    /**
     * Busca últimas compras de un producto
     *
     * @param productoId ID del producto
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de últimos detalles del producto
     */
    @Query("SELECT d FROM DetalleOrdenCompra d " +
            "WHERE d.producto.id = :productoId " +
            "ORDER BY d.ordenCompra.fechaOrden DESC")
    List<DetalleOrdenCompra> findUltimasComprasProducto(
            @Param("productoId") Long productoId,
            Pageable pageable);

    /**
     * Obtiene detalles con mayor cantidad ordenada
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de detalles ordenados por cantidad
     */
    @Query("SELECT d FROM DetalleOrdenCompra d ORDER BY d.cantidad DESC")
    List<DetalleOrdenCompra> findDetallesMayorCantidad(Pageable pageable);

    /**
     * Obtiene detalles con mayor subtotal
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de detalles ordenados por subtotal
     */
    @Query("SELECT d FROM DetalleOrdenCompra d ORDER BY d.subtotal DESC")
    List<DetalleOrdenCompra> findDetallesMayorSubtotal(Pageable pageable);

    /**
     * Verifica si un producto ha sido comprado
     *
     * @param productoId ID del producto
     * @return true si el producto ha sido comprado
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM DetalleOrdenCompra d WHERE d.producto.id = :productoId")
    boolean productoHaSidoComprado(@Param("productoId") Long productoId);

    /**
     * Elimina todos los detalles de una orden
     *
     * @param ordenCompraId ID de la orden de compra
     */
    void deleteByOrdenCompraId(Long ordenCompraId);
}
