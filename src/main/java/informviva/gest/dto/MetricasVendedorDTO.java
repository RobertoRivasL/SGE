package informviva.gest.dto;

/**
 * DTO para exportación de métricas de vendedores
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
class MetricasVendedorDTO {
    private Long vendedorId;
    private String nombreCompleto;
    private String username;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long ventasRealizadas;
    private Double montoTotalVendido;
    private Double promedioVentaPorTransaccion;
    private Long clientesAtendidos;
    private Double comisionGanada;
    private Integer metaCumplida; // porcentaje de cumplimiento de meta
}