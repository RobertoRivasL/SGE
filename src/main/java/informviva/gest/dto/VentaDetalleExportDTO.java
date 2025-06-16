package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para exportación de detalles de venta
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleExportDTO {
    private Long id;
    private String productoNombre;
    private String productoCodigo;
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuento;
    private Double subtotal;
    private String categoria;
    private String marca;
}