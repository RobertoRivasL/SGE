package informviva.gest.dto;

/**
 * DTO para reportes financieros
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
public class ReporteFinancieroDTO {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double ventasTotales;
    private Long transaccionesTotales;
    private Double ventasAnteriores;
    private Long transaccionesAnteriores;
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double ticketPromedio;
    private Double ticketPromedioAnterior;
    private Double porcentajeCambioTicket;
}