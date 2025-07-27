package informviva.gest.repository;

import informviva.gest.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto siguiendo el patrón Repository
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para acceso a datos de productos
 * - I: Interface segregada específica para productos
 * - D: Abstracción para el acceso a datos
 *
 * @author Tu nombre
 * @version 1.0
 */
@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por nombre (búsqueda parcial, insensible a mayúsculas)
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos por categoría (insensible a mayúsculas)
     *
     * @param categoria Categoría del producto
     * @return Lista de productos de la categoría
     */
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    /**
     * Busca productos dentro de un rango de precios
     *
     * @param precioMin Precio mínimo (inclusive)
     * @param precioMax Precio máximo (inclusive)
     * @return Lista de productos en el rango
     */
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Busca productos con stock menor al especificado
     *
     * @param stock Cantidad de stock límite
     * @return Lista de productos con stock bajo
     */
    List<Producto> findByStockLessThan(Integer stock);

    /**
     * Busca productos activos
     *
     * @return Lista de productos activos
     */
    List<Producto> findByActivoTrue();

    /**
     * Busca productos activos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de productos activos
     */
    Page<Producto> findByActivoTrue(Pageable pageable);

    /**
     * Busca productos por nombre y que estén activos
     *
     * @param nombre Nombre o parte del nombre
     * @return Lista de productos activos que coinciden
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Busca productos por categoría y que estén activos
     *
     * @param categoria Categoría del producto
     * @return Lista de productos activos de la categoría
     */
    List<Producto> findByCategoriaIgnoreCaseAndActivoTrue(String categoria);

    /**
     * Verifica si existe un producto con el nombre dado (excluyendo un ID específico)
     * Útil para validaciones de unicidad en actualizaciones
     *
     * @param nombre Nombre del producto
     * @param id ID a excluir de la búsqueda
     * @return true si existe otro producto con ese nombre
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /**
     * Verifica si existe un producto con el nombre dado
     *
     * @param nombre Nombre del producto
     * @return true si existe un producto con ese nombre
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca un producto por nombre exacto (insensible a mayúsculas)
     *
     * @param nombre Nombre exacto del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Cuenta productos por categoría
     *
     * @param categoria Categoría a contar
     * @return Número de productos en la categoría
     */
    long countByCategoriaIgnoreCase(String categoria);

    /**
     * Cuenta productos activos
     *
     * @return Número de productos activos
     */
    long countByActivoTrue();

    /**
     * Busca productos con stock igual a cero
     *
     * @return Lista de productos sin stock
     */
    List<Producto> findByStock(Integer stock);

    /**
     * Query personalizada para buscar productos por múltiples criterios
     *
     * @param nombre Nombre del producto (puede ser null)
     * @param categoria Categoría del producto (puede ser null)
     * @param precioMin Precio mínimo (puede ser null)
     * @param precioMax Precio máximo (puede ser null)
     * @param soloActivos Si solo buscar productos activos
     * @param pageable Información de paginación
     * @return Página de productos que cumplen los criterios
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:categoria IS NULL OR LOWER(p.categoria) = LOWER(:categoria)) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:soloActivos = false OR p.activo = true)")
    Page<Producto> buscarConCriterios(
            @Param("nombre") String nombre,
            @Param("categoria") String categoria,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            @Param("soloActivos") boolean soloActivos,
            Pageable pageable);

    /**
     * Query para obtener las categorías únicas de productos
     *
     * @return Lista de categorías distintas
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findDistinctCategorias();

    /**
     * Query para obtener productos más vendidos (requiere relación con ventas)
     *
     * @param limite Número máximo de productos a retornar
     * @return Lista de productos más vendidos
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.id DESC")
    List<Producto> findTopProductos(Pageable pageable);
}