package informviva.gest.dto;

/**
 * DTO para exportación de análisis de ventas por período
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
class AnalisisVentasPeriodoDTO {
    private String periodo; // "2024-01", "2024-02", etc.
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double ventasTotales;
    private Long transacciones;
    private Double ticketPromedio;
    private Long clientesUnicos;
    private Integer productosVendidos;
    private Double crecimientoVsPeriodoAnterior;
}