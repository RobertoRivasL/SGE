package informviva.gest.service;

import informviva.gest.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz para la gestión de categorías.
 * Define las operaciones disponibles para el manejo de categorías en el sistema.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public interface CategoriaServicio {


    List<Categoria> findAllActivas();
    Categoria save(Categoria categoria);

    // ===================== MÉTODOS DE CONSULTA =====================

    /**
     * Obtiene todas las categorías ordenadas por nombre
     *
     * @return Lista de categorías
     */
    List<Categoria> listarTodas();

    /**
     * Obtiene todas las categorías (alias para compatibilidad)
     *
     * @return Lista de categorías
     */
    List<Categoria> findAll();

    /**
     * Obtiene categorías paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de categorías
     */
    Page<Categoria> listarPaginadas(Pageable pageable);

    /**
     * Busca una categoría por su ID
     *
     * @param id ID de la categoría
     * @return Categoría encontrada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    Categoria buscarPorId(Long id);

    /**
     * Busca una categoría por su nombre exacto
     *
     * @param nombre Nombre de la categoría
     * @return Categoría encontrada o null si no existe
     */
    Categoria buscarPorNombre(String nombre);

    /**
     * Obtiene solo las categorías activas ordenadas por nombre
     *
     * @return Lista de categorías activas
     */
    List<Categoria> listarActivas();

    /**
     * Obtiene categorías activas paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de categorías activas
     */
    Page<Categoria> listarActivasPaginadas(Pageable pageable);

    /**
     * Busca categorías por texto en el nombre (case insensitive)
     *
     * @param texto Texto a buscar en el nombre de las categorías
     * @return Lista de categorías que contienen el texto
     */
    List<Categoria> buscarPorTexto(String texto);

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    /**
     * Guarda una nueva categoría o actualiza una existente
     *
     * @param categoria Categoría a guardar
     * @return Categoría guardada
     * @throws IllegalArgumentException si los datos son inválidos o el nombre ya existe
     */
    Categoria guardar(Categoria categoria);

    /**
     * Actualiza una categoría existente
     *
     * @param id        ID de la categoría a actualizar
     * @param categoria Datos actualizados de la categoría
     * @return Categoría actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos son inválidos
     */
    Categoria actualizar(Long id, Categoria categoria);

    /**
     * Elimina una categoría por su ID
     *
     * @param id ID de la categoría a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si la categoría tiene productos asociados
     */
    void eliminar(Long id);

    /**
     * Activa o desactiva una categoría
     *
     * @param id     ID de la categoría
     * @param activa true para activar, false para desactivar
     * @return true si se cambió correctamente, false en caso contrario
     */
    boolean cambiarEstado(Long id, boolean activa);

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    /**
     * Verifica si existe una categoría con el nombre dado
     *
     * @param nombre Nombre de la categoría
     * @return true si existe, false en caso contrario
     */
    boolean existePorNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre dado excluyendo un ID específico
     * (útil para validaciones de actualización)
     *
     * @param nombre    Nombre de la categoría
     * @param excludeId ID a excluir de la búsqueda
     * @return true si existe otra categoría con el mismo nombre, false en caso contrario
     */
    boolean existePorNombre(String nombre, Long excludeId);

    /**
     * Verifica si una categoría puede ser eliminada
     * (no tiene productos asociados)
     *
     * @param id ID de la categoría
     * @return true si puede ser eliminada, false en caso contrario
     */
    boolean puedeSerEliminada(Long id);

    // ===================== MÉTODOS DE CONTEO =====================

    /**
     * Cuenta el total de categorías
     *
     * @return Número total de categorías
     */
    Long contarTodas();

    /**
     * Cuenta las categorías activas
     *
     * @return Número de categorías activas
     */
    Long contarActivas();

    /**
     * Cuenta las categorías inactivas
     *
     * @return Número de categorías inactivas
     */
    Long contarInactivas();
}