package informviva.gest.repository;

import informviva.gest.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de clientes
 * Proporciona métodos de acceso a datos optimizados y consultas personalizadas
 *
 * @author Roberto Rivas
 * @version 3.1
 */
@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    // ================================
    // MÉTODOS BÁSICOS DE BÚSQUEDA
    // ================================

    /**
     * Verifica si existe un cliente con el email especificado
     */
    boolean existsByEmail(String email);

    /**
     * Busca un cliente por email
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca un cliente por RUT
     */
    Optional<Cliente> findByRut(String rut);

    /**
     * Verifica si existe un cliente con el RUT especificado
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE c.rut = :rut")
    boolean existsByRut(@Param("rut") String rut);

    // ================================
    // BÚSQUEDAS POR NOMBRE
    // ================================

    /**
     * Busca clientes por nombre (case insensitive)
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca clientes por apellido (case insensitive)
     */
    List<Cliente> findByApellidoContainingIgnoreCase(String apellido);

    /**
     * Búsqueda de texto libre en nombre, apellido y email
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Cliente> buscarPorTexto(@Param("busqueda") String busqueda);

    /**
     * Búsqueda de texto libre con paginación
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cliente> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    // ================================
    // BÚSQUEDAS POR FECHA DE REGISTRO
    // ================================

    /**
     * Busca clientes registrados entre dos fechas
     */
    List<Cliente> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin);

    /**
     * Busca clientes registrados entre dos fechas con paginación
     */
    Page<Cliente> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin, Pageable pageable);

    /**
     * Busca clientes registrados después de una fecha
     */
    List<Cliente> findByFechaRegistroAfter(LocalDate fecha);

    /**
     * Busca clientes registrados antes de una fecha
     */
    List<Cliente> findByFechaRegistroBefore(LocalDate fecha);

    /**
     * Busca clientes registrados en una fecha específica
     */
    List<Cliente> findByFechaRegistro(LocalDate fecha);

    // ================================
    // BÚSQUEDAS POR CATEGORÍA
    // ================================

    /**
     * Busca clientes por categoría
     */
    List<Cliente> findByCategoria(String categoria);

    /**
     * Busca clientes por categoría con paginación
     */
    Page<Cliente> findByCategoria(String categoria, Pageable pageable);

    /**
     * Busca clientes sin categoría asignada
     */
    @Query("SELECT c FROM Cliente c WHERE c.categoria IS NULL OR c.categoria = ''")
    List<Cliente> findClientesSinCategoria();

    /**
     * Obtiene todas las categorías distintas
     */
    @Query("SELECT DISTINCT c.categoria FROM Cliente c WHERE c.categoria IS NOT NULL AND c.categoria != '' ORDER BY c.categoria")
    List<String> findCategoriasDistintas();

    // ================================
    // CONSULTAS DE ESTADÍSTICAS
    // ================================

    /**
     * Cuenta clientes por categoría
     */
    @Query("SELECT c.categoria, COUNT(c) FROM Cliente c GROUP BY c.categoria")
    List<Object[]> contarPorCategoria();

    /**
     * Cuenta clientes registrados por mes en un año específico
     */
    @Query("SELECT MONTH(c.fechaRegistro), COUNT(c) FROM Cliente c " +
            "WHERE YEAR(c.fechaRegistro) = :año " +
            "GROUP BY MONTH(c.fechaRegistro) " +
            "ORDER BY MONTH(c.fechaRegistro)")
    List<Object[]> contarPorMesEnAño(@Param("año") int año);

    /**
     * Obtiene clientes registrados hoy
     */
    @Query("SELECT c FROM Cliente c WHERE DATE(c.fechaRegistro) = CURRENT_DATE")
    List<Cliente> findClientesRegistradosHoy();

    /**
     * Cuenta clientes registrados hoy
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE DATE(c.fechaRegistro) = CURRENT_DATE")
    Long contarClientesRegistradosHoy();

    /**
     * Obtiene clientes más antiguos (primeros registrados)
     */
    List<Cliente> findTop10ByOrderByFechaRegistroAsc();

    /**
     * Obtiene clientes más recientes
     */
    List<Cliente> findTop10ByOrderByFechaRegistroDesc();

    // ================================
    // CONSULTAS AVANZADAS CON JOINS
    // ================================

    /**
     * Busca clientes que tienen ventas registradas
     */
    @Query("SELECT DISTINCT c FROM Cliente c INNER JOIN Venta v ON c.id = v.cliente.id")
    List<Cliente> findClientesConVentas();

    /**
     * Busca clientes que NO tienen ventas registradas
     */
    @Query("SELECT c FROM Cliente c WHERE c.id NOT IN (SELECT DISTINCT v.cliente.id FROM Venta v)")
    List<Cliente> findClientesSinVentas();

    /**
     * Busca clientes con ventas en un rango de fechas
     */
    @Query("SELECT DISTINCT c FROM Cliente c INNER JOIN Venta v ON c.id = v.cliente.id " +
            "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Cliente> findClientesConVentasEnPeriodo(@Param("fechaInicio") LocalDate fechaInicio,
                                                 @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca clientes con más de X compras
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "(SELECT COUNT(v) FROM Venta v WHERE v.cliente.id = c.id) > :minimoCompras")
    List<Cliente> findClientesConMasDeXCompras(@Param("minimoCompras") int minimoCompras);

    /**
     * Busca clientes por monto total gastado mayor a X
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "(SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.cliente.id = c.id) > :montoMinimo")
    List<Cliente> findClientesPorMontoGastado(@Param("montoMinimo") Double montoMinimo);

    // ================================
    // CONSULTAS PARA REPORTES
    // ================================

    /**
     * Obtiene clientes con estadísticas de compras
     */
    @Query("SELECT c, " +
            "COALESCE(COUNT(v), 0) as numeroCompras, " +
            "COALESCE(SUM(v.total), 0) as montoTotal, " +
            "COALESCE(AVG(v.total), 0) as promedioCompra, " +
            "MAX(v.fecha) as ultimaCompra " +
            "FROM Cliente c LEFT JOIN Venta v ON c.id = v.cliente.id " +
            "GROUP BY c.id, c.nombre, c.apellido, c.email, c.rut, c.telefono, c.direccion, c.fechaRegistro, c.categoria " +
            "ORDER BY montoTotal DESC")
    List<Object[]> findClientesConEstadisticasCompras();

    /**
     * Obtiene top clientes por monto gastado
     */
    @Query("SELECT c, SUM(v.total) as montoTotal " +
            "FROM Cliente c INNER JOIN Venta v ON c.id = v.cliente.id " +
            "GROUP BY c.id " +
            "ORDER BY montoTotal DESC")
    Page<Object[]> findTopClientesPorMonto(Pageable pageable);

    /**
     * Obtiene clientes inactivos (sin compras en X días)
     */
    @Query("SELECT c FROM Cliente c WHERE c.id NOT IN (" +
            "SELECT DISTINCT v.cliente.id FROM Venta v " +
            "WHERE v.fecha > :fechaLimite)")
    List<Cliente> findClientesInactivosDesde(@Param("fechaLimite") LocalDate fechaLimite);

    // ================================
    // VALIDACIONES DE INTEGRIDAD
    // ================================

    /**
     * Verifica si un cliente tiene ventas asociadas
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Venta v WHERE v.cliente.id = :clienteId")
    boolean existenVentasParaCliente(@Param("clienteId") Long clienteId);

    /**
     * Cuenta las ventas de un cliente
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.cliente.id = :clienteId")
    Long contarVentasDeCliente(@Param("clienteId") Long clienteId);

    /**
     * Obtiene el monto total gastado por un cliente
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.cliente.id = :clienteId")
    Double calcularMontoTotalCliente(@Param("clienteId") Long clienteId);

    // ================================
    // BÚSQUEDAS OPTIMIZADAS CON PROYECCIONES
    // ================================

    /**
     * Proyección para listados optimizados
     */
    interface ClienteListProjection {
        Long getId();
        String getRut();
        String getNombre();
        String getApellido();
        String getEmail();
        String getCategoria();
        LocalDate getFechaRegistro();

        // Método calculado
        default String getNombreCompleto() {
            return getNombre() + " " + getApellido();
        }
    }

    /**
     * Obtiene lista optimizada de clientes para vistas
     */
    @Query("SELECT c.id as id, c.rut as rut, c.nombre as nombre, c.apellido as apellido, " +
            "c.email as email, c.categoria as categoria, c.fechaRegistro as fechaRegistro " +
            "FROM Cliente c ORDER BY c.nombre, c.apellido")
    Page<ClienteListProjection> findAllProjected(Pageable pageable);

    /**
     * Búsqueda optimizada con proyección
     */
    @Query("SELECT c.id as id, c.rut as rut, c.nombre as nombre, c.apellido as apellido, " +
            "c.email as email, c.categoria as categoria, c.fechaRegistro as fechaRegistro " +
            "FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "ORDER BY c.nombre, c.apellido")
    Page<ClienteListProjection> buscarPorTextoProjected(@Param("busqueda") String busqueda, Pageable pageable);

    // ================================
    // MÉTODOS DE UTILIDAD
    // ================================

    /**
     * Busca clientes por múltiples RUTs
     */
    @Query("SELECT c FROM Cliente c WHERE c.rut IN :ruts")
    List<Cliente> findByRutIn(@Param("ruts") List<String> ruts);

    /**
     * Busca clientes por múltiples emails
     */
    @Query("SELECT c FROM Cliente c WHERE c.email IN :emails")
    List<Cliente> findByEmailIn(@Param("emails") List<String> emails);

    /**
     * Busca clientes por múltiples IDs
     */
    @Query("SELECT c FROM Cliente c WHERE c.id IN :ids")
    List<Cliente> findByIdIn(@Param("ids") List<Long> ids);

    /**
     * Actualiza la categoría de múltiples clientes
     */
    @Query("UPDATE Cliente c SET c.categoria = :categoria WHERE c.id IN :ids")
    int updateCategoriaForIds(@Param("categoria") String categoria, @Param("ids") List<Long> ids);

    // Métodos por estado activo
    Page<Cliente> findByActivoTrue(Pageable pageable);
    List<Cliente> findByActivoTrue();
    Long countByActivoTrue();
    Long countByActivoFalse();

    // Métodos por ciudad
    Page<Cliente> findByCiudadIgnoreCase(String ciudad, Pageable pageable);
    Page<Cliente> findByCiudadIgnoreCaseAndActivoTrue(String ciudad, Pageable pageable);

    // Métodos por fecha de registro
    Long countByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    // Métodos por email
    List<Cliente> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    // Métodos de búsqueda combinada
    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRutContaining(
            String nombre, String apellido, String email, String rut, Pageable pageable);

    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCase(
            String nombre, String apellido, String email, String ciudad, Pageable pageable);

    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActivoTrue(
            String nombre, String apellido, String email, Pageable pageable);

    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCaseAndActivoTrue(
            String nombre, String apellido, String email, String ciudad, Pageable pageable);
}