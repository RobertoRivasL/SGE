package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para resumen de ventas
 * Migrado para usar LocalDateTime como tipo principal
 * Usa clases auxiliares independientes para evitar problemas de compilación
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResumenDTO {

    // ==================== INFORMACIÓN DEL PERÍODO ====================

    /**
     * Fecha y hora de inicio del período analizado
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin del período analizado
     */
    private LocalDateTime fechaFin;

    /**
     * Cantidad de días en el período
     */
    private Integer diasPeriodo;

    // ==================== TOTALES GENERALES ====================

    /**
     * Total de ventas en el período (monto)
     */
    private Double totalVentas;

    /**
     * Cantidad total de ventas realizadas
     */
    private Long cantidadVentas;

    /**
     * Ticket promedio por venta
     */
    private Double ticketPromedio;

    /**
     * Cantidad total de productos vendidos (unidades)
     */
    private Integer productosVendidos;

    /**
     * Número de clientes únicos que compraron
     */
    private Integer clientesUnicos;

    // ==================== COMPARACIÓN CON PERÍODO ANTERIOR ====================

    /**
     * Total de ventas del período anterior
     */
    private Double totalVentasAnterior;

    /**
     * Variación absoluta respecto al período anterior
     */
    private Double variacionVentas;

    /**
     * Variación porcentual respecto al período anterior
     */
    private Double variacionPorcentual;

    // ==================== ANÁLISIS DIARIO ====================

    /**
     * Promedio de ventas por día
     */
    private Double promedioDiario;

    /**
     * Día con mayor volumen de ventas
     */
    private LocalDate mejorDia;

    /**
     * Monto de ventas del mejor día
     */
    private Double ventasMejorDia;

    /**
     * Día con menor volumen de ventas
     */
    private LocalDate peorDia;

    /**
     * Monto de ventas del peor día
     */
    private Double ventasPeorDia;

    // ==================== TOP PERFORMERS (USANDO CLASES INDEPENDIENTES) ====================

    /**
     * Lista de productos más vendidos
     */
    private List<TopProductoDTO> topProductos;

    /**
     * Lista de clientes con mayores compras
     */
    private List<TopClienteDTO> topClientes;

    /**
     * Lista de vendedores con mejores resultados
     */
    private List<TopVendedorDTO> topVendedores;

    // ==================== ANÁLISIS POR CATEGORÍAS ====================

    /**
     * Ventas agrupadas por categoría de producto
     */
    private Map<String, Double> ventasPorCategoria;

    /**
     * Cantidad de productos vendidos por categoría
     */
    private Map<String, Integer> cantidadPorCategoria;

    // ==================== ANÁLISIS POR MÉTODO DE PAGO ====================

    /**
     * Ventas agrupadas por método de pago
     */
    private Map<String, Double> ventasPorMetodoPago;

    /**
     * Número de transacciones por método de pago
     */
    private Map<String, Integer> transaccionesPorMetodoPago;

    // ==================== TENDENCIAS ====================

    /**
     * Detalle de ventas día a día
     */
    private List<VentaDiariaDTO> ventasDiarias;

    /**
     * Tendencia general del período: "CRECIENTE", "DECRECIENTE", "ESTABLE", "VOLATIL"
     */
    private String tendencia;

    // ==================== MÉTRICAS ADICIONALES ====================

    /**
     * Total de devoluciones en el período
     */
    private Double devolucionesTotales;

    /**
     * Porcentaje de descuentos aplicados
     */
    private Double porcentajeDescuentos;

    /**
     * Margen promedio de las ventas
     */
    private Double margenPromedio;

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Calcula la tasa de crecimiento diario promedio
     */
    public Double getTasaCrecimientoDiario() {
        if (diasPeriodo == null || diasPeriodo <= 1 || variacionPorcentual == null) {
            return 0.0;
        }
        return variacionPorcentual / diasPeriodo;
    }

    /**
     * Determina si el período fue exitoso (crecimiento > 0)
     */
    public Boolean isPeriodoExitoso() {
        return variacionPorcentual != null && variacionPorcentual > 0;
    }

    /**
     * Obtiene el rendimiento como texto descriptivo
     */
    public String getRendimientoDescriptivo() {
        if (variacionPorcentual == null) return "Sin datos";

        if (variacionPorcentual > 10) return "Excelente";
        if (variacionPorcentual > 5) return "Muy bueno";
        if (variacionPorcentual > 0) return "Bueno";
        if (variacionPorcentual > -5) return "Regular";
        return "Deficiente";
    }

    /**
     * Obtiene la tendencia como enum
     */
    public informviva.gest.enums.TendenciaVenta getTendenciaEnum() {
        return informviva.gest.enums.TendenciaVenta.determinarTendencia(variacionPorcentual);
    }

    // ==================== MÉTODOS DE CONSTRUCCIÓN ====================

    /**
     * Constructor de conveniencia para período básico
     */
    public VentaResumenDTO(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasPeriodo = (int) java.time.Duration.between(fechaInicio, fechaFin).toDays();
    }

    /**
     * Constructor con fechas LocalDate (se convierten automáticamente)
     */
    public VentaResumenDTO(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio.atStartOfDay();
        this.fechaFin = fechaFin.atTime(23, 59, 59);
        this.diasPeriodo = (int) java.time.Duration.between(this.fechaInicio, this.fechaFin).toDays();
    }

    // ==================== MÉTODOS DE COMPATIBILIDAD ====================

    /**
     * Obtiene fecha de inicio como LocalDate (para compatibilidad)
     */
    public LocalDate getFechaInicioDate() {
        return fechaInicio != null ? fechaInicio.toLocalDate() : null;
    }

    /**
     * Obtiene fecha de fin como LocalDate (para compatibilidad)
     */
    public LocalDate getFechaFinDate() {
        return fechaFin != null ? fechaFin.toLocalDate() : null;
    }
}