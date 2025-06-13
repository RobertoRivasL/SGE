package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * Interfaz para la gestión de usuarios del sistema
 */
public interface UsuarioServicio {

    /**
     * Busca un usuario por su ID
     *
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     */
    Usuario buscarPorId(Long id);

    /**
     * Busca un usuario por su nombre de usuario
     *
     * @param username Nombre de usuario
     * @return Usuario encontrado o null si no existe
     */
    Usuario buscarPorUsername(String username);

    /**
     * Busca un usuario por su dirección de correo electrónico
     *
     * @param email Correo electrónico
     * @return Usuario encontrado o null si no existe
     */
    Usuario buscarPorEmail(String email);

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     *
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    Usuario guardar(Usuario usuario);

    /**
     * Elimina un usuario por su ID
     *
     * @param id ID del usuario
     */
    void eliminar(Long id);

    /**
     * Obtiene todos los usuarios
     *
     * @return Lista de usuarios
     */
    List<Usuario> listarTodos();

    /**
     * Obtiene los usuarios con rol de vendedor
     *
     * @return Lista de vendedores
     */
    List<Usuario> listarVendedores();

    /**
     * Cambia la contraseña de un usuario
     *
     * @param id            ID del usuario
     * @param nuevaPassword Nueva contraseña
     * @return true si se cambió correctamente, false en caso contrario
     */
    boolean cambiarPassword(Long id, String nuevaPassword);

    /**
     * Activa o desactiva un usuario
     *
     * @param id     ID del usuario
     * @param activo true para activar, false para desactivar
     * @return true si se cambió correctamente, false en caso contrario
     */
    boolean cambiarEstado(Long id, boolean activo);

    /**
     * Agrega un rol a un usuario
     *
     * @param id  ID del usuario
     * @param rol Rol a agregar
     * @return true si se agregó correctamente, false en caso contrario
     */
    boolean agregarRol(Long id, String rol);

    /**
     * Quita un rol a un usuario
     *
     * @param id  ID del usuario
     * @param rol Rol a quitar
     * @return true si se quitó correctamente, false en caso contrario
     */
    boolean quitarRol(Long id, String rol);

    /**
     * Verifica si un usuario tiene un rol específico
     *
     * @param id  Usuario ID
     * @param rol Rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    boolean tieneRol(Long id, String rol);

    /**
     * Busca usuarios por nombre o correo electrónico
     *
     * @param query Texto a buscar en nombre o correo
     * @return Lista de usuarios que coinciden
     */
    List<Usuario> buscarPorNombreOEmail(String query);


    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Obtiene usuarios que han accedido desde una fecha específica
     */
    List<Usuario> obtenerUsuariosActivosDesde(LocalDateTime fechaAcceso);

    /**
     * Obtiene usuarios creados en un período con hora específica
     */
    List<Usuario> obtenerUsuariosCreadosEnPeriodo(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Actualiza la fecha de último acceso de un usuario
     */
    void actualizarUltimoAcceso(Long usuarioId, LocalDateTime fechaAcceso);

    /**
     * Obtiene estadísticas de actividad de usuarios
     */
    Map<String, Object> obtenerEstadisticasActividad(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene usuarios que no han accedido en un período determinado
     */
    List<Usuario> obtenerUsuariosInactivos(int diasInactividad);
}
