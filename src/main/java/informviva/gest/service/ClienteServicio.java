package informviva.gest.service;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Usuario;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la gestión de clientes
 *
 * @author Roberto Rivas
 * @version 3.0
 */
public interface ClienteServicio {

    // Métodos básicos CRUD
    List<Cliente> obtenerTodos();

    Cliente buscarPorId(Long id);

    Cliente guardar(Cliente cliente);

    void eliminar(Long id);

    boolean rutEsValido(String rut);

    // Métodos para reportes
    List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin);

    List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    List<Cliente> obtenerClientesPorCategoria(String categoria);

    List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite);

    // Métodos de análisis
    Double calcularPromedioComprasPorCliente();

    Long contarClientesActivos();

    List<Cliente> buscarPorNombre(String nombre);

    List<Cliente> buscarPorEmail(String email);

    boolean existeClienteConEmail(String email);

    boolean existeClienteConRut(String rut);

    Page<Cliente> obtenerTodosPaginados(Pageable pageable);

    Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable);

    // ===== NUEVOS MÉTODOS PARA RESPALDO =====

    /**
     * Busca clientes por rango de fecha de registro
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Lista de clientes registrados en el período
     */
    List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Exporta datos de clientes en formato específico
     *
     * @param formato     Formato de exportación (JSON, CSV, etc.)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin    Fecha de fin (opcional)
     * @return Datos exportados como String
     */
    String exportarDatos(String formato, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Importa datos de clientes desde formato específico
     *
     * @param datos   Datos a importar
     * @param formato Formato de los datos
     * @return Número de registros importados
     */
    int importarDatos(String datos, String formato);

    /**
     * Valida la integridad de los datos de clientes
     *
     * @return Mapa con resultados de validación
     */
    Map<String, Object> validarIntegridadDatos();

    /**
     * Obtiene estadísticas de clientes para un período
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Mapa con estadísticas
     */
    Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta el total de clientes en el sistema
     *
     * @return Número total de clientes
     */
    Long contarTotalClientes();

    /**
     * Obtiene clientes sin ventas en un período
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Lista de clientes sin ventas
     */
    List<Cliente> obtenerClientesSinVentas(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Actualiza masivamente la categoría de clientes
     *
     * @param clienteIds     Lista de IDs de clientes
     * @param nuevaCategoria Nueva categoría a asignar
     * @return Número de clientes actualizados
     */
    int actualizarCategoriaEnLote(List<Long> clienteIds, String nuevaCategoria);


    // Métodos existentes mantienen compatibilidad con LocalDate
    List<Venta> listarTodas();

    Page<Venta> listarPaginadas(Pageable pageable);

    Venta guardar(VentaDTO ventaDTO);

    Venta actualizar(Long id, VentaDTO ventaDTO);

    Venta anular(Long id);

    // Métodos existentes con LocalDate (mantener para compatibilidad)
    List<Venta> buscarPorRangoFechas(LocalDate inicio, LocalDate fin);

    Double calcularTotalVentas(LocalDate inicio, LocalDate fin);

    Long contarTransacciones(LocalDate inicio, LocalDate fin);

    Long contarArticulosVendidos(LocalDate inicio, LocalDate fin);

    Double calcularTicketPromedio(LocalDate inicio, LocalDate fin);

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

    // Métodos existentes sin cambios
    List<Venta> buscarPorCliente(Cliente cliente);

    List<Venta> buscarPorVendedor(Usuario vendedor);

    boolean existenVentasPorCliente(Long clienteId);

    boolean existenVentasPorProducto(Long productoId);

    VentaDTO convertirADTO(Venta venta);

    Double calcularTotalVentas();

    Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior);

    Long contarVentasHoy();

    Long contarVentasPorCliente(Long clienteId);

    Double calcularTotalVentasPorCliente(Long clienteId);

    Long contarUnidadesVendidasPorProducto(Long productoId);

    Double calcularIngresosPorProducto(Long productoId);

    List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite);

    List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite);
}