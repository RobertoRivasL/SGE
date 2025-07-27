package informviva.gest.controlador;

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.model.ExportacionHistorial;
import informviva.gest.service.ExportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para el módulo de exportación
 * Maneja tanto las vistas HTML como las exportaciones API
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/exportacion")
public class ExportacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionControlador.class);

    private final ExportacionServicio exportacionServicio;

    @Autowired
    public ExportacionControlador(ExportacionServicio exportacionServicio) {
        this.exportacionServicio = exportacionServicio;
    }

    /**
     * Muestra la página principal del módulo de exportación
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String mostrarCentroExportacion(Model model) {
        try {
            // Obtener estadísticas de exportaciones
            Map<String, Object> estadisticas = exportacionServicio.obtenerEstadisticasExportacion();
            model.addAttribute("estadisticas", estadisticas);

            // Obtener historial reciente
            model.addAttribute("historialReciente", exportacionServicio.obtenerHistorialReciente(10));

            logger.info("Centro de exportación cargado exitosamente");
            return "exportacion/centro";
        } catch (Exception e) {
            logger.error("Error al cargar centro de exportación: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el módulo de exportación");
            return "error/500";
        }
    }

    /**
     * API: Exportar usuarios
     */
    @PostMapping("/api/usuarios/{formato}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarUsuarios(
            @PathVariable String formato,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false, defaultValue = "true") Boolean soloActivos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            logger.info("Iniciando exportación de usuarios en formato: {}", formato);

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("usuarios");
            config.setFormato(formato);
            config.setFechaInicio(fechaInicio != null ? fechaInicio.atStartOfDay() : null);
            config.setFechaFin(fechaFin != null ? fechaFin.atTime(23, 59, 59) : null);
            config.addFiltro("rol", rol);
            config.addFiltro("soloActivos", soloActivos);

            byte[] archivo = exportacionServicio.exportarUsuarios(config);

            if (archivo == null || archivo.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(formato));
            headers.setContentDispositionFormData("attachment",
                    generateFileName("usuarios", formato, fechaInicio, fechaFin));

            logger.info("Exportación de usuarios completada. Tamaño: {} bytes", archivo.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error en exportación de usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Exportar clientes
     */
    @PostMapping("/api/clientes/{formato}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<byte[]> exportarClientes(
            @PathVariable String formato,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") Boolean soloConCompras,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            logger.info("Iniciando exportación de clientes en formato: {}", formato);

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("clientes");
            config.setFormato(formato);
            config.setFechaInicio(fechaInicio != null ? fechaInicio.atStartOfDay() : null);
            config.setFechaFin(fechaFin != null ? fechaFin.atTime(23, 59, 59) : null);
            config.addFiltro("categoria", categoria);
            config.addFiltro("soloConCompras", soloConCompras);

            byte[] archivo = exportacionServicio.exportarClientes(config);

            if (archivo == null || archivo.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(formato));
            headers.setContentDispositionFormData("attachment",
                    generateFileName("clientes", formato, fechaInicio, fechaFin));

            logger.info("Exportación de clientes completada. Tamaño: {} bytes", archivo.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error en exportación de clientes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Exportar productos
     */
    @PostMapping("/api/productos/{formato}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'PRODUCTOS', 'VENTAS')")
    public ResponseEntity<byte[]> exportarProductos(
            @PathVariable String formato,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") Boolean soloBajoStock,
            @RequestParam(required = false, defaultValue = "true") Boolean soloActivos) {

        try {
            logger.info("Iniciando exportación de productos en formato: {}", formato);

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("productos");
            config.setFormato(formato);
            config.addFiltro("categoria", categoria);
            config.addFiltro("soloBajoStock", soloBajoStock);
            config.addFiltro("soloActivos", soloActivos);

            byte[] archivo = exportacionServicio.exportarProductos(config);

            if (archivo == null || archivo.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(formato));
            headers.setContentDispositionFormData("attachment",
                    generateFileName("productos", formato, null, null));

            logger.info("Exportación de productos completada. Tamaño: {} bytes", archivo.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error en exportación de productos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Exportar ventas
     */
    @PostMapping("/api/ventas/{formato}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<byte[]> exportarVentas(
            @PathVariable String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String vendedor,
            @RequestParam(required = false) String estado) {

        try {
            logger.info("Iniciando exportación de ventas en formato: {}", formato);

            // Establecer fechas por defecto si no se proporcionan
            if (fechaInicio == null) {
                fechaInicio = LocalDate.now().minusMonths(1);
            }
            if (fechaFin == null) {
                fechaFin = LocalDate.now();
            }

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("ventas");
            config.setFormato(formato);
            config.setFechaInicio(fechaInicio.atStartOfDay());
            config.setFechaFin(fechaFin.atTime(23, 59, 59));
            config.addFiltro("vendedor", vendedor);
            config.addFiltro("estado", estado);

            byte[] archivo = exportacionServicio.exportarVentas(config);

            if (archivo == null || archivo.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(formato));
            headers.setContentDispositionFormData("attachment",
                    generateFileName("ventas", formato, fechaInicio, fechaFin));

            logger.info("Exportación de ventas completada. Tamaño: {} bytes", archivo.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error en exportación de ventas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Exportar reportes completos
     */
    @PostMapping("/api/reportes/{formato}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<byte[]> exportarReportes(
            @PathVariable String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false, defaultValue = "resumen") String tipoReporte,
            @RequestParam(required = false, defaultValue = "true") Boolean incluirGraficos) {

        try {
            logger.info("Iniciando exportación de reportes en formato: {}", formato);

            // Establecer fechas por defecto si no se proporcionan
            if (fechaInicio == null) {
                fechaInicio = LocalDate.now().minusMonths(3);
            }
            if (fechaFin == null) {
                fechaFin = LocalDate.now();
            }

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("reportes");
            config.setFormato(formato);
            config.setFechaInicio(fechaInicio.atStartOfDay());
            config.setFechaFin(fechaFin.atTime(23, 59, 59));
            config.addFiltro("tipoReporte", tipoReporte);
            config.addFiltro("incluirGraficos", incluirGraficos);

            byte[] archivo = exportacionServicio.exportarReportes(config);

            if (archivo == null || archivo.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(formato));
            headers.setContentDispositionFormData("attachment",
                    generateFileName("reporte_" + tipoReporte, formato, fechaInicio, fechaFin));

            logger.info("Exportación de reportes completada. Tamaño: {} bytes", archivo.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error en exportación de reportes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Obtener estimaciones de exportación
     */
    @GetMapping("/api/estimaciones/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstimaciones(
            @PathVariable String tipo,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            Map<String, Object> estimaciones = exportacionServicio.obtenerEstimaciones(tipo, formato,
                    fechaInicio != null ? fechaInicio.atStartOfDay() : null,
                    fechaFin != null ? fechaFin.atTime(23, 59, 59) : null);
            return ResponseEntity.ok(estimaciones);
        } catch (Exception e) {
            logger.error("Error obteniendo estimaciones: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Obtener historial de exportaciones
     */
    @GetMapping("/api/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<ExportacionHistorial> historialPage = exportacionServicio.obtenerHistorialPaginado(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("content", historialPage.getContent());
            response.put("totalPages", historialPage.getTotalPages());
            response.put("totalElements", historialPage.getTotalElements());
            response.put("currentPage", historialPage.getNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo historial: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determina el MediaType según el formato
     */
    private MediaType getMediaType(String formato) {
        return switch (formato.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "excel", "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv" -> MediaType.parseMediaType("text/csv");
            case "json" -> MediaType.APPLICATION_JSON;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    /**
     * Genera nombre de archivo con formato estándar
     */
    private String generateFileName(String tipo, String formato, LocalDate fechaInicio, LocalDate fechaFin) {
        StringBuilder nombre = new StringBuilder();
        nombre.append(tipo);

        if (fechaInicio != null && fechaFin != null) {
            nombre.append("_").append(fechaInicio).append("_").append(fechaFin);
        } else {
            nombre.append("_").append(LocalDate.now());
        }

        // Determinar extensión
        String extension = switch (formato.toLowerCase()) {
            case "excel", "xlsx" -> "xlsx";
            case "csv" -> "csv";
            case "pdf" -> "pdf";
            case "json" -> "json";
            default -> "dat";
        };

        nombre.append(".").append(extension);
        return nombre.toString();
    }
}