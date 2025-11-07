package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para estadísticas e indicadores del inventario
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasInventarioDTO {

    // ===== ESTADÍSTICAS GENERALES =====

    /**
     * Total de productos en el sistema
     */
    private Long totalProductos;

    /**
     * Total de productos activos
     */
    private Long productosActivos;

    /**
     * Total de productos inactivos
     */
    private Long productosInactivos;

    /**
     * Total de unidades en stock
     */
    private Long totalUnidadesStock;

    /**
     * Valor total del inventario
     */
    private BigDecimal valorTotalInventario;

    // ===== ALERTAS DE STOCK =====

    /**
     * Productos con stock bajo (debajo del mínimo)
     */
    private Long productosStockBajo;

    /**
     * Productos sin stock
     */
    private Long productosSinStock;

    /**
     * Productos con stock crítico (0 o negativo)
     */
    private Long productosStockCritico;

    /**
     * Lista de productos con bajo stock
     */
    private List<ProductoStockDTO> productosConStockBajo = new ArrayList<>();

    // ===== MOVIMIENTOS =====

    /**
     * Total de movimientos registrados
     */
    private Long totalMovimientos;

    /**
     * Movimientos del día actual
     */
    private Long movimientosHoy;

    /**
     * Movimientos del mes actual
     */
    private Long movimientosMes;

    /**
     * Total de entradas (mes actual)
     */
    private Long totalEntradasMes;

    /**
     * Total de salidas (mes actual)
     */
    private Long totalSalidasMes;

    /**
     * Unidades entradas (mes actual)
     */
    private Long unidadesEntradasMes;

    /**
     * Unidades salidas (mes actual)
     */
    private Long unidadesSalidasMes;

    // ===== VALORIZACIÓN =====

    /**
     * Costo total de entradas (mes actual)
     */
    private BigDecimal costoEntradasMes;

    /**
     * Costo total de salidas (mes actual)
     */
    private BigDecimal costoSalidasMes;

    /**
     * Valor promedio por producto
     */
    private BigDecimal valorPromedioProducto;

    // ===== TENDENCIAS =====

    /**
     * Variación de stock respecto al mes anterior (%)
     */
    private Double variacionStockPorcentaje;

    /**
     * Tendencia de movimientos (CRECIENTE, DECRECIENTE, ESTABLE)
     */
    private String tendenciaMovimientos;

    /**
     * Rotación de inventario (veces)
     */
    private Double rotacionInventario;

    // ===== METADATA =====

    /**
     * Fecha de generación de las estadísticas
     */
    private LocalDateTime fechaGeneracion;

    /**
     * Período analizado
     */
    private String periodoAnalizado;

    /**
     * DTO interno para productos con stock bajo
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoStockDTO {
        private Long id;
        private String codigo;
        private String nombre;
        private Integer stockActual;
        private Integer stockMinimo;
        private Integer diferencia;
        private String estado; // CRITICO, BAJO, NORMAL

        public ProductoStockDTO(Long id, String codigo, String nombre, Integer stockActual, Integer stockMinimo) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.diferencia = stockActual - stockMinimo;
            this.estado = calcularEstado();
        }

        private String calcularEstado() {
            if (stockActual == null || stockActual <= 0) return "CRITICO";
            if (stockMinimo == null) return "NORMAL";
            if (stockActual < stockMinimo) return "BAJO";
            return "NORMAL";
        }
    }

    /**
     * Constructor con valores por defecto
     */
    public EstadisticasInventarioDTO(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
        this.totalProductos = 0L;
        this.productosActivos = 0L;
        this.productosInactivos = 0L;
        this.totalUnidadesStock = 0L;
        this.valorTotalInventario = BigDecimal.ZERO;
        this.productosStockBajo = 0L;
        this.productosSinStock = 0L;
        this.productosStockCritico = 0L;
        this.totalMovimientos = 0L;
        this.movimientosHoy = 0L;
        this.movimientosMes = 0L;
        this.totalEntradasMes = 0L;
        this.totalSalidasMes = 0L;
        this.unidadesEntradasMes = 0L;
        this.unidadesSalidasMes = 0L;
        this.costoEntradasMes = BigDecimal.ZERO;
        this.costoSalidasMes = BigDecimal.ZERO;
        this.valorPromedioProducto = BigDecimal.ZERO;
        this.variacionStockPorcentaje = 0.0;
        this.rotacionInventario = 0.0;
    }

    /**
     * Calcula métricas derivadas
     */
    public void calcularMetricasDerivadas() {
        // Calcular valor promedio por producto
        if (totalProductos != null && totalProductos > 0 && valorTotalInventario != null) {
            this.valorPromedioProducto = valorTotalInventario.divide(
                    BigDecimal.valueOf(totalProductos),
                    2,
                    BigDecimal.ROUND_HALF_UP
            );
        }

        // Calcular rotación de inventario
        if (valorTotalInventario != null && !valorTotalInventario.equals(BigDecimal.ZERO) &&
                costoSalidasMes != null) {
            double rotacion = costoSalidasMes.divide(valorTotalInventario, 4, BigDecimal.ROUND_HALF_UP)
                    .doubleValue() * 12; // Anualizado
            this.rotacionInventario = Math.round(rotacion * 100.0) / 100.0;
        }

        // Determinar tendencia
        if (unidadesEntradasMes != null && unidadesSalidasMes != null) {
            long diferencia = unidadesEntradasMes - unidadesSalidasMes;
            if (diferencia > 0) {
                this.tendenciaMovimientos = "CRECIENTE";
            } else if (diferencia < 0) {
                this.tendenciaMovimientos = "DECRECIENTE";
            } else {
                this.tendenciaMovimientos = "ESTABLE";
            }
        }
    }
}
