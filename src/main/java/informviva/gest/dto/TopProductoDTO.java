// ==================== TopProductoDTO.java ====================
package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para productos más vendidos
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductoDTO {
    private Long id;
    private String nombre;
    private String codigo;
    private Integer cantidadVendida;
    private Double totalVentas;
    private String categoria;
    private Double margen;
    private Double porcentajeDelTotal;
}