package informviva.gest.service;

import informviva.gest.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del servicio para la gestión de usuarios del sistema.
 * 100% Compatible con UsuarioServicioImpl existente.
 *
 * @author Roberto Rivas Lopez
 * @version 2.1.0 - EXACTAMENTE COMPATIBLE CON IMPLEMENTACIÓN
 */
public interface UsuarioServicio {

    // ===================== MÉTODOS DE CONSULTA BÁSICA =====================

    /**
     * Busca usuario por ID - RETORNA USUARIO DIRECTAMENTE
     */
    Usuario buscarPorId(Long id);

    /**
     * Busca usuario por nombre de usuario
     */
    Usuario buscarPorUsername(String username);

    /**
     * Busca usuario por email
     */
    Usuario buscarPorEmail(String email);

    /**
     * Lista todos los usuarios del sistema
     */
    List<Usuario> listarTodos();

    /**
     * Lista solo vendedores activos
     */
    List<Usuario> listarVendedores();

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    /**
     * Guarda o actualiza un usuario
     */
    Usuario guardar(Usuario usuario);

    /**
     * Elimina un usuario por ID
     */
    void eliminar(Long id);

    // ===================== MÉTODOS DE GESTIÓN DE ESTADO Y CONTRASEÑA =====================

    /**
     * Cambia la contraseña de un usuario
     */
    boolean cambiarPassword(Long id, String nuevaPassword);

    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    boolean cambiarEstado(Long id, boolean activo);

    // ===================== MÉTODOS DE GESTIÓN DE ROLES =====================

    /**
     * Agrega un rol a un usuario
     */
    boolean agregarRol(Long id, String rol);

    /**
     * Quita un rol de un usuario
     */
    boolean quitarRol(Long id, String rol);

    /**
     * Verifica si un usuario tiene un rol específico
     */
    boolean tieneRol(Long id, String rol);

    // ===================== MÉTODOS DE BÚSQUEDA AVANZADA =====================

    /**
     * Busca usuarios por nombre o email
     */
    List<Usuario> buscarPorNombreOEmail(String query);

    /**
     * Obtiene usuarios activos desde una fecha específica
     */
    List<Usuario> obtenerUsuariosActivosDesde(LocalDateTime fechaAcceso);

    /**
     * Obtiene usuarios creados en un período específico
     */
    List<Usuario> obtenerUsuariosCreadosEnPeriodo(LocalDateTime inicio, LocalDateTime fin);

    // ===================== MÉTODOS DE ACTIVIDAD =====================

    /**
     * Actualiza el último acceso de un usuario
     */
    void actualizarUltimoAcceso(Long usuarioId, LocalDateTime fechaAcceso);

    /**
     * Obtiene estadísticas de actividad de usuarios
     */
    Map<String, Object> obtenerEstadisticasActividad(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene usuarios inactivos por número de días
     */
    List<Usuario> obtenerUsuariosInactivos(int diasInactividad);

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    /**
     * Verifica si existe un usuario con el username dado
     */
    boolean existePorUsername(String username);
}