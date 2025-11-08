package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para estadísticas e indicadores del módulo de compras
 *
 * Proporciona métricas y KPIs para análisis de compras y proveedores
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasComprasDTO {

    // ===== ESTADÍSTICAS GENERALES =====

    /**
     * Total de órdenes de compra en el sistema
     */
    private Long totalOrdenes;

    /**
     * Total de proveedores registrados
     */
    private Long totalProveedores;

    /**
     * Total de proveedores activos
     */
    private Long proveedoresActivos;

    /**
     * Total de proveedores inactivos
     */
    private Long proveedoresInactivos;

    /**
     * Órdenes en estado pendiente
     */
    private Long ordenesPendientes;

    /**
     * Órdenes en estado enviadas
     */
    private Long ordenesEnviadas;

    /**
     * Órdenes en estado confirmadas
     */
    private Long ordenesConfirmadas;

    /**
     * Órdenes completadas
     */
    private Long ordenesCompletadas;

    /**
     * Órdenes canceladas
     */
    private Long ordenesCanceladas;

    /**
     * Órdenes en borrador
     */
    private Long ordenesBorrador;

    // ===== MONTOS Y VALORIZACIÓN =====

    /**
     * Monto total de todas las compras
     */
    private BigDecimal montoTotalCompras;

    /**
     * Monto total de compras del mes actual
     */
    private BigDecimal montoMes;

    /**
     * Monto total de compras del mes anterior
     */
    private BigDecimal montoMesAnterior;

    /**
     * Monto total de compras del año actual
     */
    private BigDecimal montoAnio;

    /**
     * Monto promedio por orden de compra
     */
    private BigDecimal promedioOrden;

    /**
     * Monto de órdenes pendientes
     */
    private BigDecimal montoPendiente;

    /**
     * Monto de órdenes confirmadas (comprometido)
     */
    private BigDecimal montoComprometido;

    // ===== PROVEEDORES DESTACADOS =====

    /**
     * Proveedor con más órdenes
     */
    private String proveedorMasFrecuente;

    /**
     * ID del proveedor más frecuente
     */
    private Long proveedorMasFrecuenteId;

    /**
     * Número de órdenes del proveedor más frecuente
     */
    private Long ordenasProveedorMasFrecuente;

    /**
     * Proveedor con mayor monto de compras
     */
    private String proveedorMayorMonto;

    /**
     * ID del proveedor con mayor monto
     */
    private Long proveedorMayorMontoId;

    /**
     * Monto total del proveedor principal
     */
    private BigDecimal montoProveedorPrincipal;

    /**
     * Lista de top proveedores
     */
    private List<ProveedorEstadisticaDTO> topProveedores = new ArrayList<>();

    // ===== PRODUCTOS Y CATEGORÍAS =====

    /**
     * Total de productos distintos comprados
     */
    private Long totalProductosComprados;

    /**
     * Total de unidades compradas (mes actual)
     */
    private Long totalUnidadesCompradas;

    /**
     * Producto más comprado
     */
    private String productoMasComprado;

    /**
     * ID del producto más comprado
     */
    private Long productoMasCompradoId;

    /**
     * Unidades del producto más comprado
     */
    private Long unidadesProductoMasComprado;

    // ===== TENDENCIAS Y COMPARATIVAS =====

    /**
     * Variación de compras respecto al mes anterior (%)
     */
    private Double variacionMensualPorcentaje;

    /**
     * Variación de compras respecto al año anterior (%)
     */
    private Double variacionAnualPorcentaje;

    /**
     * Tendencia de compras (CRECIENTE, DECRECIENTE, ESTABLE)
     */
    private String tendenciaCompras;

    /**
     * Promedio de días para completar una orden
     */
    private Double promedioTiempoEntrega;

    /**
     * Tasa de cumplimiento de proveedores (%)
     */
    private Double tasaCumplimiento;

    /**
     * Órdenes completadas a tiempo (%)
     */
    private Double porcentajeATiempo;

    // ===== ALERTAS Y NOTIFICACIONES =====

    /**
     * Órdenes atrasadas (pasada la fecha estimada)
     */
    private Long ordenesAtrasadas;

    /**
     * Órdenes próximas a vencer (7 días)
     */
    private Long ordenesProximasVencer;

    /**
     * Proveedores sin órdenes en los últimos 90 días
     */
    private Long proveedoresInactivos90Dias;

    /**
     * Lista de órdenes próximas a entregar
     */
    private List<OrdenProximaDTO> ordenesProximas = new ArrayList<>();

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
     * DTO interno para estadísticas de proveedor
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorEstadisticaDTO {
        private Long id;
        private String nombre;
        private String rut;
        private Long numeroOrdenes;
        private BigDecimal montoTotal;
        private BigDecimal montoPromedio;
        private Integer calificacion;
        private Double tasaCumplimiento;
        private LocalDateTime ultimaCompra;
    }

    /**
     * DTO interno para órdenes próximas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdenProximaDTO {
        private Long id;
        private String numeroOrden;
        private String proveedorNombre;
        private LocalDateTime fechaEntregaEstimada;
        private BigDecimal total;
        private String estado;
        private Integer diasRestantes;
    }

    /**
     * DTO interno para productos comprados
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoCompradoDTO {
        private Long id;
        private String nombre;
        private String codigo;
        private Long cantidadTotal;
        private BigDecimal costoTotal;
        private BigDecimal costoPromedio;
        private Integer numeroProveedores;
    }

    /**
     * Constructor con valores por defecto
     */
    public EstadisticasComprasDTO(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
        this.totalOrdenes = 0L;
        this.totalProveedores = 0L;
        this.proveedoresActivos = 0L;
        this.proveedoresInactivos = 0L;
        this.ordenesPendientes = 0L;
        this.ordenesEnviadas = 0L;
        this.ordenesConfirmadas = 0L;
        this.ordenesCompletadas = 0L;
        this.ordenesCanceladas = 0L;
        this.ordenesBorrador = 0L;
        this.montoTotalCompras = BigDecimal.ZERO;
        this.montoMes = BigDecimal.ZERO;
        this.montoMesAnterior = BigDecimal.ZERO;
        this.montoAnio = BigDecimal.ZERO;
        this.promedioOrden = BigDecimal.ZERO;
        this.montoPendiente = BigDecimal.ZERO;
        this.montoComprometido = BigDecimal.ZERO;
        this.totalProductosComprados = 0L;
        this.totalUnidadesCompradas = 0L;
        this.variacionMensualPorcentaje = 0.0;
        this.variacionAnualPorcentaje = 0.0;
        this.promedioTiempoEntrega = 0.0;
        this.tasaCumplimiento = 0.0;
        this.porcentajeATiempo = 0.0;
        this.ordenesAtrasadas = 0L;
        this.ordenesProximasVencer = 0L;
        this.proveedoresInactivos90Dias = 0L;
        this.topProveedores = new ArrayList<>();
        this.ordenesProximas = new ArrayList<>();
    }

    /**
     * Calcula métricas derivadas
     */
    public void calcularMetricasDerivadas() {
        // Calcular promedio por orden
        if (totalOrdenes != null && totalOrdenes > 0 && montoTotalCompras != null) {
            this.promedioOrden = montoTotalCompras.divide(
                    BigDecimal.valueOf(totalOrdenes),
                    2,
                    BigDecimal.ROUND_HALF_UP
            );
        }

        // Calcular variación mensual
        if (montoMes != null && montoMesAnterior != null && montoMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diferencia = montoMes.subtract(montoMesAnterior);
            this.variacionMensualPorcentaje = diferencia
                    .divide(montoMesAnterior, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        // Determinar tendencia
        if (variacionMensualPorcentaje != null) {
            if (variacionMensualPorcentaje > 5.0) {
                this.tendenciaCompras = "CRECIENTE";
            } else if (variacionMensualPorcentaje < -5.0) {
                this.tendenciaCompras = "DECRECIENTE";
            } else {
                this.tendenciaCompras = "ESTABLE";
            }
        }

        // Calcular tasa de cumplimiento
        if (ordenesCompletadas != null && totalOrdenes != null && totalOrdenes > 0) {
            this.tasaCumplimiento = (ordenesCompletadas.doubleValue() / totalOrdenes.doubleValue()) * 100.0;
        }
    }

    /**
     * Obtiene un resumen textual de las estadísticas
     *
     * @return Resumen de estadísticas
     */
    public String getResumen() {
        return String.format(
                "Compras: %d órdenes por $%s | Proveedores activos: %d | " +
                        "Pendientes: %d | Completadas: %d | Variación mensual: %.2f%%",
                totalOrdenes != null ? totalOrdenes : 0,
                montoTotalCompras != null ? montoTotalCompras.toString() : "0",
                proveedoresActivos != null ? proveedoresActivos : 0,
                ordenesPendientes != null ? ordenesPendientes : 0,
                ordenesCompletadas != null ? ordenesCompletadas : 0,
                variacionMensualPorcentaje != null ? variacionMensualPorcentaje : 0.0
        );
    }

    /**
     * Obtiene el nivel de salud del módulo de compras
     *
     * @return EXCELENTE, BUENO, REGULAR, BAJO
     */
    public String getNivelSalud() {
        if (tasaCumplimiento == null) return "DESCONOCIDO";

        if (tasaCumplimiento >= 90.0) return "EXCELENTE";
        if (tasaCumplimiento >= 75.0) return "BUENO";
        if (tasaCumplimiento >= 60.0) return "REGULAR";
        return "BAJO";
    }
}
