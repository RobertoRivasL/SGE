package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data // Proporciona Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos (puedes necesitar constructores adicionales si prefieres)
public class VentaResumenDTO {

    // --- Métricas de Resumen (basado en reportes.html y panel-ventas.html) ---
    // Usamos BigDecimal para manejar dinero con precisión
    private BigDecimal totalVentas;
    private Long totalTransacciones;
    private Long totalArticulosVendidos;
    private BigDecimal ticketPromedio;
    private Long clientesNuevos;
    private Long productosSinVentas;
    private BigDecimal promedioPrecio;
    private BigDecimal totalIngresos;

    // --- Métricas de Comparación (basado en panel-ventas.html) ---
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double porcentajeCambioTicketPromedio;
    private Double porcentajeCambioClientesNuevos;

    // --- Datos para Gráficos y Tablas (basado en reportes.html y panel-ventas.html) ---
    private List<ProductoVendidoDTO> productosMasVendidos;
    private List<VentaPorPeriodoDTO> ventasPorPeriodo;
    private List<VentaPorCategoriaDTO> ventasPorCategoria;
    private List<VentaPorVendedorDTO> ventasPorVendedor;

    public Long getProductosSinVentas() {
        return productosSinVentas;
    }

    public void setProductosSinVentas(Long productosSinVentas) {
        this.productosSinVentas = productosSinVentas;
    }

    public BigDecimal getPromedioPrecio() {
        return promedioPrecio;
    }

    public void setPromedioPrecio(BigDecimal promedioPrecio) {
        this.promedioPrecio = promedioPrecio;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }


}