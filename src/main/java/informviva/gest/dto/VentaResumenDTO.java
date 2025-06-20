package informviva.gest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 🔧 PASO 1: VentaResumenDTO corregido
 * ✅ Cambiar List<Object> por tipos específicos
 * ✅ Agregar campos adicionales necesarios
 */
public class VentaResumenDTO {

    // === CAMPOS EXISTENTES (mantener) ===
    private BigDecimal totalVentas;
    private BigDecimal ticketPromedio;
    private Long totalTransacciones;
    private Long clientesNuevos;

    // 🔧 CAMBIO PRINCIPAL: List<Object> → tipos específicos
    private List<ProductoVendidoDTO> productosMasVendidos;     // ✅ Era List<Object>
    private List<VentaPorPeriodoDTO> ventasPorPeriodo;        // ✅ Era List<Object>
    private List<VentaPorVendedorDTO> ventasPorVendedor;      // ✅ Era List<Object>

    // ✅ Este ya estaba bien tipado
    private Map<String, Double> ventasPorCategoria;

    // 🆕 CAMPOS ADICIONALES (requeridos por ExportacionServicio)
    private Long totalArticulosVendidos;
    private Long productosSinVentas;
    private BigDecimal promedioPrecio;
    private BigDecimal totalIngresos;

    // === GETTERS Y SETTERS ===

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public Long getTotalTransacciones() {
        return totalTransacciones;
    }

    public void setTotalTransacciones(Long totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }

    public Long getClientesNuevos() {
        return clientesNuevos;
    }

    public void setClientesNuevos(Long clientesNuevos) {
        this.clientesNuevos = clientesNuevos;
    }

    // 🔧 MÉTODOS CORREGIDOS: Con tipos específicos
    public List<ProductoVendidoDTO> getProductosMasVendidos() {
        return productosMasVendidos;
    }

    public void setProductosMasVendidos(List<ProductoVendidoDTO> productosMasVendidos) {
        this.productosMasVendidos = productosMasVendidos;
    }

    public List<VentaPorPeriodoDTO> getVentasPorPeriodo() {
        return ventasPorPeriodo;
    }

    public void setVentasPorPeriodo(List<VentaPorPeriodoDTO> ventasPorPeriodo) {
        this.ventasPorPeriodo = ventasPorPeriodo;
    }

    public Map<String, Double> getVentasPorCategoria() {
        return ventasPorCategoria;
    }

    public void setVentasPorCategoria(Map<String, Double> ventasPorCategoria) {
        this.ventasPorCategoria = ventasPorCategoria;
    }

    public List<VentaPorVendedorDTO> getVentasPorVendedor() {
        return ventasPorVendedor;
    }

    public void setVentasPorVendedor(List<VentaPorVendedorDTO> ventasPorVendedor) {
        this.ventasPorVendedor = ventasPorVendedor;
    }

    // 🆕 GETTERS/SETTERS NUEVOS
    public Long getTotalArticulosVendidos() {
        return totalArticulosVendidos;
    }

    public void setTotalArticulosVendidos(Long totalArticulosVendidos) {
        this.totalArticulosVendidos = totalArticulosVendidos;
    }

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

    // En VentaResumenDTO.java
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double porcentajeCambioTicketPromedio;
    private Double porcentajeCambioClientesNuevos;

    public Double getPorcentajeCambioVentas() { return porcentajeCambioVentas; }
    public void setPorcentajeCambioVentas(Double porcentajeCambioVentas) { this.porcentajeCambioVentas = porcentajeCambioVentas; }

    public Double getPorcentajeCambioTransacciones() { return porcentajeCambioTransacciones; }
    public void setPorcentajeCambioTransacciones(Double porcentajeCambioTransacciones) { this.porcentajeCambioTransacciones = porcentajeCambioTransacciones; }

    public Double getPorcentajeCambioTicketPromedio() { return porcentajeCambioTicketPromedio; }
    public void setPorcentajeCambioTicketPromedio(Double porcentajeCambioTicketPromedio) { this.porcentajeCambioTicketPromedio = porcentajeCambioTicketPromedio; }

    public Double getPorcentajeCambioClientesNuevos() { return porcentajeCambioClientesNuevos; }
    public void setPorcentajeCambioClientesNuevos(Double porcentajeCambioClientesNuevos) { this.porcentajeCambioClientesNuevos = porcentajeCambioClientesNuevos; }
}