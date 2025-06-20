package informviva.gest.repository;

import informviva.gest.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a las entidades Categoria en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con categorías.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

   // ===================== MÉTODOS DE BÚSQUEDA POR NOMBRE =====================

    /**
     * Verifica si existe una categoría con el nombre especificado
     *
     * @param nombre Nombre de la categoría
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre especificado excluyendo un ID específico
     *
     * @param nombre Nombre de la categoría
     * @param id     ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /**
     * Busca una categoría por su nombre exacto
     *
     * @param nombre Nombre de la categoría
     * @return Categoría encontrada o vacío si no existe
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Busca categorías por nombre que contenga el texto especificado (case insensitive)
     *
     * @param nombre Texto a buscar en el nombre
     * @return Lista de categorías que coinciden
     */
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    // ===================== MÉTODOS POR ESTADO ACTIVO =====================

    /**
     * Obtiene solo las categorías activas ordenadas por nombre
     *
     * @return Lista de categorías activas
     */
    @Query("SELECT c FROM Categoria c WHERE c.activa = true ORDER BY c.nombre ASC")
    List<Categoria> findByActivaTrue();

    /**
     * Obtiene categorías activas paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de categorías activas
     */
    @Query("SELECT c FROM Categoria c WHERE c.activa = true ORDER BY c.nombre ASC")
    Page<Categoria> findByActivaTrue(Pageable pageable);

    /**
     * Obtiene categorías inactivas
     *
     * @return Lista de categorías inactivas
     */
    @Query("SELECT c FROM Categoria c WHERE c.activa = false ORDER BY c.nombre ASC")
    List<Categoria> findByActivaFalse();

    /**
     * Obtiene todas las categorías ordenadas por nombre
     *
     * @return Lista de categorías ordenadas
     */
    @Query("SELECT c FROM Categoria c ORDER BY c.nombre ASC")
    List<Categoria> findAllByOrderByNombreAsc();

    // ===================== MÉTODOS DE CONTEO =====================

    /**
     * Cuenta las categorías activas
     *
     * @return Número de categorías activas
     */
    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.activa = true")
    Long countByActivaTrue();

    /**
     * Cuenta las categorías inactivas
     *
     * @return Número de categorías inactivas
     */
    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.activa = false")
    Long countByActivaFalse();

    // ===================== MÉTODOS PARA VALIDACIONES DE NEGOCIO =====================

    /**
     * Busca categorías que tienen productos asociados
     *
     * @return Lista de categorías con productos
     */
    @Query("SELECT DISTINCT c FROM Categoria c WHERE EXISTS (SELECT 1 FROM Producto p WHERE p.categoria = c)")
    List<Categoria> findCategoriasConProductos();

    /**
     * Busca categorías que NO tienen productos asociados
     *
     * @return Lista de categorías sin productos
     */
    @Query("SELECT c FROM Categoria c WHERE NOT EXISTS (SELECT 1 FROM Producto p WHERE p.categoria = c)")
    List<Categoria> findCategoriasSinProductos();

    /**
     * Verifica si una categoría tiene productos asociados
     *
     * @param categoriaId ID de la categoría
     * @return true si tiene productos, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE p.categoria.id = :categoriaId")
    boolean tieneProductosAsociados(@Param("categoriaId") Long categoriaId);

    // ===================== MÉTODOS DE BÚSQUEDA AVANZADA =====================

    /**
     * Busca categorías por texto en nombre o descripción
     *
     * @param texto Texto a buscar
     * @return Lista de categorías que coinciden
     */
    @Query("SELECT c FROM Categoria c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "ORDER BY c.nombre ASC")
    List<Categoria> buscarPorTexto(@Param("texto") String texto);

    /**
     * Busca categorías activas por texto en nombre o descripción
     *
     * @param texto Texto a buscar
     * @return Lista de categorías activas que coinciden
     */
    @Query("SELECT c FROM Categoria c WHERE c.activa = true AND " +
            "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "ORDER BY c.nombre ASC")
    List<Categoria> buscarActivasPorTexto(@Param("texto") String texto);

    // ===================== MÉTODOS PARA REPORTES =====================

    /**
     * Obtiene estadísticas de categorías por estado
     *
     * @return Array con [total, activas, inactivas]
     */
    @Query("SELECT " +
            "COUNT(c), " +
            "SUM(CASE WHEN c.activa = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.activa = false THEN 1 ELSE 0 END) " +
            "FROM Categoria c")
    Object[] obtenerEstadisticas();

    /**
     * Obtiene las categorías más utilizadas (con más productos)
     *
//     * @param limite Número máximo de resultados
     * @return Lista de arrays [categoria, cantidad_productos]
     */
    @Query("SELECT c, COUNT(p) as cantidad FROM Categoria c " +
            "LEFT JOIN Producto p ON p.categoria = c " +
            "GROUP BY c " +
            "ORDER BY cantidad DESC")
    List<Object[]> findCategoriasMasUtilizadas(Pageable pageable);

    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);


    // Consulta personalizada para las más utilizadas
    @Query("SELECT c FROM Categoria c JOIN Producto p ON p.categoria.id = c.id GROUP BY c.id ORDER BY COUNT(p.id) DESC")
    List<Categoria> findMasUtilizadas(Pageable pageable);

    default List<Categoria> findMasUtilizadas(int limite) {
        return findMasUtilizadas(PageRequest.of(0, limite));
    }
}
