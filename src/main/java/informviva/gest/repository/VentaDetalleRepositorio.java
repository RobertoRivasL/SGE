package informviva.gest.repository;

import informviva.gest.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Repositorio para la entidad VentaDetalle.
 * Indispensable para que VentaServicioImpl funcione.
 */
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {

    Long sumCantidadByVenta_FechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT SUM(vd.cantidad) FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :inicio AND :fin")
    Long sumCantidadByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    boolean existsByProductoId(Long productoId);
}
