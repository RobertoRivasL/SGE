package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de detalle de venta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleDTO {

    private Long id;

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private Double precioUnitario;

    private Double descuento;
}