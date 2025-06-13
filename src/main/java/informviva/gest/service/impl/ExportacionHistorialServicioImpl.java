package informviva.gest.service.impl;



import informviva.gest.model.ExportacionHistorial;
import informviva.gest.repository.ExportacionHistorialRepositorio;
import informviva.gest.service.ExportacionHistorialServicio;
import informviva.gest.service.ReporteClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio para el historial de exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
@Transactional
public class ExportacionHistorialServicioImpl implements ExportacionHistorialServicio {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionHistorialServicioImpl.class);

    private final ExportacionHistorialRepositorio exportacionHistorialRepositorio;
    private final ReporteClienteServicio reporteClienteServicio;
    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;

    @Autowired
    public ExportacionHistorialServicioImpl(
            ExportacionHistorialRepositorio exportacionHistorialRepositorio,
            ReporteClienteServicio reporteClienteServicio,
            ProductoServicio productoServicio,
            VentaServicio ventaServicio) {
        this.exportacionHistorialRepositorio = exportacionHistorialRepositorio;
        this.reporteClienteServicio = reporteClienteServicio;
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
    }

    @Override
    public ExportacionHistorial registrarExportacion(String tipoExportacion, String formato,
                                                     String usuarioSolicitante, LocalDate fechaInicioDatos,
                                                     LocalDate fechaFinDatos) {
        try {
            ExportacionHistorial historial = new ExportacionHistorial();
            historial.setTipoExportacion(tipoExportacion.toUpperCase());
            historial.setFormato(formato.toUpperCase());
            historial.setUsuarioSolicitante(usuarioSolicitante);
            historial.setFechaInicioDatos(fechaInicioDatos);
            historial.setFechaFinDatos(fechaFinDatos);
            historial.setEstado("Procesando");
            historial.setFechaSolicitud(LocalDateTime.now());

            ExportacionHistorial guardado = exportacionHistorialRepositorio.save(historial);
            logger.info("Exportación registrada: {} {} por usuario {}", formato, tipoExportacion, usuarioSolicitante);

            return guardado;
        } catch (Exception e) {
            logger.error("Error al registrar exportación: {}", e.getMessage());
            throw new RuntimeException("Error al registrar exportación", e);
        }
    }

    @Override
    public ExportacionHistorial marcarComoCompletada(Long id, Integer registros, Long tamanoArchivo, String rutaArchivo) {
        try {
            ExportacionHistorial historial = buscarPorId(id);
            if (historial == null) {
                throw new RuntimeException("Exportación no encontrada con ID: " + id);
            }

            historial.setEstado("Completado");
            historial.setNumeroRegistros(registros);
            historial.setTamanoArchivo(tamanoArchivo);
            historial.setRutaArchivo(rutaArchivo);

            ExportacionHistorial guardado = exportacionHistorialRepositorio.save(historial);
            logger.info("Exportación {} marcada como completada - {} registros, {} bytes",
                    id, registros, tamanoArchivo);

            return guardado;
        } catch (Exception e) {
            logger.error("Error al marcar exportación como completada: {}", e.getMessage());
            throw new RuntimeException("Error al marcar exportación como completada", e);
        }
    }

    @Override
    public ExportacionHistorial marcarComoError(Long id, String mensajeError) {
        try {
            ExportacionHistorial historial = buscarPorId(id);
            if (historial == null) {
                throw new RuntimeException("Exportación no encontrada con ID: " + id);
            }

            historial.setEstado("Error");
            historial.setMensajeError(mensajeError);

            ExportacionHistorial guardado = exportacionHistorialRepositorio.save(historial);
            logger.error("Exportación {} marcada como error: {}", id, mensajeError);

            return guardado;
        } catch (Exception e) {
            logger.error("Error al marcar exportación como error: {}", e.getMessage());
            throw new RuntimeException("Error al marcar exportación como error", e);
        }
    }

    @Override
    public ExportacionHistorial actualizarTiempoProcesamiento(Long id, Long tiempoProcesamiento) {
        try {
            ExportacionHistorial historial = buscarPorId(id);
            if (historial == null) {
                throw new RuntimeException("Exportación no encontrada con ID: " + id);
            }

            historial.setTiempoProcesamiento(tiempoProcesamiento);

            return exportacionHistorialRepositorio.save(historial);
        } catch (Exception e) {
            logger.error("Error al actualizar tiempo de procesamiento: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar tiempo de procesamiento", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportacionHistorial> obtenerHistorialReciente(int limite) {
        try {
            return exportacionHistorialRepositorio.findTopByOrderByFechaSolicitudDesc(limite);
        } catch (Exception e) {
            logger.error("Error al obtener historial reciente: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportacionHistorial> obtenerHistorialPorUsuario(String usuarioSolicitante, int limite) {
        try {
            List<ExportacionHistorial> historial = exportacionHistorialRepositorio
                    .findByUsuarioSolicitanteOrderByFechaSolicitudDesc(usuarioSolicitante);

            return historial.size() > limite ? historial.subList(0, limite) : historial;
        } catch (Exception e) {
            logger.error("Error al obtener historial por usuario {}: {}", usuarioSolicitante, e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportacionHistorial> obtenerHistorialPorFechasSolicitud(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return exportacionHistorialRepositorio
                    .findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error al obtener historial por fechas de solicitud: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportacionHistorial> obtenerHistorialPorFechasDatos(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            return exportacionHistorialRepositorio
                    .findByFechaInicioDatosBetweenOrderByFechaSolicitudDesc(fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error al obtener historial por fechas de datos: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasExportacion() {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Estadísticas básicas
            estadisticas.put("totalExportaciones", exportacionHistorialRepositorio.count());
            estadisticas.put("exportacionesCompletadas", exportacionHistorialRepositorio.countExportacionesCompletadas());
            estadisticas.put("exportacionesConError", exportacionHistorialRepositorio.countExportacionesConError());
            estadisticas.put("exportacionesProcesando", exportacionHistorialRepositorio.countExportacionesProcesando());
            estadisticas.put("totalRegistrosExportados", exportacionHistorialRepositorio.obtenerTotalRegistrosExportados());
            estadisticas.put("tamanoTotalArchivos", exportacionHistorialRepositorio.obtenerTamanoTotalArchivos());

            // Estadísticas por tipo de exportación
            List<Object[]> estadisticasPorTipo = exportacionHistorialRepositorio.obtenerEstadisticasPorTipo();
            Map<String, Long> porTipo = new HashMap<>();
            for (Object[] resultado : estadisticasPorTipo) {
                porTipo.put((String) resultado[0], (Long) resultado[1]);
            }
            estadisticas.put("exportacionesPorTipo", porTipo);

            // Estadísticas por formato
            List<Object[]> estadisticasPorFormato = exportacionHistorialRepositorio.obtenerEstadisticasPorFormato();
            Map<String, Long> porFormato = new HashMap<>();
            for (Object[] resultado : estadisticasPorFormato) {
                porFormato.put((String) resultado[0], (Long) resultado[1]);
            }
            estadisticas.put("exportacionesPorFormato", porFormato);

            // Exportaciones del día actual
            LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            List<ExportacionHistorial> exportacionesHoy = exportacionHistorialRepositorio
                    .obtenerExportacionesDelDia(inicioHoy, finHoy);
            estadisticas.put("exportacionesHoy", exportacionesHoy.size());

            logger.info("Estadísticas de exportación generadas exitosamente");

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de exportación: {}", e.getMessage());
        }

        return estadisticas;
    }

    @Override
    @Transactional
    public Long limpiarHistorialAnteriorA(LocalDateTime fechaLimite) {
        try {
            Long registrosEliminados = exportacionHistorialRepositorio.deleteByFechaSolicitudBefore(fechaLimite);
            logger.info("Limpieza de historial completada: {} registros eliminados", registrosEliminados);
            return registrosEliminados;
        } catch (Exception e) {
            logger.error("Error al limpiar historial anterior a {}: {}", fechaLimite, e.getMessage());
            return 0L;
        }
    }

    @Override
    @Async
    public void ejecutarExportacionesProgramadas() {
        logger.info("Iniciando exportaciones programadas");

        try {
            String usuarioSistema = "SISTEMA";
            LocalDateTime ahora = LocalDateTime.now();

            // Ejemplo: Exportar reporte semanal de clientes cada lunes
            if (ahora.getDayOfWeek().getValue() == 1) { // Lunes
                logger.info("Ejecutando exportación semanal de clientes");

                try {
                    // Registrar inicio de exportación
                    LocalDate inicioSemana = ahora.toLocalDate().minusWeeks(1);
                    LocalDate finSemana = ahora.toLocalDate();

                    ExportacionHistorial exportacion = registrarExportacion(
                            "CLIENTES", "EXCEL", usuarioSistema, inicioSemana, finSemana);

                    // Simular procesamiento
                    Thread.sleep(1000);

                    // Marcar como completada
                    marcarComoCompletada(exportacion.getId(), 100, 50000L, "/exports/clientes_semanal.xlsx");

                } catch (Exception e) {
                    // Si hay una exportación registrada, marcarla como error
                    logger.error("Error en exportación semanal de clientes: {}", e.getMessage());
                }
            }

            // Ejemplo: Backup mensual de productos el primer día del mes
            if (ahora.getDayOfMonth() == 1) {
                logger.info("Ejecutando backup mensual de productos");

                try {
                    ExportacionHistorial exportacion = registrarExportacion(
                            "PRODUCTOS", "CSV", usuarioSistema, null, null);

                    // Simular procesamiento
                    Thread.sleep(500);

                    marcarComoCompletada(exportacion.getId(), 500, 25000L, "/exports/productos_backup.csv");

                } catch (Exception e) {
                    logger.error("Error en backup mensual de productos: {}", e.getMessage());
                }
            }

            logger.info("Exportaciones programadas completadas");

        } catch (Exception e) {
            logger.error("Error en exportaciones programadas: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExportacionHistorial buscarPorId(Long id) {
        try {
            return exportacionHistorialRepositorio.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar exportación por ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la IP del usuario actual (simplificado)
     */
    private String obtenerIpUsuario() {
        // En un entorno real, esto vendría del HttpServletRequest
        // Por ahora retornamos un valor por defecto
        return "127.0.0.1";
    }
}