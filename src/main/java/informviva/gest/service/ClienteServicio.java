package informviva.gest.service;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Usuario;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    Optional<Cliente> buscarPorEmail(String email);

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
    Page<Venta> listarVentasPaginadas(Pageable pageable);

    List<Venta> listarTodasVentas();

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

    /**
     * Busca clientes por término de búsqueda con límite
     * @param termino Término a buscar en nombre, apellido, email, RUT
     * @param limite Número máximo de resultados
     * @return Lista de clientes que coinciden
     */
    List<Cliente> buscarPorTermino(String termino, int limite);

    /**
     * Busca clientes por término con paginación
     * @param termino Término a buscar
     * @param pageable Configuración de paginación
     * @return Página de clientes
     */
    Page<Cliente> buscarPorTermino(String termino, Pageable pageable);

    /**
     * Busca clientes por término y ciudad
     */
    Page<Cliente> buscarPorTerminoYCiudad(String termino, String ciudad, Pageable pageable);

    /**
     * Busca clientes por término y que estén activos
     */
    Page<Cliente> buscarPorTerminoYActivos(String termino, Pageable pageable);

    /**
     * Busca clientes por término, ciudad y activos
     */
    Page<Cliente> buscarPorTerminoYCiudadYActivos(String termino, String ciudad, Pageable pageable);

    /**
     * Busca clientes por ciudad
     */
    Page<Cliente> buscarPorCiudad(String ciudad, Pageable pageable);

    /**
     * Busca clientes por ciudad y activos
     */
    Page<Cliente> buscarPorCiudadYActivos(String ciudad, Pageable pageable);

// ========== MÉTODOS DE OBTENCIÓN ESPECÍFICA ==========

    /**
     * Obtiene solo clientes activos
     * @return Lista de clientes activos
     */
    List<Cliente> obtenerActivos();

    /**
     * Obtiene clientes activos con paginación
     * @param pageable Configuración de paginación
     * @return Página de clientes activos
     */
    Page<Cliente> obtenerActivosPaginados(Pageable pageable);

    /**
     * Lista todas las ciudades donde hay clientes
     * @return Lista de ciudades únicas
     */
    List<String> listarCiudades();

// ========== MÉTODOS DE CONTEO ==========

    /**
     * Cuenta clientes activos
     * @return Número de clientes activos
     */
    Long contarActivos();

    /**
     * Cuenta clientes inactivos
     * @return Número de clientes inactivos
     */
    Long contarInactivos();

    /**
     * Cuenta clientes nuevos hoy
     * @return Número de clientes registrados hoy
     */
    Long contarNuevosHoy();

    /**
     * Cuenta clientes nuevos este mes
     * @return Número de clientes registrados este mes
     */
    Long contarNuevosMes();

    /**
     * Cuenta el total de clientes
     * @return Número total de clientes
     */
    Long contarTodos();

    /**
     * Cuenta ciudades únicas con clientes
     * @return Número de ciudades diferentes
     */
    Long contarCiudades();

// ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * Verifica si existe un email en otro cliente
     * @param email Email a verificar
     * @param clienteId ID del cliente actual (para edición)
     * @return true si el email existe en otro cliente
     */
    boolean existeEmailOtroCliente(String email, Long clienteId);

    /**
     * Verifica si existe un email
     * @param email Email a verificar
     * @return true si el email existe
     */
    boolean existeEmail(String email);

    boolean existeClienteConEmailExcluyendo(String email, Long id);
    boolean existeClienteConRutExcluyendo(String rut, Long id);
    java.util.List<Cliente> buscarPorTermino(String termino);
    Cliente buscarPorRut(String rut);

    List<Cliente> buscarPorApellido(String apellido);
    List<Cliente> buscarPorEdadEntre(int edadMin, int edadMax);
    List<Cliente> buscarPorEstadoActivo(boolean activo);
    List<Cliente> buscarPorCategorias(List<String> categorias);

    List<Cliente> buscarPorTelefono(String telefono);
    List<Cliente> buscarPorDireccion(String direccion);
    List<Cliente> obtenerClientesRegistradosRecientemente(LocalDate fechaInicio);

    Page<Cliente> listarClientesPaginadas(Pageable pageable);
    List<Cliente> listarTodasClientes();
    }