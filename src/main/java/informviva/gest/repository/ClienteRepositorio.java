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
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    // Métodos básicos
    boolean existsByEmail(String email);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByRut(String rut);

    // Búsquedas por nombre
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByApellidoContainingIgnoreCase(String apellido);

    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Cliente> buscarPorTexto(@Param("busqueda") String busqueda);

    // Búsquedas por fecha de registro
    List<Cliente> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin);

    List<Cliente> findByFechaRegistroAfter(LocalDate fecha);

    List<Cliente> findByFechaRegistroBefore(LocalDate fecha);

    // Búsquedas por categoría
    List<Cliente> findByCategoria(String categoria);

    List<Cliente> findByCategoriaIn(List<String> categorias);

    // Consultas de conteo
    long countByFechaRegistroBetween(LocalDate inicio, LocalDate fin);

    long countByCategoria(String categoria);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaRegistro >= :fecha")
    long contarClientesRegistradosDespuesDe(@Param("fecha") LocalDate fecha);

    // Consultas para reportes avanzados
    @Query("SELECT DISTINCT c.categoria FROM Cliente c WHERE c.categoria IS NOT NULL ORDER BY c.categoria")
    List<String> findAllCategorias();

    @Query("SELECT c FROM Cliente c WHERE c.fechaRegistro = :fecha ORDER BY c.fechaRegistro DESC")
    List<Cliente> findClientesRegistradosEnFecha(@Param("fecha") LocalDate fecha);

    // Consulta para obtener clientes con más actividad (que tienen ventas)
    @Query("SELECT DISTINCT c FROM Cliente c JOIN Venta v ON c.id = v.cliente.id " +
            "WHERE v.fecha BETWEEN :inicio AND :fin " +
            "ORDER BY c.nombre, c.apellido")
    List<Cliente> findClientesConVentasEnPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // Consulta para estadísticas
    @Query("SELECT AVG(COUNT(c)) FROM Cliente c GROUP BY c.categoria")
    Double obtenerPromedioClientesPorCategoria();

    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cliente> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    Page<Cliente> findByNombreContainingOrEmailContainingIgnoreCase(
            String nombre, String email, Pageable pageable);


    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Busca clientes registrados en un período con hora específica
     * Nota: Como fechaRegistro es LocalDate, convertimos para comparación
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "c.fechaRegistro >= :fechaInicio AND c.fechaRegistro <= :fechaFin " +
            "ORDER BY c.fechaRegistro DESC")
    List<Cliente> findByFechaRegistroBetweenDateTime(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Cuenta clientes registrados en un período con conversión de DateTime a Date
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE " +
            "c.fechaRegistro >= :fechaInicio AND c.fechaRegistro <= :fechaFin")
    Long countByFechaRegistroBetweenDateTime(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Obtiene clientes que no han tenido actividad desde una fecha específica
     * Requiere JOIN con tabla de ventas
     */
    @Query("SELECT c FROM Cliente c WHERE c.id NOT IN " +
            "(SELECT DISTINCT v.cliente.id FROM Venta v WHERE v.fecha >= :fechaUltimaActividad)")
    List<Cliente> findClientesInactivosDesde(@Param("fechaUltimaActividad") LocalDateTime fechaUltimaActividad);

    /**
     * Obtiene clientes registrados en las últimas N horas
     */
    @Query("SELECT c FROM Cliente c WHERE c.fechaRegistro >= :fechaLimite ORDER BY c.fechaRegistro DESC")
    List<Cliente> findClientesRegistradosEnUltimasHoras(@Param("fechaLimite") LocalDate fechaLimite);

}