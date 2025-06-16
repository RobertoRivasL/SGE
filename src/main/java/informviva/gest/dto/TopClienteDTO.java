package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para clientes con mayores compras
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopClienteDTO {
    private Long id;
    private String nombre;
    private String rut;
    private String email;
    private Double totalCompras;
    private Integer cantidadCompras;
    private String categoria;
    private LocalDateTime ultimaCompra;
    private Double porcentajeDelTotal;
}