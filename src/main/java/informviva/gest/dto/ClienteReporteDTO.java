package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteReporteDTO {
    private Long id;
    private String rut;
    private String nombreCompleto;
    private String email;
    private LocalDate fechaRegistro;
    private Integer comprasRealizadas;
    private BigDecimal totalCompras;
    private BigDecimal promedioPorCompra;
    private LocalDate ultimaCompra;
}