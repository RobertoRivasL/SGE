package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Venta;
import java.util.List;

/**
 * Interfaz para el servicio de Ventas.
 * Define los métodos que el controlador puede invocar.
 */
public interface VentaServicio {
    Venta crearNuevaVenta(VentaDTO ventaDTO);
    List<Venta> obtenerTodasLasVentas();
    Venta obtenerVentaPorId(Long id);
}