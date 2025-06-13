package informviva.gest.controlador;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.service.ReporteClienteServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador para reportes de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/reportes/clientes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
public class ReporteClienteControlador {

    private static final Logger logger = LoggerFactory.getLogger(ReporteClienteControlador.class);

    @Autowired
    private ReporteClienteServicio reporteClienteServicio;

    /**
     * Muestra la página principal de reportes de clientes
     */
    @GetMapping
    public String mostrarReporteClientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model modelo) {

        try {
            // Establecer fechas por defecto si no se proporcionan
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3); // Últimos 3 meses por defecto
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // Validar que la fecha de inicio no sea posterior a la fecha fin
            if (startDate.isAfter(endDate)) {
                startDate = endDate.minusMonths(1);
            }

            // Generar reporte de clientes
            List<ClienteReporteDTO> clientes = reporteClienteServicio.generarReporteClientes(startDate, endDate);

            // Obtener estadísticas generales
            Map<String, Object> estadisticas = reporteClienteServicio.obtenerEstadisticasGenerales(startDate, endDate);

            // Obtener distribución por antigüedad
            Map<String, Long> distribucionAntiguedad = reporteClienteServicio.analizarDistribucionAntiguedad();

            // Obtener métricas de retención
            Map<String, Object> metricasRetencion = reporteClienteServicio.calcularMetricasRetencion(startDate, endDate);

            // Agregar datos al modelo
            modelo.addAttribute("clientes", clientes);
            modelo.addAttribute("estadisticas", estadisticas);
            modelo.addAttribute("distribucionAntiguedad", distribucionAntiguedad);
            modelo.addAttribute("metricasRetencion", metricasRetencion);
            modelo.addAttribute("startDate", startDate);
            modelo.addAttribute("endDate", endDate);

            logger.info("Reporte de clientes generado exitosamente para el período: {} - {}", startDate, endDate);

        } catch (Exception e) {
            logger.error("Error al generar reporte de clientes: {}", e.getMessage());
            modelo.addAttribute("mensajeError", "Error al generar el reporte: " + e.getMessage());
            modelo.addAttribute("clientes", List.of());
            modelo.addAttribute("estadisticas", Map.of());
            modelo.addAttribute("distribucionAntiguedad", Map.of());
            modelo.addAttribute("metricasRetencion", Map.of());
        }

        return "reportes/clientes";
    }

    /**
     * Muestra el dashboard de análisis de clientes
     */
    @GetMapping("/dashboard")
    public String mostrarDashboardClientes(Model modelo) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);

            // Obtener top 10 clientes
            List<ClienteReporteDTO> topClientes = reporteClienteServicio.obtenerTopClientes(10, null, null);

            // Obtener estadísticas del mes actual
            Map<String, Object> estadisticasMes = reporteClienteServicio.obtenerEstadisticasGenerales(inicioMes, hoy);

            // Obtener clientes inactivos (últimos 90 días)
            List<Cliente> clientesInactivos = reporteClienteServicio.obtenerClientesInactivos(90);

            // Distribución por antigüedad
            Map<String, Long> distribucionAntiguedad = reporteClienteServicio.analizarDistribucionAntiguedad();

            modelo.addAttribute("topClientes", topClientes);
            modelo.addAttribute("estadisticasMes", estadisticasMes);
            modelo.addAttribute("clientesInactivos", clientesInactivos);
            modelo.addAttribute("distribucionAntiguedad", distribucionAntiguedad);

        } catch (Exception e) {
            logger.error("Error al cargar dashboard de clientes: {}", e.getMessage());
            modelo.addAttribute("mensajeError", "Error al cargar el dashboard");
        }

        return "reportes/clientes-dashboard";
    }

    /**
     * Exporta el reporte de clientes a Excel
     */
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarReporteExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Establecer fechas por defecto si no se proporcionan
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // Generar reporte
            List<ClienteReporteDTO> clientes = reporteClienteServicio.generarReporteClientes(startDate, endDate);

            // Exportar a Excel
            byte[] excelData = reporteClienteServicio.exportarReporteClientes(clientes, "EXCEL");

            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "reporte_clientes_" + startDate + "_" + endDate + ".xlsx");

            logger.info("Reporte de clientes exportado a Excel exitosamente");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            logger.error("Error al exportar reporte a Excel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporta el reporte de clientes a CSV
     */
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarReporteCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Establecer fechas por defecto si no se proporcionan
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // Generar reporte
            List<ClienteReporteDTO> clientes = reporteClienteServicio.generarReporteClientes(startDate, endDate);

            // Exportar a CSV
            byte[] csvData = reporteClienteServicio.exportarReporteClientes(clientes, "CSV");

            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment",
                    "reporte_clientes_" + startDate + "_" + endDate + ".csv");

            logger.info("Reporte de clientes exportado a CSV exitosamente");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            logger.error("Error al exportar reporte a CSV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST para obtener datos de clientes (para gráficos dinámicos)
     */
    @GetMapping("/api/datos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDatosClientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Establecer fechas por defecto
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // Obtener datos
            List<ClienteReporteDTO> clientes = reporteClienteServicio.generarReporteClientes(startDate, endDate);
            Map<String, Object> estadisticas = reporteClienteServicio.obtenerEstadisticasGenerales(startDate, endDate);
            Map<String, Long> distribucionAntiguedad = reporteClienteServicio.analizarDistribucionAntiguedad();

            // Preparar respuesta
            Map<String, Object> respuesta = Map.of(
                    "clientes", clientes,
                    "estadisticas", estadisticas,
                    "distribucionAntiguedad", distribucionAntiguedad,
                    "periodo", Map.of("inicio", startDate, "fin", endDate)
            );

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al obtener datos de clientes via API: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene clientes inactivos
     */
    @GetMapping("/inactivos")
    public String mostrarClientesInactivos(
            @RequestParam(defaultValue = "90") int dias,
            Model modelo) {

        try {
            List<Cliente> clientesInactivos = reporteClienteServicio.obtenerClientesInactivos(dias);

            modelo.addAttribute("clientesInactivos", clientesInactivos);
            modelo.addAttribute("diasInactividad", dias);

        } catch (Exception e) {
            logger.error("Error al obtener clientes inactivos: {}", e.getMessage());
            modelo.addAttribute("mensajeError", "Error al cargar clientes inactivos");
            modelo.addAttribute("clientesInactivos", List.of());
        }

        return "reportes/clientes-inactivos";
    }
}