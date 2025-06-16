package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para reportes financieros
 * Migrado para usar LocalDateTime
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFinancieroDTO {
    // Período del reporte
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Métricas principales
    private Double ventasTotales;
    private Long transaccionesTotales;
    private Double ticketPromedio;
    private Double ingresoPromedioDiario;

    // Comparación con período anterior
    private Double ventasAnteriores;
    private Long transaccionesAnteriores;
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;

    // Métricas adicionales
    private Double margenBruto;
    private Double costoVentas;
    private Double utilidadBruta;
    private Double gastos;
    private Double utilidadNeta;

    // Análisis por categorías
    private Map<String, Double> ventasPorCategoria;
    private Map<String, Double> ventasPorMetodoPago;
    private Map<String, Long> transaccionesPorDia;

    // Proyecciones
    private Double proyeccionMensual;
    private Double metaMensual;
    private Double porcentajeMeta;

    // Estado del período
    private String resumenEjecutivo;
    private String recomendaciones;
}