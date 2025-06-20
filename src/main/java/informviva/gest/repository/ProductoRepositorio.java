package informviva.gest.repository;

import informviva.gest.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a las entidades Producto en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con productos.
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    /**
     * Verifica si existe un producto con el código especificado
     * @param codigo Código del producto
     * @return true si existe, false en caso contrario
     */


    /**
     * Busca un producto por su código
     *
     * @param codigo Código del producto
     * @return Producto encontrado o null si no existe
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Obtiene productos con stock menor al umbral especificado, ordenados por stock ascendente
     *
     * @param umbral Cantidad máxima de stock para considerar como bajo
     * @return Lista de productos con bajo stock
     */
    List<Producto> findByStockLessThanOrderByStockAsc(int umbral);

    /**
     * Busca productos por nombre que contenga el texto especificado (case insensitive)
     *
     * @param nombre Texto a buscar en el nombre
     * @return Lista de productos que coinciden
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Obtiene productos por categoría
     *
     * @param categoriaId ID de la categoría
     * @return Lista de productos de la categoría
     */
    List<Producto> findByCategoriaId(Long categoriaId);

    /**
     * Obtiene solo los productos activos
     *
     * @return Lista de productos activos
     */
    List<Producto> findByActivoTrue();

    /**
     * Obtiene productos inactivos
     *
     * @return Lista de productos inactivos
     */
    List<Producto> findByActivoFalse();

    /**
     * Busca productos por marca
     *
     * @param marca Marca del producto
     * @return Lista de productos de la marca
     */
    List<Producto> findByMarcaContainingIgnoreCase(String marca);

    /**
     * Busca productos por rango de precios
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos en el rango de precios
     */
    List<Producto> findByPrecioBetween(Double precioMin, Double precioMax);

    /**
     * Cuenta productos activos
     *
     * @return Número de productos activos
     */
    Long countByActivoTrue();

    /**
     * Cuenta productos inactivos
     *
     * @return Número de productos inactivos
     */
    Long countByActivoFalse();

    /**
     * Cuenta productos con stock menor al umbral
     *
     * @param umbral Umbral de stock
     * @return Número de productos con bajo stock
     */
    Long countByStockLessThan(int umbral);

    /**
     * Cuenta productos por categoría
     *
     * @param categoriaId ID de la categoría
     * @return Número de productos en la categoría
     */
    Long countByCategoriaId(Long categoriaId);

    /**
     * Obtiene productos ordenados por fecha de creación (más recientes primero)
     *
     * @return Lista de productos ordenados por fecha de creación descendente
     */
    List<Producto> findAllByOrderByFechaCreacionDesc();

    /**
     * Obtiene productos ordenados por stock ascendente
     *
     * @return Lista de productos ordenados por stock ascendente
     */
    List<Producto> findAllByOrderByStockAsc();

    /**
     * Busca productos con stock mayor a cero (disponibles)
     *
     * @return Lista de productos disponibles
     */
    @Query("SELECT p FROM Producto p WHERE p.stock > 0 AND p.activo = true")
    List<Producto> findProductosDisponibles();

    /**
     * Busca productos sin stock
     *
     * @return Lista de productos sin stock
     */
    @Query("SELECT p FROM Producto p WHERE p.stock = 0 OR p.stock IS NULL")
    List<Producto> findProductosSinStock();

    /**
     * Búsqueda combinada por nombre, código o marca
     *
     * @param texto Texto a buscar
     * @return Lista de productos que coinciden
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(p.marca) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Producto> buscarPorTexto(@Param("texto") String texto);

    /**
     * Obtiene productos más vendidos (requiere relación con VentaDetalle)
     *
     * @param limite Número máximo de productos a retornar
     * @return Lista de productos más vendidos
     */
    @Query("SELECT p FROM Producto p WHERE p.id IN " +
            "(SELECT vd.producto.id FROM VentaDetalle vd " +
            "GROUP BY vd.producto.id " +
            "ORDER BY SUM(vd.cantidad) DESC) " +
            "ORDER BY (SELECT SUM(vd2.cantidad) FROM VentaDetalle vd2 WHERE vd2.producto.id = p.id) DESC")
    List<Producto> findProductosMasVendidos(@Param("limite") int limite);

    @Query("SELECT DISTINCT c.nombre FROM Producto p JOIN p.categoria c WHERE c IS NOT NULL")
    List<String> obtenerCategorias();

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Query("SELECT p FROM Producto p WHERE UPPER(p.nombre) LIKE %:termino% OR UPPER(p.codigo) LIKE %:termino%")
    Page<Producto> buscarPorNombreOCodigo(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.categoria) LIKE LOWER(CONCAT('%', :categoria, '%'))")
    Page<Producto> buscarPorCategoria(@Param("categoria") String categoria, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.stock > 0")
    Page<Producto> listarConStock(Pageable pageable);

    Page<Producto> findByNombreContainingOrCodigoContainingIgnoreCase(
            String nombre, String codigo, Pageable pageable);

    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

    Page<Producto> findByStockGreaterThan(Integer stock, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Producto> findByStockLessThan(int stock, Pageable pageable);

    // En ProductoRepositorio
    Page<Producto> findByActivoFalse(Pageable pageable);

    // En ProductoRepositorio
    Page<Producto> findByActivoTrue(Pageable pageable);


}

