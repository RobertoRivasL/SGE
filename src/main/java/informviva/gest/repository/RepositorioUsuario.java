package informviva.gest.repository;


import informviva.gest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Encuentra usuarios que han accedido desde una fecha específica
     * Nota: ultimoAcceso es LocalDate, así que convertimos
     */
    @Query("SELECT u FROM Usuario u WHERE u.ultimoAcceso >= :fechaAcceso ORDER BY u.ultimoAcceso DESC")
    List<Usuario> findUsuariosActivosDesde(@Param("fechaAcceso") LocalDate fechaAcceso);

    /**
     * Encuentra usuarios creados en un período específico
     */
    @Query("SELECT u FROM Usuario u WHERE u.fechaCreacion BETWEEN :inicio AND :fin ORDER BY u.fechaCreacion DESC")
    List<Usuario> findUsuariosCreadosEnPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    /**
     * Encuentra usuarios que no han accedido en N días
     */
    @Query("SELECT u FROM Usuario u WHERE u.ultimoAcceso < :fechaLimite OR u.ultimoAcceso IS NULL")
    List<Usuario> findUsuariosInactivos(@Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Cuenta usuarios activos (que han accedido recientemente)
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.ultimoAcceso >= :fechaLimite AND u.activo = true")
    Long countUsuariosActivosDesde(@Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Encuentra usuarios por rol específico
     */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = :rol AND u.activo = true")
    List<Usuario> findByRolAndActivoTrue(@Param("rol") String rol);

    /**
     * Obtiene estadísticas de usuarios por período
     */
    @Query(value = "SELECT DATE(u.fecha_creacion) as fecha, COUNT(*) as cantidad " +
            "FROM usuarios u " +
            "WHERE u.fecha_creacion BETWEEN :inicio AND :fin " +
            "GROUP BY DATE(u.fecha_creacion) " +
            "ORDER BY fecha", nativeQuery = true)
    List<Object[]> findEstadisticasCreacionPorFecha(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

}