package informviva.gest.repository;


import informviva.gest.model.Categoria;
import org.springframework.data.domain.Page;
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
 * @version 2.0
 */
@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

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
     * Busca una categoría por su nombre
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

    /**
     * Obtiene solo las categorías activas
     *
     * @return Lista de categorías activas
     */
    List<Categoria> findByActivaTrue();

    /**
     * Obtiene categorías inactivas
     *
     * @return Lista de categorías inactivas
     */
    List<Categoria> findByActivaFalse();

    /**
     * Obtiene categorías activas paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de categorías activas
     */
    Page<Categoria> findByActivaTrue(Pageable pageable);

    /**
     * Busca categorías por nombre con paginación
     *
     * @param nombre   Texto a buscar en el nombre
     * @param pageable Configuración de paginación
     * @return Página de categorías que coinciden
     */
    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Cuenta categorías activas
     *
     * @return Número de categorías activas
     */
    Long countByActivaTrue();

    /**
     * Cuenta categorías inactivas
     *
     * @return Número de categorías inactivas
     */
    Long countByActivaFalse();

    /**
     * Obtiene categorías ordenadas por nombre ascendente
     *
     * @return Lista de categorías ordenadas por nombre
     */
    List<Categoria> findAllByOrderByNombreAsc();

    /**
     * Busca categorías por descripción que contenga el texto especificado
     *
     * @param descripcion Texto a buscar en la descripción
     * @return Lista de categorías que coinciden
     */
    List<Categoria> findByDescripcionContainingIgnoreCase(String descripcion);

    // CategoriaRepositorio.java
    Optional<Categoria> findFirstByNombreContainingIgnoreCase(String nombre);

    /**
     * Búsqueda combinada por nombre o descripción
     *
     * @param texto Texto a buscar
     * @return Lista de categorías que coinciden
     */
    @Query("SELECT c FROM Categoria c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Categoria> buscarPorTexto(@Param("texto") String texto);

    /**
     * Búsqueda combinada por nombre o descripción con paginación
     *
     * @param texto    Texto a buscar
     * @param pageable Configuración de paginación
     * @return Página de categorías que coinciden
     */
    @Query("SELECT c FROM Categoria c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    Page<Categoria> buscarPorTexto(@Param("texto") String texto, Pageable pageable);

    /**
     * Obtiene categorías más utilizadas (requiere relación con Producto)
     *
     * @param limite Número máximo de categorías a retornar
     * @return Lista de categorías más utilizadas
     */
    @Query("SELECT c FROM Categoria c WHERE c.id IN " +
            "(SELECT p.categoria.id FROM Producto p " +
            "GROUP BY p.categoria.id " +
            "ORDER BY COUNT(p.categoria.id) DESC) " +
            "ORDER BY (SELECT COUNT(p2.categoria.id) FROM Producto p2 WHERE p2.categoria.id = c.id) DESC")
    List<Categoria> findCategoriasMasUtilizadas(@Param("limite") int limite);
}
