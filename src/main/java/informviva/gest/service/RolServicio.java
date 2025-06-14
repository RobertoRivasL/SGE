package informviva.gest.service;

import informviva.gest.model.Rol;

import java.util.List;

/**
 * Interfaz para la gestión de roles del sistema.
 * Define las operaciones disponibles para el manejo de roles y permisos.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public interface RolServicio {

    /**
     * Obtiene todos los roles del sistema
     *
     * @return Lista de roles
     */
    List<Rol> listarTodos();

    /**
     * Busca un rol por su ID
     *
     * @param id ID del rol
     * @return Rol encontrado o null si no existe
     */
    Rol buscarPorId(Long id);

    /**
     * Busca un rol por su nombre
     *
     * @param nombre Nombre del rol
     * @return Rol encontrado o null si no existe
     */
    Rol buscarPorNombre(String nombre);

    /**
     * Guarda un nuevo rol o actualiza uno existente
     *
     * @param rol Rol a guardar
     * @return Rol guardado
     */
    Rol guardar(Rol rol);

    /**
     * Elimina un rol por su ID
     *
     * @param id ID del rol a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean eliminar(Long id);

    /**
     * Actualiza los permisos de un rol
     *
     * @param rolId    ID del rol
     * @param permisos Lista de permisos a asignar
     */
    void actualizarPermisos(Long rolId, List<String> permisos);

    /**
     * Obtiene la lista completa de permisos disponibles en la aplicación
     *
     * @return Lista de permisos disponibles
     */
    List<String> listarTodosLosPermisos();

    /**
     * Verifica si existe un rol con el nombre especificado
     *
     * @param nombre Nombre del rol
     * @return true si existe, false en caso contrario
     */
    boolean existePorNombre(String nombre);

    /**
     * Verifica si un rol puede ser eliminado
     * (no tiene usuarios asociados)
     *
     * @param id ID del rol
     * @return true si puede ser eliminado, false en caso contrario
     */
    boolean puedeSerEliminado(Long id);

    /**
     * Cuenta el total de roles
     *
     * @return Número total de roles
     */
    Long contarTodos();

    /**
     * Obtiene los permisos de un rol específico
     *
     * @param rolId ID del rol
     * @return Lista de permisos del rol
     */
    List<String> obtenerPermisosPorRol(Long rolId);
}