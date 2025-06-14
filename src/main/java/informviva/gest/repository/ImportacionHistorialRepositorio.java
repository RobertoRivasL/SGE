package informviva.gest.repository;


import informviva.gest.model.ImportacionHistorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder al historial de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface ImportacionHistorialRepositorio extends JpaRepository<ImportacionHistorial, Long> {

    /**
     * Encuentra importaciones por tipo
     */
    List<ImportacionHistorial> findByTipoImportacionOrderByFechaImportacionDesc(String tipoImportacion);

    /**
     * Encuentra importaciones de un usuario específico
     */
    List<ImportacionHistorial> findByUsuarioIdOrderByFechaImportacionDesc(Long usuarioId);

    /**
     * Encuentra importaciones en un rango de fechas
     */
    List<ImportacionHistorial> findByFechaImportacionBetweenOrderByFechaImportacionDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Encuentra importaciones exitosas por tipo
     */
    List<ImportacionHistorial> findByTipoImportacionAndExitosoTrueOrderByFechaImportacionDesc(String tipoImportacion);

    /**
     * Encuentra importaciones con errores por tipo
     */
    List<ImportacionHistorial> findByTipoImportacionAndExitosoFalseOrderByFechaImportacionDesc(String tipoImportacion);

    /**
     * Cuenta importaciones por tipo
     */
    Long countByTipoImportacion(String tipoImportacion);

    /**
     * Cuenta importaciones exitosas por tipo
     */
    Long countByTipoImportacionAndExitosoTrue(String tipoImportacion);

    /**
     * Cuenta importaciones con errores por tipo
     */
    Long countByTipoImportacionAndExitosoFalse(String tipoImportacion);

    /**
     * Busca importaciones con paginación
     */
    Page<ImportacionHistorial> findAllByOrderByFechaImportacionDesc(Pageable pageable);

    /**
     * Busca importaciones por tipo con paginación
     */
    Page<ImportacionHistorial> findByTipoImportacionOrderByFechaImportacionDesc(
            String tipoImportacion, Pageable pageable);

    List<ImportacionHistorial> findAllByOrderByFechaImportacionDesc();

    /**
     * Obtiene estadísticas de importaciones por tipo
     */
    @Query("SELECT i.tipoImportacion, " +
            "COUNT(i), " +
            "SUM(CASE WHEN i.exitoso = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.exitoso = false THEN 1 ELSE 0 END), " +
            "SUM(i.totalRegistros), " +
            "SUM(i.registrosExitosos), " +
            "SUM(i.registrosConError) " +
            "FROM ImportacionHistorial i " +
            "WHERE i.fechaImportacion >= :fechaInicio " +
            "GROUP BY i.tipoImportacion " +
            "ORDER BY i.tipoImportacion")
    List<Object[]> obtenerEstadisticasPorTipo(@Param("fechaInicio") LocalDateTime fechaInicio);

    /**
     * Obtiene las últimas importaciones realizadas
     */
    List<ImportacionHistorial> findTop10ByOrderByFechaImportacionDesc();

    /**
     * Encuentra importaciones por nombre de archivo
     */
    List<ImportacionHistorial> findByNombreArchivoContainingIgnoreCaseOrderByFechaImportacionDesc(String nombreArchivo);

    /**
     * Calcula el total de registros procesados por tipo en un período
     */
    @Query("SELECT COALESCE(SUM(i.totalRegistros), 0) " +
            "FROM ImportacionHistorial i " +
            "WHERE i.tipoImportacion = :tipo " +
            "AND i.fechaImportacion BETWEEN :fechaInicio AND :fechaFin")
    Long calcularTotalRegistrosProcesados(@Param("tipo") String tipo,
                                          @Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Calcula el total de registros exitosos por tipo en un período
     */
    @Query("SELECT COALESCE(SUM(i.registrosExitosos), 0) " +
            "FROM ImportacionHistorial i " +
            "WHERE i.tipoImportacion = :tipo " +
            "AND i.fechaImportacion BETWEEN :fechaInicio AND :fechaFin")
    Long calcularTotalRegistrosExitosos(@Param("tipo") String tipo,
                                        @Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);
}