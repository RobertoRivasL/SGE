package informviva.gest.service;


import informviva.gest.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz para la gestión de categorías
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface CategoriaServicio {

    /**
     * Obtiene todas las categorías
     *
     * @return Lista de categorías
     */
    List<Categoria> listarTodas();

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
     * @return Categoría encontrada o null si no existe
     */
    Categoria buscarPorId(Long id);

    /**
     * Busca una categoría por su nombre
     *
     * @param nombre Nombre de la categoría
     * @return Categoría encontrada o null si no existe
     */
    Categoria buscarPorNombre(String nombre);

    /**
     * Guarda una nueva categoría o actualiza una existente
     *
     * @param categoria Categoría a guardar
     * @return Categoría guardada
     */
    Categoria guardar(Categoria categoria);

    /**
     * Actualiza una categoría existente
     *
     * @param id        ID de la categoría
     * @param categoria Datos actualizados de la categoría
     * @return Categoría actualizada
     */
    Categoria actualizar(Long id, Categoria categoria);

    /**
     * Elimina una categoría por su ID
     *
     * @param id ID de la categoría
     */
    void eliminar(Long id);

    /**
     * Verifica si existe una categoría con el nombre dado
     *
     * @param nombre Nombre de la categoría
     * @return true si existe, false en caso contrario
     */
    boolean existePorNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre dado excluyendo un ID específico
     *
     * @param nombre    Nombre de la categoría
     * @param excludeId ID a excluir de la búsqueda
     * @return true si existe, false en caso contrario
     */
    boolean existePorNombre(String nombre, Long excludeId);


    /**
     * Obtiene categorías activas solamente
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
     * Activa o desactiva una categoría
     *
     * @param id     ID de la categoría
     * @param activa true para activar, false para desactivar
     * @return true si se cambió correctamente, false en caso contrario
     */
    boolean cambiarEstado(Long id, boolean activa);

    /**
     * Cuenta el total de categorías
     *
     * @return Número total de categorías
     */
    Long contarTodas();

    /**
     * Cuenta categorías activas
     *
     * @return Número de categorías activas
     */
    Long contarActivas();

    /**
     * Cuenta categorías inactivas
     *
     * @return Número de categorías inactivas
     */
    Long contarInactivas();

    /**
     * Busca categorías por texto en nombre o descripción
     *
     * @param texto Texto a buscar
     * @return Lista de categorías que coinciden
     */
    List<Categoria> buscarPorTexto(String texto);

    /**
     * Busca categorías por texto con paginación
     *
     * @param texto    Texto a buscar
     * @param pageable Configuración de paginación
     * @return Página de categorías que coinciden
     */
    Page<Categoria> buscarPorTextoPaginado(String texto, Pageable pageable);

    /**
     * Obtiene las categorías más utilizadas
     *
     * @param limite Número máximo de categorías a retornar
     * @return Lista de categorías más utilizadas
     */
    List<Categoria> obtenerMasUtilizadas(int limite);

    /**
     * Verifica si una categoría puede ser eliminada (no tiene productos asociados)
     *
     * @param id ID de la categoría
     * @return true si puede ser eliminada, false en caso contrario
     */
    boolean puedeSerEliminada(Long id);

    List<Categoria> findAllActivas();

    Categoria save(Categoria categoria);
}