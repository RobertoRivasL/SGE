package informviva.gest.repository;

import informviva.gest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de usuarios - CORREGIDO
 * Interfaz que extiende JpaRepository para operaciones CRUD básicas
 * e incluye consultas personalizadas
 *
 * @author Roberto Rivas
 * @version 2.1 - CORREGIDO para Set<String> roles
 */
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    // ==================== BÚSQUEDAS BÁSICAS ====================

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    // ==================== BÚSQUEDAS POR ESTADO ====================

    /**
     * Obtiene todos los usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Obtiene todos los usuarios inactivos
     */
    List<Usuario> findByActivoFalse();

    /**
     * Cuenta usuarios activos
     */
    long countByActivoTrue();

    /**
     * Cuenta usuarios inactivos
     */
    long countByActivoFalse();

    // ==================== BÚSQUEDAS POR ROL - CORREGIDAS ====================

    /**
     * Busca usuarios que tengan un rol específico
     * ✅ CORREGIDO: Para Set<String> roles
     */
    @Query("SELECT u FROM Usuario u WHERE :rolNombre MEMBER OF u.roles")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);

    /**
     * Cuenta usuarios que tengan un rol específico
     * ✅ Para Set<String> roles
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE :rolNombre MEMBER OF u.roles")
    long countByRolNombre(@Param("rolNombre") String rolNombre);

    /**
     * Busca usuarios vendedores (con rol VENDEDOR o ADMIN)
     * ✅ CORREGIDO: Para Set<String> roles
     */
    @Query("SELECT DISTINCT u FROM Usuario u WHERE " +
            "('VENDEDOR' MEMBER OF u.roles OR 'ADMIN' MEMBER OF u.roles OR 'GERENTE' MEMBER OF u.roles) " +
            "AND u.activo = true")
    List<Usuario> findVendedores();

    /**
     * Busca usuarios administradores
     * ✅ CORREGIDO: Para Set<String> roles
     */
    @Query("SELECT DISTINCT u FROM Usuario u WHERE " +
            "('ADMIN' MEMBER OF u.roles OR 'SUPER_ADMIN' MEMBER OF u.roles) " +
            "AND u.activo = true")
    List<Usuario> findAdministradores();

    /**
     * Verifica si un usuario tiene un rol específico
     * ✅ CORREGIDO: Para Set<String> roles
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.id = :usuarioId AND :rolNombre MEMBER OF u.roles")
    boolean usuarioTieneRol(@Param("usuarioId") Long usuarioId, @Param("rolNombre") String rolNombre);

    // ==================== BÚSQUEDAS POR FECHAS ====================

    /**
     * Busca usuarios creados en un rango de fechas
     */
    List<Usuario> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca usuarios que se han conectado en un rango de fechas
     */
    List<Usuario> findByUltimoAccesoBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca usuarios que no se han conectado desde una fecha específica
     */
    List<Usuario> findByUltimoAccesoBeforeOrUltimoAccesoIsNull(LocalDateTime fecha);

    /**
     * Obtiene usuarios creados en los últimos N días
     */
    @Query("SELECT u FROM Usuario u WHERE u.fechaCreacion >= :fechaLimite")
    List<Usuario> findUsuariosRecientes(@Param("fechaLimite") LocalDateTime fechaLimite);

    // ==================== BÚSQUEDAS DE TEXTO ====================

    /**
     * Busca usuarios por nombre o apellido (case insensitive)
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Usuario> findByNombreOrApellidoContaining(@Param("texto") String texto);

    /**
     * Busca usuarios por nombre completo (case insensitive)
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(CONCAT(u.nombre, ' ', u.apellido)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Usuario> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);

    /**
     * Busca usuarios por email parcial (case insensitive)
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Usuario> findByEmailContaining(@Param("email") String email);

    // ==================== ESTADÍSTICAS Y MÉTRICAS ====================

    /**
     * Cuenta usuarios por estado de actividad
     */
    @Query("SELECT u.activo, COUNT(u) FROM Usuario u GROUP BY u.activo")
    List<Object[]> countUsuariosPorEstado();

    /**
     * Cuenta usuarios por rol
     * ✅ CORREGIDO: Para Set<String> roles usando subconsulta
     */
    @Query("SELECT r.rol, COUNT(r.usuario) FROM " +
            "(SELECT u as usuario, r as rol FROM Usuario u JOIN u.roles r) r " +
            "GROUP BY r.rol")
    List<Object[]> countUsuariosPorRol();

    /**
     * Obtiene estadísticas de conexiones recientes
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.ultimoAcceso >= :fechaLimite")
    Long countUsuariosConectadosDesde(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtiene usuarios más activos (con más ventas)
     */
    @Query("SELECT u, COUNT(v) as ventasCount FROM Usuario u " +
            "LEFT JOIN Venta v ON u.id = v.vendedor.id " +
            "WHERE u.activo = true " +
            "GROUP BY u " +
            "ORDER BY ventasCount DESC")
    List<Object[]> findUsuariosMasActivos();

    // ==================== OPERACIONES ESPECIALES ====================

    /**
     * Busca usuarios sin ventas asignadas
     */
    @Query("SELECT u FROM Usuario u WHERE u.id NOT IN " +
            "(SELECT DISTINCT v.vendedor.id FROM Venta v WHERE v.vendedor IS NOT NULL)")
    List<Usuario> findUsuariosSinVentas();

    /**
     * Busca usuarios con ventas en un período específico
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN Venta v ON u.id = v.vendedor.id " +
            "WHERE v.fecha BETWEEN :inicio AND :fin")
    List<Usuario> findUsuariosConVentasEnPeriodo(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin);

    /**
     * Actualiza la fecha de último acceso de un usuario
     * ⚠️ NOTA: Cambiar LocalDateTime por LocalDate según tu modelo Usuario
     */
    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoAcceso = :fechaAcceso WHERE u.id = :usuarioId")
    void actualizarUltimoAcceso(@Param("usuarioId") Long usuarioId,
                                @Param("fechaAcceso") LocalDateTime fechaAcceso);

    /**
     * Busca usuarios por múltiples criterios
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:activo IS NULL OR u.activo = :activo)")
    List<Usuario> findByCriteriosMultiples(@Param("nombre") String nombre,
                                           @Param("email") String email,
                                           @Param("activo") Boolean activo);

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verifica si un username está disponible (no existe o pertenece al usuario actual)
     */
    @Query("SELECT COUNT(u) = 0 FROM Usuario u WHERE u.username = :username AND " +
            "(:usuarioId IS NULL OR u.id != :usuarioId)")
    boolean isUsernameDisponible(@Param("username") String username, @Param("usuarioId") Long usuarioId);

    /**
     * Verifica si un email está disponible (no existe o pertenece al usuario actual)
     */
    @Query("SELECT COUNT(u) = 0 FROM Usuario u WHERE u.email = :email AND " +
            "(:usuarioId IS NULL OR u.id != :usuarioId)")
    boolean isEmailDisponible(@Param("email") String email, @Param("usuarioId") Long usuarioId);

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    /**
     * Busca usuarios inactivos por más de X días
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = false AND " +
            "(u.ultimoAcceso IS NULL OR u.ultimoAcceso < :fechaLimite)")
    List<Usuario> findUsuariosInactivosPorMasDe(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Busca usuarios que nunca se han conectado
     */
    @Query("SELECT u FROM Usuario u WHERE u.ultimoAcceso IS NULL")
    List<Usuario> findUsuariosNuncaConectados();

    /**
     * Busca usuarios con datos incompletos
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "u.nombre IS NULL OR u.nombre = '' OR " +
            "u.apellido IS NULL OR u.apellido = '' OR " +
            "u.email IS NULL OR u.email = ''")
    List<Usuario> findUsuariosConDatosIncompletos();

    // ==================== MÉTODOS ADICIONALES PARA ROLES ====================

    /**
     * Busca usuarios por múltiples roles (cualquiera de los roles especificados)
     */
    @Query("SELECT DISTINCT u FROM Usuario u WHERE u.activo = true AND " +
            "EXISTS (SELECT r FROM u.roles r WHERE r IN :roles)")
    List<Usuario> findByRolesIn(@Param("roles") List<String> roles);

    /**
     * Busca usuarios que tengan TODOS los roles especificados
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
            "SIZE(u.roles) >= :rolesCount AND " +
            "(SELECT COUNT(r) FROM u.roles r WHERE r IN :roles) = :rolesCount")
    List<Usuario> findByAllRoles(@Param("roles") List<String> roles, @Param("rolesCount") Long rolesCount);

    /**
     * Cuenta usuarios por cada rol (versión alternativa más simple)
     */
    @Query(value = "SELECT ur.rol, COUNT(ur.usuario_id) " +
            "FROM usuario_roles ur " +
            "GROUP BY ur.rol", nativeQuery = true)
    List<Object[]> countUsuariosPorRolNativo();

    /**
     * Busca usuarios con roles específicos para ventas
     */
    @Query("SELECT DISTINCT u FROM Usuario u WHERE u.activo = true AND " +
            "('VENDEDOR' MEMBER OF u.roles OR 'ADMIN' MEMBER OF u.roles OR " +
            "'GERENTE' MEMBER OF u.roles OR 'SUPERVISOR' MEMBER OF u.roles)")
    List<Usuario> findUsuariosConPermisosVenta();

    /**
     * Busca usuarios sin roles asignados
     */
    @Query("SELECT u FROM Usuario u WHERE SIZE(u.roles) = 0")
    List<Usuario> findUsuariosSinRoles();

    /**
     * Busca usuarios con un rol específico excluyendo otros
     */
    @Query("SELECT u FROM Usuario u WHERE :rolIncluir MEMBER OF u.roles AND " +
            ":rolExcluir NOT MEMBER OF u.roles AND u.activo = true")
    List<Usuario> findByRolIncluyendoExcluyendo(@Param("rolIncluir") String rolIncluir,
                                                @Param("rolExcluir") String rolExcluir);
}