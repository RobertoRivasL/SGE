package informviva.gest.dto;

/**
 * DTO para detalles de venta en exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
class VentaDetalleExportDTO {
    private Long ventaId;
    private String productoNombre;
    private String productoCodigo;
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuento;
    private Double subtotal;
    private Double total;
}
