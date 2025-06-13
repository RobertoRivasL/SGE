package informviva.gest.repository;

import informviva.gest.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a las entidades VentaDetalle en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con detalles de venta.
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Repository
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {

    /**
     * Busca detalles de venta por ID de venta
     *
     * @param ventaId ID de la venta
     * @return Lista de detalles de venta
     */
    List<VentaDetalle> findByVentaId(Long ventaId);

    /**
     * Verifica si existen detalles para un producto específico
     *
     * @param productoId ID del producto
     * @return true si existen detalles, false en caso contrario
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Elimina todos los detalles de una venta específica
     *
     * @param ventaId ID de la venta
     */
    void deleteByVentaId(Long ventaId);

    /**
     * Cuenta la cantidad total de un producto vendido
     *
     * @param productoId ID del producto
     * @return Cantidad total vendida
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd WHERE vd.producto.id = :productoId")
    Long countByProductoId(@Param("productoId") Long productoId);

    /**
     * Busca detalles de venta por rango de fechas de la venta asociada
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Lista de detalles de venta
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<VentaDetalle> findByVentaFechaBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Calcula el total vendido de un producto en un período
     *
     * @param productoId  ID del producto
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Total vendido
     */
    @Query("SELECT COALESCE(SUM(vd.total), 0.0) FROM VentaDetalle vd " +
            "WHERE vd.producto.id = :productoId " +
            "AND vd.venta.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "AND vd.venta.estado != 'ANULADA'")
    Double calcularTotalVendidoPorProducto(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    //    /**
//     * Obtiene los productos más vendidos en un período
//     *
//     * @param fechaInicio Fecha de inicio
//     * @param fechaFin Fecha de fin
//     * @param limite Número máximo de resultados
//     * @return Lista de detalles agrupados por producto
//     */
    @Query("SELECT vd.producto.id, vd.producto.nombre, SUM(vd.cantidad), SUM(vd.total) " +
            "FROM VentaDetalle vd " +
            "WHERE vd.venta.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "AND vd.venta.estado != 'ANULADA' " +
            "GROUP BY vd.producto.id, vd.producto.nombre " +
            "ORDER BY SUM(vd.cantidad) DESC")
    List<Object[]> findProductosMasVendidos(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}