package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoReporteDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private Integer stock;
    private Integer unidadesVendidas;
    private BigDecimal ingresos;
    private BigDecimal precioPromedio;
    private Double porcentajeTotal;

    public void setCategoria(Categoria categoria) {
        if (categoria != null) {
            this.categoria = categoria.getNombre();
        } else {
            this.categoria = null;


        }
    }
}
