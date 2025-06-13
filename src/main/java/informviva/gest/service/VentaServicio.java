package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.model.VentaDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz para la gestión de ventas
 *
 * @author Roberto Rivas
 * @version 3.0
 */
public interface VentaServicio {

    /**
     * Obtiene todas las ventas
     *
     * @return Lista de ventas
     */
    List<Venta> listarTodas();

    /**
     * Obtiene ventas paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de ventas
     */
    Page<Venta> listarPaginadas(Pageable pageable);

    /**
     * Busca una venta por su ID
     *
     * @param id ID de la venta
     * @return Venta encontrada o null si no existe
     */
    Venta buscarPorId(Long id);

    /**
     * Guarda una nueva venta
     *
     * @param ventaDTO DTO con los datos de la venta
     * @return Venta guardada
     */
    Venta guardar(VentaDTO ventaDTO);

    /**
     * Actualiza una venta existente
     *
     * @param id       ID de la venta
     * @param ventaDTO DTO con los nuevos datos
     * @return Venta actualizada
     */
    Venta actualizar(Long id, VentaDTO ventaDTO);

    /**
     * Elimina una venta
     *
     * @param id ID de la venta
     */
    void eliminar(Long id);

    /**
     * Anula una venta sin eliminarla
     *
     * @param id ID de la venta
     * @return Venta anulada
     */
    Venta anular(Long id);

    /**
     * Busca ventas por rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Lista de ventas en el rango
     */
    List<Venta> buscarPorRangoFechas(LocalDate inicio, LocalDate fin);

    /**
     * Busca ventas por rango de fechas con hora
     *
     * @param inicio Fecha y hora de inicio
     * @param fin    Fecha y hora de fin
     * @return Lista de ventas en el rango
     */
    List<Venta> buscarPorRangoFechasCompleto(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca ventas por cliente
     *
     * @param cliente Cliente
     * @return Lista de ventas del cliente
     */
    List<Venta> buscarPorCliente(Cliente cliente);

    /**
     * Busca ventas por vendedor
     *
     * @param vendedor Vendedor
     * @return Lista de ventas del vendedor
     */
    List<Venta> buscarPorVendedor(Usuario vendedor);

    /**
     * Verifica si existen ventas para un cliente
     *
     * @param clienteId ID del cliente
     * @return true si existen ventas, false en caso contrario
     */
    boolean existenVentasPorCliente(Long clienteId);

    /**
     * Verifica si existen ventas para un producto
     *
     * @param productoId ID del producto
     * @return true si existen ventas, false en caso contrario
     */
    boolean existenVentasPorProducto(Long productoId);

    /**
     * Convierte una entidad Venta a VentaDTO
     *
     * @param venta Entidad Venta
     * @return DTO de la venta
     */
    VentaDTO convertirADTO(Venta venta);

    /**
     * Obtiene el monto total de ventas en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Monto total de ventas
     */
    Double calcularTotalVentas(LocalDate inicio, LocalDate fin);

    /**
     * Obtiene el monto total de todas las ventas
     *
     * @return Monto total de ventas
     */
    Double calcularTotalVentas();

    /**
     * Obtiene el número total de transacciones en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Número total de transacciones
     */
    Long contarTransacciones(LocalDate inicio, LocalDate fin);

    /**
     * Obtiene el número total de artículos vendidos en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Número total de artículos vendidos
     */
    Long contarArticulosVendidos(LocalDate inicio, LocalDate fin);

    /**
     * Calcula el ticket promedio en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Ticket promedio
     */
    Double calcularTicketPromedio(LocalDate inicio, LocalDate fin);

    /**
     * Calcula el porcentaje de cambio entre dos períodos
     *
     * @param valorActual   Valor actual
     * @param valorAnterior Valor anterior
     * @return Porcentaje de cambio
     */
    Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior);

    /**
     * Obtiene el número de ventas realizadas hoy
     *
     * @return Número de ventas del día actual
     */
    Long contarVentasHoy();

    Long contarVentasPorCliente(Long clienteId);

    Double calcularTotalVentasPorCliente(Long clienteId);

    Long contarUnidadesVendidasPorProducto(Long productoId);

    Double calcularIngresosPorProducto(Long productoId);

    List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite);

    List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite);

    // ===== NUEVOS MÉTODOS PARA RESPALDO =====

    /**
     * Obtiene todos los detalles de venta de una venta específica
     *
     * @param ventaId ID de la venta
     * @return Lista de detalles de venta
     */
    List<VentaDetalle> obtenerDetallesPorVentaId(Long ventaId);

    /**
     * Obtiene todos los detalles de venta en un rango de fechas
     *
     * @param fechaInicio Fecha y hora de inicio
     * @param fechaFin    Fecha y hora de fin
     * @return Lista de detalles de venta
     */
    List<VentaDetalle> obtenerDetallesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Cuenta el total de detalles de venta en el sistema
     *
     * @return Número total de detalles
     */
    Long contarTotalDetalles();

    /**
     * Obtiene estadísticas de venta para un período específico
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Mapa con estadísticas (totalVentas, numeroVentas, ticketPromedio, etc.)
     */
    Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Exporta datos de ventas en formato específico
     *
     * @param formato     Formato de exportación (JSON, CSV, etc.)
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Datos exportados como String
     */
    String exportarDatos(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Importa datos de ventas desde formato específico
     *
     * @param datos   Datos a importar
     * @param formato Formato de los datos
     * @return Número de registros importados
     */
    int importarDatos(String datos, String formato);

    /**
     * Valida la integridad de las ventas en el sistema
     *
     * @return Mapa con resultados de validación
     */
    Map<String, Object> validarIntegridadDatos();


    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Busca ventas por rango de fechas con hora específica
     */
    List<Venta> buscarPorRangoFechasConHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Calcula el total de ventas en un rango de fechas y horas
     */
    Double calcularTotalVentasConHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta transacciones en un rango de fechas y horas
     */
    Long contarTransaccionesConHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta artículos vendidos en un rango de fechas y horas
     */
    Long contarArticulosVendidosConHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Calcula ticket promedio en un rango de fechas y horas
     */
    Double calcularTicketPromedioConHora(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene ventas de un cliente en un período específico con hora
     */
    List<Venta> buscarVentasPorClienteEnPeriodo(Long clienteId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene ventas de un vendedor en un período específico con hora
     */
    List<Venta> buscarVentasPorVendedorEnPeriodo(Long vendedorId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene las ventas más recientes hasta una fecha específica
     */
    List<Venta> obtenerVentasRecientes(LocalDateTime hastaFecha, int limite);

    /**
     * Calcula métricas de ventas por hora del día
     */
    Map<Integer, Double> obtenerVentasPorHoraDelDia(LocalDateTime fecha);

    /**
     * Obtiene el resumen de ventas para un período con granularidad por hora
     */
    List<Map<String, Object>> obtenerResumenVentasPorHora(LocalDateTime inicio, LocalDateTime fin);

}