package informviva.gest.repository;

import informviva.gest.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad VentaDetalle.
 * Indispensable para que VentaServicioImpl funcione.
 */
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {
}
