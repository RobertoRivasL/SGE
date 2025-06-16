package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz para el servicio de Ventas.
 * Define los métodos que el controlador puede invocar.
 */
public interface VentaServicio {

    Venta crearNuevaVenta(VentaDTO ventaDTO);

    List<Venta> obtenerTodasLasVentas();

    Venta obtenerVentaPorId(Long id);

    Long contarTransacciones(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Long contarVentasPorCliente(Long clienteId);

    Double calcularTotalVentasPorCliente(Long clienteId);

    Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin);

    Double calcularPorcentajeCambio(Double actual, Double anterior);

    /**
     * Verifica si existen ventas para un cliente
     */
    boolean existenVentasPorCliente(Long clienteId);

    /**
     * Verifica si existen ventas para un producto
     */
    boolean existenVentasPorProducto(Long productoId);

    /**
     * Cuenta unidades vendidas por producto
     */
    Long contarUnidadesVendidasPorProducto(Long productoId);

    /**
     * Calcula ingresos por producto
     */
    Double calcularIngresosPorProducto(Long productoId);

    /**
     * Busca ventas recientes por producto
     */
    List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite);

    /**
     * Busca ventas recientes por cliente
     */
    List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite);

    /**
     * Busca ventas por cliente
     */
    List<Venta> buscarPorCliente(Cliente cliente);

    /**
     * MIGRAR: Calcula ticket promedio - USAR LocalDateTime
     */
    Double calcularTicketPromedio(LocalDateTime inicio, LocalDateTime fin);

    /**
     * MIGRAR: Cuenta artículos vendidos - USAR LocalDateTime
     */
    Long contarArticulosVendidos(LocalDateTime inicio, LocalDateTime fin);

    /**
     * NUEVO: Métodos de compatibilidad que reciben LocalDate y convierten
     */
    default Double calcularTicketPromedio(LocalDate inicio, LocalDate fin) {
        return calcularTicketPromedio(inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    default Long contarArticulosVendidos(LocalDate inicio, LocalDate fin) {
        return contarArticulosVendidos(inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    default Double calcularTotalVentas(LocalDate inicio, LocalDate fin) {
        return calcularTotalVentas(inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    default Long contarTransacciones(LocalDate inicio, LocalDate fin) {
        return contarTransacciones(inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

}