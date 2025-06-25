package informviva.gest.service;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de clientes
 * Interfaz completa con todos los métodos requeridos por controladores
 *
 * @author Roberto Rivas
 * @version 4.1
 */
public interface ClienteServicio {

    // ========== MÉTODOS BÁSICOS CRUD ==========

    /**
     * Obtiene todos los clientes
     */
    List<Cliente> obtenerTodos();

    /**
     * Busca un cliente por su ID
     */
    Cliente buscarPorId(Long id);

    /**
     * Guarda un nuevo cliente o actualiza uno existente
     */
    Cliente guardar(Cliente cliente);

    /**
     * Elimina un cliente por su ID
     */
    void eliminar(Long id);

    // ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * Valida si un RUT tiene el formato correcto
     */
    boolean rutEsValido(String rut);

    /**
     * Verifica si existe un cliente con el email especificado
     */
    boolean existeClienteConEmail(String email);

    /**
     * Verifica si existe un cliente con el RUT especificado
     */
    boolean existeClienteConRut(String rut);

    /**
     * Verifica si existe un cliente con el RUT especificado (alias)
     */
    boolean existePorRut(String rut);

    /**
     * Verifica si existe un email en otro cliente (para edición)
     */
    boolean existeClienteConEmailExcluyendo(String email, Long id);

    /**
     * Verifica si existe un RUT en otro cliente (para edición)
     */
    boolean existeClienteConRutExcluyendo(String rut, Long id);

    // ===== MÉTODOS ADICIONALES PARA CONTROLADORES =====

    /**
     * Verifica si existe un email (alias para controladores)
     */
    boolean existeEmail(String email);

    /**
     * Verifica si existe email en otro cliente (alias para controladores)
     */
    boolean existeEmailOtroCliente(String email, Long id);

    // ========== MÉTODOS DE BÚSQUEDA ==========

    /**
     * Busca un cliente por su RUT
     */
    Cliente buscarPorRut(String rut);

    /**
     * Busca clientes por nombre
     */
    List<Cliente> buscarPorNombre(String nombre);

    /**
     * Busca clientes por apellido
     */
    List<Cliente> buscarPorApellido(String apellido);

    /**
     * Busca un cliente por su email
     */
    Optional<Cliente> buscarPorEmail(String email);

    /**
     * Busca clientes por teléfono
     */
    List<Cliente> buscarPorTelefono(String telefono);

    /**
     * Busca clientes por dirección
     */
    List<Cliente> buscarPorDireccion(String direccion);

    /**
     * Busca clientes por término general (nombre, apellido, email, RUT)
     */
    List<Cliente> buscarPorTermino(String termino);

    /**
     * Busca clientes por término con límite
     */
    List<Cliente> buscarPorTermino(String termino, int limite);

    /**
     * Busca clientes por término con paginación
     */
    Page<Cliente> buscarPorTermino(String termino, Pageable pageable);

    /**
     * Busca clientes por nombre o email con paginación
     */
    Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable);

    // ========== MÉTODOS POR CATEGORÍA Y ESTADO ==========

    /**
     * Obtiene clientes por categoría
     */
    List<Cliente> obtenerClientesPorCategoria(String categoria);

    /**
     * Busca clientes por múltiples categorías
     */
    List<Cliente> buscarPorCategorias(List<String> categorias);

    /**
     * Busca clientes por estado activo/inactivo
     */
    List<Cliente> buscarPorEstadoActivo(boolean activo);

    /**
     * Obtiene solo clientes activos
     */
    List<Cliente> obtenerActivos();

    /**
     * Obtiene clientes activos con paginación
     */
    Page<Cliente> obtenerActivosPaginados(Pageable pageable);

    // ========== MÉTODOS POR FECHAS ==========

    /**
     * Busca clientes por rango de fecha de registro
     */
    List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene clientes nuevos en un período
     */
    List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene clientes registrados recientemente
     */
    List<Cliente> obtenerClientesRegistradosRecientemente(LocalDate fechaInicio);

    /**
     * Busca clientes por rango de edad
     */
    List<Cliente> buscarPorEdadEntre(int edadMin, int edadMax);

    // ========== MÉTODOS DE PAGINACIÓN ==========

    /**
     * Obtiene todos los clientes con paginación
     */
    Page<Cliente> obtenerTodosPaginados(Pageable pageable);

    /**
     * Lista clientes con paginación
     */
    Page<Cliente> listarClientesPaginadas(Pageable pageable);

    // ========== MÉTODOS DE CONTEO ==========

    /**
     * Cuenta el total de clientes
     */
    Long contarTotalClientes();

    /**
     * Cuenta el total de clientes (alias para controladores)
     */
    Long contarTodos();

    /**
     * Cuenta clientes nuevos en un período
     */
    Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta clientes activos
     */
    Long contarClientesActivos();

    /**
     * Cuenta clientes activos (alias)
     */
    Long contarActivos();

    /**
     * Cuenta clientes inactivos
     */
    Long contarInactivos();

    /**
     * Cuenta clientes nuevos hoy
     */
    Long contarNuevosHoy();

    /**
     * Cuenta clientes nuevos este mes
     */
    Long contarNuevosMes();

    // ========== MÉTODOS DE REPORTES ==========

    /**
     * Obtiene clientes con compras en un período
     */
    List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene top clientes por compras
     */
    List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite);

    /**
     * Obtiene clientes sin ventas en un período
     */
    List<Cliente> obtenerClientesSinVentas(LocalDate fechaInicio, LocalDate fechaFin);

    // ========== MÉTODOS DE ANÁLISIS ==========

    /**
     * Calcula el promedio de compras por cliente
     */
    Double calcularPromedioComprasPorCliente();

    /**
     * Obtiene estadísticas de clientes para un período
     */
    Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    // ========== MÉTODOS DE BÚSQUEDA POR CIUDAD ==========

    /**
     * Lista todas las ciudades disponibles
     */
    List<String> listarCiudades();

    /**
     * Cuenta el número de ciudades
     */
    Long contarCiudades();

    /**
     * Busca clientes por ciudad con paginación
     */
    Page<Cliente> buscarPorCiudad(String ciudad, Pageable pageable);

    /**
     * Busca clientes activos por ciudad con paginación
     */
    Page<Cliente> buscarPorCiudadYActivos(String ciudad, Pageable pageable);

    /**
     * Busca clientes activos por término con paginación
     */
    Page<Cliente> buscarPorTerminoYActivos(String termino, Pageable pageable);

    /**
     * Busca clientes por término y ciudad con paginación
     */
    Page<Cliente> buscarPorTerminoYCiudad(String termino, String ciudad, Pageable pageable);

    /**
     * Busca clientes activos por término y ciudad con paginación
     */
    Page<Cliente> buscarPorTerminoYCiudadYActivos(String termino, String ciudad, Pageable pageable);

    // ========== MÉTODOS DE IMPORTACIÓN/EXPORTACIÓN ==========

    /**
     * Exporta datos de clientes en formato específico
     */
    String exportarDatos(String formato, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Exporta datos de clientes en formato específico (sobrecarga)
     */
    String exportarDatos(String formato, List<Cliente> clientes);

    /**
     * Importa datos de clientes desde formato específico
     */
    int importarDatos(String datos, String formato);

    /**
     * Valida la integridad de los datos de clientes
     */
    Map<String, Object> validarIntegridadDatos();

    // ========== MÉTODOS DE ACTUALIZACIÓN MASIVA ==========

    /**
     * Actualiza masivamente la categoría de clientes
     */
    int actualizarCategoriaEnLote(List<Long> clienteIds, String nuevaCategoria);
}