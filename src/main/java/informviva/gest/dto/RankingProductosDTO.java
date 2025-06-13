package informviva.gest.dto;

/**
 * DTO para exportación de ranking de productos
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
class RankingProductosDTO {
    private Integer posicion;
    private String codigo;
    private String nombre;
    private String categoria;
    private Long unidadesVendidas;
    private Double ingresosTotales;
    private Double porcentajeVentas;
    private Double precioPromedio;
    private Integer stockActual;
}
