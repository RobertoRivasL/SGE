package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de ventas
 * @author Roberto Rivas
 * @version 2.0
 */
public interface VentaServicio {

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Crear una nueva venta a partir de un DTO
     */
    Venta crearNuevaVenta(VentaDTO ventaDTO);

    /**
     * Obtener todas las ventas
     */
    List<Venta> obtenerTodasLasVentas();

    /**
     * Obtener venta por ID
     */
    Venta obtenerVentaPorId(Long id);

    // ==================== MÉTODOS DE BÚSQUEDA ====================

    /**
     * Buscar ventas por rango de fechas
     */
    List<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Buscar ventas por rango de fechas con paginación
     */
    Page<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    /**
     * Buscar ventas por cliente
     */
    List<Venta> buscarPorCliente(Long clienteId);

    /**
     * Buscar ventas por cliente (sobrecarga)
     */
    List<Venta> buscarPorCliente(Cliente cliente);

    /**
     * Buscar ventas por vendedor y fechas
     */
    List<Venta> buscarPorVendedorYFechas(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Buscar ventas para exportar con filtros
     */
    List<Venta> buscarVentasParaExportar(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                         String estado, String metodoPago, String vendedor);

    /**
     * Buscar ventas recientes por producto
     */
    List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite);

    /**
     * Buscar ventas recientes por cliente
     */
    List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite);

    // ==================== MÉTODOS CRUD ====================

    /**
     * Buscar venta por ID (Optional)
     */
    Optional<Venta> buscarPorId(Long id);

    /**
     * Listar todas las ventas (alias para compatibilidad)
     */
    List<Venta> listarTodas();

    /**
     * Guardar venta
     */
    Venta guardar(Venta venta);

    /**
     * Guardar venta desde DTO
     */
    Venta guardar(VentaDTO ventaDTO);

    /**
     * Actualizar venta
     */
    Venta actualizar(Venta venta);

    /**
     * Actualizar venta desde DTO
     */
    Venta actualizar(Long id, VentaDTO ventaDTO);

    /**
     * Eliminar venta
     */
    void eliminar(Long id);

    /**
     * Anular venta
     */
    Venta anular(Long id);

    /**
     * Duplicar venta
     */
    Venta duplicarVenta(Venta ventaOriginal);

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    /**
     * Convertir venta a DTO
     */
    VentaDTO convertirADTO(Venta venta);

    // ==================== MÉTODOS DE CÁLCULO Y ESTADÍSTICAS ====================

    /**
     * Contar transacciones en un rango de fechas
     */
    Long contarTransacciones(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Contar ventas por cliente
     */
    Long contarVentasPorCliente(Long clienteId);

    /**
     * Contar ventas de hoy
     */
    Long contarVentasHoy();

    /**
     * Calcular total de ventas por cliente
     */
    Double calcularTotalVentasPorCliente(Long clienteId);

    /**
     * Calcular total de ventas en un período
     */
    Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Calcular porcentaje de cambio
     */
    Double calcularPorcentajeCambio(Double actual, Double anterior);

    /**
     * Calcular ticket promedio
     */
    Double calcularTicketPromedio(LocalDateTime inicio, LocalDateTime fin);

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verificar si existen ventas por cliente
     */
    boolean existenVentasPorCliente(Long clienteId);

    /**
     * Verificar si existen ventas por producto
     */
    boolean existenVentasPorProducto(Long productoId);

    // ==================== MÉTODOS DE ARTÍCULOS Y PRODUCTOS ====================

    /**
     * Contar artículos vendidos en un período
     */
    Long contarArticulosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Contar unidades vendidas por producto
     */
    Long contarUnidadesVendidasPorProducto(Long productoId);

    /**
     * Calcular ingresos por producto
     */
    Double calcularIngresosPorProducto(Long productoId);
}