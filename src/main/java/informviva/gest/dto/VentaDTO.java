package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {

    private Long id;

    private LocalDateTime fecha;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El vendedor es obligatorio")
    private Long vendedorId;

    private String metodoPago;

    private String observaciones;

    @Valid
    @NotNull(message = "Debe incluir al menos un producto")
    @Size(min = 1, message = "Debe incluir al menos un producto")
    private List<VentaDetalleDTO> detalles;
}