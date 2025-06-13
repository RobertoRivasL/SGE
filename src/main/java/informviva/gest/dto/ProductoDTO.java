package informviva.gest.dto;

import informviva.gest.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private Integer stock;
    private BigDecimal precio;
    private String descripcion;

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}