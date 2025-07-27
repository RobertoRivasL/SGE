package informviva.gest.service;

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.model.ExportacionHistorial;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del servicio para la gestión de exportaciones del sistema.
 * 100% Compatible con ExportacionServicioImpl existente.
 *
 * @author Roberto Rivas Lopez
 * @version 2.0.0 - EXACTAMENTE COMPATIBLE CON IMPLEMENTACIÓN
 */
public interface ExportacionServicio {

    // ===================== MÉTODOS PRINCIPALES DE EXPORTACIÓN =====================

    /**
     * Exporta usuarios según la configuración especificada
     */
    byte[] exportarUsuarios(ExportConfigDTO config);

    /**
     * Exporta clientes según la configuración especificada
     */
    byte[] exportarClientes(ExportConfigDTO config);

    /**
     * Exporta productos según la configuración especificada
     */
    byte[] exportarProductos(ExportConfigDTO config);

    /**
     * Exporta ventas según la configuración especificada
     */
    byte[] exportarVentas(ExportConfigDTO config);

    /**
     * Exporta reportes según la configuración especificada
     */
    byte[] exportarReportes(ExportConfigDTO config);

    // ===================== MÉTODOS DE ESTADÍSTICAS Y HISTORIAL =====================

    /**
     * Obtiene estadísticas de exportaciones
     */
    Map<String, Object> obtenerEstadisticasExportacion();

    /**
     * Obtiene el historial reciente de exportaciones
     */
    List<ExportacionHistorial> obtenerHistorialReciente(int limite);

    /**
     * Obtiene estimaciones de exportación
     */
    Map<String, Object> obtenerEstimaciones(String tipo, String formato,
                                            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene historial paginado
     */
    Page<ExportacionHistorial> obtenerHistorialPaginado(int page, int size);

    // ===================== MÉTODOS LEGACY PARA COMPATIBILIDAD =====================

    /**
     * @deprecated Usar exportarUsuarios(ExportConfigDTO config)
     */
    @Deprecated
    byte[] exportarUsuarios(String formato, Map<String, Object> filtros);

    /**
     * @deprecated Usar exportarClientes(ExportConfigDTO config)
     */
    @Deprecated
    byte[] exportarClientes(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros);

    /**
     * @deprecated Usar exportarProductos(ExportConfigDTO config)
     */
    @Deprecated
    byte[] exportarProductos(String formato, Map<String, Object> filtros);

    /**
     * @deprecated Usar exportarVentas(ExportConfigDTO config)
     */
    @Deprecated
    byte[] exportarVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros);
}