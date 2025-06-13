package informviva.gest.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaAvanzadaDTO {
    private String texto;
    private Long categoriaId;
    private Double precioMin;
    private Double precioMax;
    private Integer stockMin;
    private Boolean soloActivos;
    private Boolean soloConStock;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}