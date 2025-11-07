package informviva.gest.repository;

import informviva.gest.model.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Proveedor siguiendo el patrón Repository
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para acceso a datos de proveedores
 * - I: Interface segregada específica para proveedores
 * - D: Abstracción para el acceso a datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {

    // ===== BÚSQUEDAS BÁSICAS =====

    /**
     * Busca proveedores activos
     *
     * @return Lista de proveedores activos
     */
    List<Proveedor> findByActivoTrue();

    /**
     * Busca proveedores activos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de proveedores activos
     */
    Page<Proveedor> findByActivoTrue(Pageable pageable);

    /**
     * Busca proveedores inactivos
     *
     * @return Lista de proveedores inactivos
     */
    List<Proveedor> findByActivoFalse();

    /**
     * Busca proveedores inactivos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de proveedores inactivos
     */
    Page<Proveedor> findByActivoFalse(Pageable pageable);

    // ===== BÚSQUEDAS POR NOMBRE =====

    /**
     * Busca proveedores por nombre (búsqueda parcial, insensible a mayúsculas)
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de proveedores que coinciden
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca proveedores por nombre con paginación
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @param pageable Información de paginación
     * @return Página de proveedores que coinciden
     */
    Page<Proveedor> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Busca proveedores por nombre y que estén activos
     *
     * @param nombre Nombre o parte del nombre
     * @return Lista de proveedores activos que coinciden
     */
    List<Proveedor> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Busca proveedores por nombre y que estén activos con paginación
     *
     * @param nombre Nombre o parte del nombre
     * @param pageable Información de paginación
     * @return Página de proveedores activos que coinciden
     */
    Page<Proveedor> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    // ===== BÚSQUEDAS POR RUT =====

    /**
     * Busca un proveedor por RUT
     *
     * @param rut RUT del proveedor
     * @return Optional con el proveedor si existe
     */
    Optional<Proveedor> findByRut(String rut);

    /**
     * Verifica si existe un proveedor con el RUT dado
     *
     * @param rut RUT del proveedor
     * @return true si existe un proveedor con ese RUT
     */
    boolean existsByRut(String rut);

    /**
     * Verifica si existe un proveedor con el RUT dado, excluyendo un ID específico
     * Útil para validaciones de unicidad en actualizaciones
     *
     * @param rut RUT del proveedor
     * @param id ID a excluir de la búsqueda
     * @return true si existe otro proveedor con ese RUT
     */
    boolean existsByRutAndIdNot(String rut, Long id);

    // ===== BÚSQUEDAS POR CATEGORÍA =====

    /**
     * Busca proveedores por categoría
     *
     * @param categoria Categoría del proveedor
     * @return Lista de proveedores de la categoría
     */
    List<Proveedor> findByCategoria(String categoria);

    /**
     * Busca proveedores por categoría con paginación
     *
     * @param categoria Categoría del proveedor
     * @param pageable Información de paginación
     * @return Página de proveedores de la categoría
     */
    Page<Proveedor> findByCategoria(String categoria, Pageable pageable);

    /**
     * Busca proveedores por categoría y que estén activos
     *
     * @param categoria Categoría del proveedor
     * @return Lista de proveedores activos de la categoría
     */
    List<Proveedor> findByCategoriaAndActivoTrue(String categoria);

    // ===== BÚSQUEDAS POR UBICACIÓN =====

    /**
     * Busca proveedores por ciudad
     *
     * @param ciudad Ciudad del proveedor
     * @param pageable Información de paginación
     * @return Página de proveedores de la ciudad
     */
    Page<Proveedor> findByCiudadIgnoreCase(String ciudad, Pageable pageable);

    /**
     * Busca proveedores por región
     *
     * @param region Región del proveedor
     * @param pageable Información de paginación
     * @return Página de proveedores de la región
     */
    Page<Proveedor> findByRegionIgnoreCase(String region, Pageable pageable);

    // ===== BÚSQUEDAS POR CALIFICACIÓN =====

    /**
     * Busca proveedores con calificación mayor o igual a la especificada
     *
     * @param calificacion Calificación mínima
     * @param pageable Información de paginación
     * @return Página de proveedores con calificación suficiente
     */
    Page<Proveedor> findByCalificacionGreaterThanEqual(Integer calificacion, Pageable pageable);

    /**
     * Busca proveedores activos ordenados por calificación descendente
     *
     * @param pageable Información de paginación
     * @return Página de proveedores ordenados por calificación
     */
    Page<Proveedor> findByActivoTrueOrderByCalificacionDesc(Pageable pageable);

    // ===== CONTADORES =====

    /**
     * Cuenta proveedores activos
     *
     * @return Número de proveedores activos
     */
    long countByActivoTrue();

    /**
     * Cuenta proveedores inactivos
     *
     * @return Número de proveedores inactivos
     */
    long countByActivoFalse();

    /**
     * Cuenta proveedores por categoría
     *
     * @param categoria Categoría a contar
     * @return Número de proveedores en la categoría
     */
    long countByCategoria(String categoria);

    // ===== QUERIES PERSONALIZADAS =====

    /**
     * Búsqueda de texto libre en nombre, RUT, email, contacto
     *
     * @param termino Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de proveedores que coinciden
     */
    @Query("SELECT p FROM Proveedor p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.nombreFantasia) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.rut) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.contactoNombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Proveedor> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    /**
     * Busca proveedores con criterios múltiples
     *
     * @param nombre Nombre del proveedor (puede ser null)
     * @param categoria Categoría del proveedor (puede ser null)
     * @param ciudad Ciudad del proveedor (puede ser null)
     * @param soloActivos Si solo buscar proveedores activos
     * @param pageable Información de paginación
     * @return Página de proveedores que cumplen los criterios
     */
    @Query("SELECT p FROM Proveedor p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:ciudad IS NULL OR LOWER(p.ciudad) = LOWER(:ciudad)) AND " +
            "(:soloActivos = false OR p.activo = true)")
    Page<Proveedor> buscarConCriterios(
            @Param("nombre") String nombre,
            @Param("categoria") String categoria,
            @Param("ciudad") String ciudad,
            @Param("soloActivos") boolean soloActivos,
            Pageable pageable);

    /**
     * Obtiene proveedores con sus estadísticas de órdenes
     *
     * @return Lista de objetos con proveedor y estadísticas
     */
    @Query("SELECT p, " +
            "COALESCE(COUNT(o), 0) as numeroOrdenes, " +
            "COALESCE(SUM(o.total), 0) as montoTotal, " +
            "MAX(o.fechaOrden) as ultimaOrden " +
            "FROM Proveedor p LEFT JOIN OrdenCompra o ON p.id = o.proveedor.id " +
            "WHERE p.activo = true " +
            "GROUP BY p.id " +
            "ORDER BY montoTotal DESC")
    List<Object[]> findProveedoresConEstadisticas();

    /**
     * Busca proveedores sin órdenes de compra
     *
     * @return Lista de proveedores sin órdenes
     */
    @Query("SELECT p FROM Proveedor p WHERE p.id NOT IN " +
            "(SELECT DISTINCT o.proveedor.id FROM OrdenCompra o)")
    List<Proveedor> findProveedoresSinOrdenes();

    /**
     * Busca proveedores con órdenes en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de proveedores con órdenes en el período
     */
    @Query("SELECT DISTINCT p FROM Proveedor p " +
            "INNER JOIN OrdenCompra o ON p.id = o.proveedor.id " +
            "WHERE o.fechaOrden BETWEEN :fechaInicio AND :fechaFin")
    List<Proveedor> findProveedoresConOrdenesEnPeriodo(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);

    /**
     * Obtiene las categorías únicas de proveedores
     *
     * @return Lista de categorías distintas
     */
    @Query("SELECT DISTINCT p.categoria FROM Proveedor p " +
            "WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findDistinctCategorias();

    /**
     * Cuenta proveedores que ofrecen crédito
     *
     * @return Número de proveedores con días de crédito > 0
     */
    @Query("SELECT COUNT(p) FROM Proveedor p WHERE p.diasCredito > 0 AND p.activo = true")
    Long countProveedoresConCredito();

    /**
     * Busca proveedores con más de X días de crédito
     *
     * @param dias Días mínimos de crédito
     * @return Lista de proveedores que cumplen
     */
    @Query("SELECT p FROM Proveedor p WHERE p.diasCredito >= :dias AND p.activo = true")
    List<Proveedor> findProveedoresConCreditoMayorA(@Param("dias") Integer dias);

    /**
     * Obtiene top proveedores por número de órdenes
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de proveedores ordenados por número de órdenes
     */
    @Query("SELECT p, COUNT(o) as numOrdenes FROM Proveedor p " +
            "LEFT JOIN OrdenCompra o ON p.id = o.proveedor.id " +
            "WHERE p.activo = true " +
            "GROUP BY p.id " +
            "ORDER BY numOrdenes DESC")
    List<Object[]> findTopProveedoresPorOrdenes(Pageable pageable);

    /**
     * Obtiene top proveedores por monto total
     *
     * @param pageable Información de paginación (para limitar resultados)
     * @return Lista de proveedores ordenados por monto
     */
    @Query("SELECT p, COALESCE(SUM(o.total), 0) as montoTotal FROM Proveedor p " +
            "LEFT JOIN OrdenCompra o ON p.id = o.proveedor.id " +
            "WHERE p.activo = true " +
            "GROUP BY p.id " +
            "ORDER BY montoTotal DESC")
    List<Object[]> findTopProveedoresPorMonto(Pageable pageable);

    /**
     * Busca proveedores con contacto completo
     *
     * @return Lista de proveedores con datos de contacto
     */
    @Query("SELECT p FROM Proveedor p WHERE " +
            "p.contactoNombre IS NOT NULL AND p.contactoNombre <> '' AND " +
            "(p.contactoEmail IS NOT NULL OR p.contactoTelefono IS NOT NULL)")
    List<Proveedor> findProveedoresConContactoCompleto();

    /**
     * Verifica si un proveedor tiene órdenes de compra asociadas
     *
     * @param proveedorId ID del proveedor
     * @return true si tiene órdenes asociadas
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM OrdenCompra o WHERE o.proveedor.id = :proveedorId")
    boolean tieneOrdenesCompra(@Param("proveedorId") Long proveedorId);

    /**
     * Cuenta las órdenes de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @return Número de órdenes del proveedor
     */
    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.proveedor.id = :proveedorId")
    Long contarOrdenesProveedor(@Param("proveedorId") Long proveedorId);

    /**
     * Calcula el monto total de compras a un proveedor
     *
     * @param proveedorId ID del proveedor
     * @return Monto total de compras
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o " +
            "WHERE o.proveedor.id = :proveedorId AND o.estado <> 'CANCELADA'")
    java.math.BigDecimal calcularMontoTotalProveedor(@Param("proveedorId") Long proveedorId);
}
