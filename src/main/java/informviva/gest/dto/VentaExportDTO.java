package informviva.gest.dto;

/**
 * DTO para exportación de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaExportDTO {
    private Long id;
    private LocalDateTime fecha;
    private String clienteNombre;
    private String clienteRut;
    private String vendedorNombre;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String metodoPago;
    private String estado;
    private String observaciones;
    private Integer cantidadItems;
}