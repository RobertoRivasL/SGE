package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para vendedores con mejores resultados
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopVendedorDTO {
    private Long id;
    private String nombre;
    private String username;
    private Double totalVentas;
    private Integer cantidadVentas;
    private Double comisionEstimada;
    private Double ticketPromedio;
    private Double porcentajeDelTotal;
}