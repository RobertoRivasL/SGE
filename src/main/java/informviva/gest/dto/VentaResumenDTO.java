package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO refactorizado para resumen de ventas
 * Corrige tipos genéricos y mejora la estructura de datos
 *
 * @author Roberto Rivas
 * @version 3.0 - REFACTORIZADO COMPLETO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResumenDTO {

    // ==================== MÉTRICAS PRINCIPALES ====================

    private BigDecimal totalVentas;
    private BigDecimal totalIngresos;
    private BigDecimal ticketPromedio;
    private Long totalTransacciones;
    private Long clientesNuevos;
    private Long clientesUnicos;
    private Long totalArticulosVendidos;
    private Long productosSinVentas;
    private BigDecimal promedioPrecio;

    // ==================== LISTAS TIPIFICADAS CORRECTAMENTE ====================

    /**
     * ✅ CORREGIDO: Tipo específico en lugar de List<Object>
     */
    private List<ProductoVendidoDTO> productosMasVendidos;

    /**
     * ✅ CORREGIDO: Tipo específico en lugar de List<Object>
     */
    private List<VentaPorPeriodoDTO> ventasPorPeriodo;

    /**
     * ✅ CORREGIDO: Tipo específico en lugar de List<Object>
     */
    private List<VentaPorVendedorDTO> ventasPorVendedor;

    /**
     * ✅ CORREGIDO: Tipo específico en lugar de List<Object>
     */
    private List<VentaPorCategoriaDTO> ventasPorCategoria;

    // ==================== DATOS ADICIONALES ====================

    private Map<String, Double> ventasPorMetodoPago;
    private Map<String, Long> ventasPorEstado;
    private Map<String, Double> ventasPorMes;
    private Map<String, Double> ventasPorDiaSemana;

    // ==================== MÉTRICAS DE COMPARACIÓN ====================

    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double porcentajeCambioTicketPromedio;
    private Double porcentajeCambioClientesNuevos;
    private Double porcentajeCambioIngresos;

    // ==================== INFORMACIÓN DE PERÍODO ====================

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaGeneracion;
    private String periodo; // "DIARIO", "SEMANAL", "MENSUAL", "ANUAL"

    // ==================== MÉTRICAS AVANZADAS ====================

    private Double tasaCrecimientoVentas;
    private Double margenPromedio;
    private Double valorPromedioCliente;
    private Integer ventasPromedioPorDia;
    private Double concentracionVentas; // % de ventas de top 20% productos

    // ==================== ALERTAS Y OBSERVACIONES ====================

    private List<String> alertas;
    private List<String> observaciones;
    private String tendenciaGeneral; // "CRECIENTE", "DECRECIENTE", "ESTABLE"

    // ==================== CONSTRUCTORES ADICIONALES ====================

    /**
     * Constructor básico con métricas principales
     */
    public VentaResumenDTO(BigDecimal totalVentas, BigDecimal totalIngresos,
                           Long totalTransacciones, BigDecimal ticketPromedio) {
        this.totalVentas = totalVentas;
        this.totalIngresos = totalIngresos;
        this.totalTransacciones = totalTransacciones;
        this.ticketPromedio = ticketPromedio;
        this.fechaGeneracion = LocalDateTime.now();
    }

    /**
     * Constructor con período específico
     */
    public VentaResumenDTO(LocalDateTime fechaInicio, LocalDateTime fechaFin, String periodo) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.periodo = periodo;
        this.fechaGeneracion = LocalDateTime.now();

        // Inicializar valores por defecto
        this.totalVentas = BigDecimal.ZERO;
        this.totalIngresos = BigDecimal.ZERO;
        this.ticketPromedio = BigDecimal.ZERO;
        this.totalTransacciones = 0L;
        this.clientesNuevos = 0L;
        this.clientesUnicos = 0L;
        this.totalArticulosVendidos = 0L;
        this.productosSinVentas = 0L;
        this.promedioPrecio = BigDecimal.ZERO;
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    /**
     * Calcula el ticket promedio basado en totales
     */
    public BigDecimal calcularTicketPromedio() {
        if (totalTransacciones != null && totalTransacciones > 0 && totalIngresos != null) {
            return totalIngresos.divide(BigDecimal.valueOf(totalTransacciones), 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el promedio de productos por transacción
     */
    public Double getPromedioProductosPorTransaccion() {
        if (totalTransacciones != null && totalTransacciones > 0 && totalArticulosVendidos != null) {
            return totalArticulosVendidos.doubleValue() / totalTransacciones.doubleValue();
        }
        return 0.0;
    }

    /**
     * Calcula la tasa de conversión de clientes nuevos
     */
    public Double getTasaConversionClientesNuevos() {
        if (clientesUnicos != null && clientesUnicos > 0 && clientesNuevos != null) {
            return (clientesNuevos.doubleValue() / clientesUnicos.doubleValue()) * 100;
        }
        return 0.0;
    }

    /**
     * Obtiene el crecimiento porcentual de ventas
     */
    public String getCrecimientoVentasFormateado() {
        if (porcentajeCambioVentas != null) {
            String signo = porcentajeCambioVentas >= 0 ? "+" : "";
            return signo + String.format("%.2f%%", porcentajeCambioVentas);
        }
        return "N/A";
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verifica si el resumen tiene datos válidos
     */
    public boolean tienesDatosValidos() {
        return totalTransacciones != null && totalTransacciones > 0;
    }

    /**
     * Verifica si hay mejora en las métricas principales
     */
    public boolean hayMejoraEnMetricas() {
        return (porcentajeCambioVentas != null && porcentajeCambioVentas > 0) ||
                (porcentajeCambioTransacciones != null && porcentajeCambioTransacciones > 0) ||
                (porcentajeCambioTicketPromedio != null && porcentajeCambioTicketPromedio > 0);
    }

    // ==================== MÉTODOS DE FORMATEO ====================

    /**
     * Formatea el total de ventas como moneda
     */
    public String getTotalVentasFormateado() {
        if (totalVentas != null) {
            return "$" + String.format("%,.2f", totalVentas);
        }
        return "$0.00";
    }

    /**
     * Formatea el ticket promedio como moneda
     */
    public String getTicketPromedioFormateado() {
        if (ticketPromedio != null) {
            return "$" + String.format("%,.2f", ticketPromedio);
        }
        return "$0.00";
    }

    /**
     * Obtiene el período en formato legible
     */
    public String getPeriodoFormateado() {
        if (fechaInicio != null && fechaFin != null) {
            return fechaInicio.toLocalDate() + " al " + fechaFin.toLocalDate();
        }
        return periodo != null ? periodo : "No especificado";
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Inicializa listas vacías si son null
     */
    public void inicializarListasVacias() {
        if (productosMasVendidos == null) {
            productosMasVendidos = new java.util.ArrayList<>();
        }
        if (ventasPorPeriodo == null) {
            ventasPorPeriodo = new java.util.ArrayList<>();
        }
        if (ventasPorVendedor == null) {
            ventasPorVendedor = new java.util.ArrayList<>();
        }
        if (ventasPorCategoria == null) {
            ventasPorCategoria = new java.util.ArrayList<>();
        }
        if (alertas == null) {
            alertas = new java.util.ArrayList<>();
        }
        if (observaciones == null) {
            observaciones = new java.util.ArrayList<>();
        }
    }

    /**
     * Agrega una alerta al resumen
     */
    public void agregarAlerta(String alerta) {
        if (alertas == null) {
            alertas = new java.util.ArrayList<>();
        }
        alertas.add(alerta);
    }

    /**
     * Agrega una observación al resumen
     */
    public void agregarObservacion(String observacion) {
        if (observaciones == null) {
            observaciones = new java.util.ArrayList<>();
        }
        observaciones.add(observacion);
    }

    /**
     * Establece la tendencia basada en los porcentajes de cambio
     */
    public void calcularTendenciaGeneral() {
        if (porcentajeCambioVentas != null) {
            if (porcentajeCambioVentas > 5) {
                tendenciaGeneral = "CRECIENTE";
            } else if (porcentajeCambioVentas < -5) {
                tendenciaGeneral = "DECRECIENTE";
            } else {
                tendenciaGeneral = "ESTABLE";
            }
        } else {
            tendenciaGeneral = "ESTABLE";
        }
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder para construcción fluida del DTO
     */
    public static class Builder {
        private VentaResumenDTO resumen = new VentaResumenDTO();

        public Builder totalVentas(BigDecimal totalVentas) {
            resumen.totalVentas = totalVentas;
            return this;
        }

        public Builder totalIngresos(BigDecimal totalIngresos) {
            resumen.totalIngresos = totalIngresos;
            return this;
        }

        public Builder totalTransacciones(Long totalTransacciones) {
            resumen.totalTransacciones = totalTransacciones;
            return this;
        }

        public Builder ticketPromedio(BigDecimal ticketPromedio) {
            resumen.ticketPromedio = ticketPromedio;
            return this;
        }

        public Builder clientesNuevos(Long clientesNuevos) {
            resumen.clientesNuevos = clientesNuevos;
            return this;
        }

        public Builder periodo(LocalDateTime inicio, LocalDateTime fin, String tipo) {
            resumen.fechaInicio = inicio;
            resumen.fechaFin = fin;
            resumen.periodo = tipo;
            return this;
        }

        public Builder productosMasVendidos(List<ProductoVendidoDTO> productos) {
            resumen.productosMasVendidos = productos;
            return this;
        }

        public VentaResumenDTO build() {
            resumen.fechaGeneracion = LocalDateTime.now();
            resumen.inicializarListasVacias();
            resumen.calcularTendenciaGeneral();
            return resumen;
        }
    }

    /**
     * Método estático para iniciar el builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "VentaResumenDTO{" +
                "totalVentas=" + totalVentas +
                ", totalTransacciones=" + totalTransacciones +
                ", ticketPromedio=" + ticketPromedio +
                ", periodo='" + periodo + '\'' +
                ", tendencia='" + tendenciaGeneral + '\'' +
                '}';
    }
}