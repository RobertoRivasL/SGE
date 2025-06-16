package informviva.gest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class VentaDTO {

    @NotNull(message = "Debe seleccionar un cliente.")
    private Long clienteId;

    @NotEmpty(message = "Debe añadir al menos un producto a la venta.")
    @Valid
    private List<ProductoVentaDTO> productos;

    @Data
    public static class ProductoVentaDTO {
        @NotNull
        private Long productoId;

        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        private int cantidad;
    }


}