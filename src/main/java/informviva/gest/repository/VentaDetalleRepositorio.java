package informviva.gest.repository;

import informviva.gest.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {

    /**
     * Verificar si existen detalles por producto
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Sumar cantidad vendida por rango de fechas
     */
    @Query("SELECT SUM(vd.cantidad) FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long sumCantidadByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Sumar cantidad vendida por producto
     */
    @Query("SELECT SUM(vd.cantidad) FROM VentaDetalle vd WHERE vd.producto.id = :productoId")
    Long sumCantidadByProductoId(@Param("productoId") Long productoId);
}