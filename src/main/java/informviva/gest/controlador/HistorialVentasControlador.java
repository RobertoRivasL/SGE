package informviva.gest.controlador;


import java.util.Optional;

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.service.VentaServicio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ExportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para el historial y análisis de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/ventas")
@PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
public class HistorialVentasControlador {

    private static final Logger logger = LoggerFactory.getLogger(HistorialVentasControlador.class);

    private final VentaServicio ventaServicio;
    private final ClienteServicio clienteServicio;
    private final ExportacionServicio exportacionServicio;

    @Autowired
    public HistorialVentasControlador(VentaServicio ventaServicio,
                                      ClienteServicio clienteServicio,
                                      ExportacionServicio exportacionServicio) {
        this.ventaServicio = ventaServicio;
        this.clienteServicio = clienteServicio;
        this.exportacionServicio = exportacionServicio;
    }


    private HttpHeaders crearHeadersExportacion(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String nombreArchivo;
        MediaType mediaType;

        switch (formato.toLowerCase()) {
            case "csv":
                nombreArchivo = "historial_ventas_" + fechaInicio + "_" + fechaFin + ".csv";
                mediaType = MediaType.TEXT_PLAIN;
                break;
            case "pdf":
                nombreArchivo = "historial_ventas_" + fechaInicio + "_" + fechaFin + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default: // excel
                nombreArchivo = "historial_ventas_" + fechaInicio + "_" + fechaFin + ".xlsx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        return headers;
    }

    /**
     * Muestra el historial de ventas con filtros
     */
    @GetMapping("/historial")
    public String mostrarHistorialVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String metodo,
            @RequestParam(required = false) String estado,
            Model model) {

        try {
            // Establecer fechas por defecto si no se proporcionan
            if (fechaInicio == null && fechaFin == null) {
                fechaFin = LocalDate.now();
                fechaInicio = fechaFin.minusMonths(1); // Último mes por defecto
            } else if (fechaInicio == null) {
                fechaInicio = fechaFin.minusMonths(1);
            } else if (fechaFin == null) {
                fechaFin = LocalDate.now();
            }

            // Validar que fecha inicio no sea posterior a fecha fin
            if (fechaInicio.isAfter(fechaFin)) {
                LocalDate temp = fechaInicio;
                fechaInicio = fechaFin;
                fechaFin = temp;// Configurar paginación
                Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
            }

            // Configurar paginación
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

            // Obtener ventas filtradas
            Page<VentaDTO> ventas = ventaServicio.buscarPorRangoFechas(fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), pageable);

            // Calcular estadísticas del período
            Map<String, Object> estadisticas = calcularEstadisticasPeriodo(
                    fechaInicio, fechaFin, cliente, metodo, estado);

            // Agregar atributos al modelo
            model.addAttribute("ventas", ventas);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("cliente", cliente);
            model.addAttribute("metodo", metodo);
            model.addAttribute("estado", estado);
            model.addAttribute("currentPage", page);

            // Agregar estadísticas
            model.addAllAttributes(estadisticas);

            logger.info("Historial de ventas cargado - Período: {} a {}, Total: {}",
                    fechaInicio, fechaFin, ventas.getTotalElements());

            return "ventas/historial";

        } catch (Exception e) {
            logger.error("Error al cargar historial de ventas: {}", e.getMessage());
            model.addAttribute("mensaje", "Error al cargar el historial de ventas");
            model.addAttribute("tipoMensaje", "error");
            return "ventas/historial";
        }
    }

    /**
     * Exporta el historial de ventas a Excel
     */
    @GetMapping("/historial/exportar")
    public ResponseEntity<byte[]> exportarHistorialVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String metodo,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "excel") String formato) {

        try {
            if (fechaInicio == null && fechaFin == null) {
                fechaFin = LocalDateTime.now();
                fechaInicio = fechaFin.minusMonths(1);
            }

            ExportConfigDTO config = new ExportConfigDTO();
            config.setTipo("ventas");
            config.setFormato(formato);
            config.setFechaInicio(fechaInicio);
            config.setFechaFin(fechaFin);
            config.addFiltro("cliente", cliente);
            config.addFiltro("metodo", metodo);
            config.addFiltro("estado", estado);

            byte[] archivo = exportacionServicio.exportarVentas(config);

            HttpHeaders headers = crearHeadersExportacion(formato, fechaInicio, fechaFin);

            logger.info("Exportando historial de ventas - Formato: {}, Período: {} a {}", formato, fechaInicio, fechaFin);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(archivo);

        } catch (Exception e) {
            logger.error("Error al exportar historial de ventas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Duplica una venta existente
     */
    /**
     * Duplica una venta existente
     */
    @PostMapping("/duplicar/{id}")
    public String duplicarVenta(@PathVariable Long id, Model model) {
        try {
            VentaDTO ventaOriginal = ventaServicio.buscarPorId(id);
            if (ventaOriginal == null) {
                return manejarVentaNoEncontrada(model);
            }

            VentaDTO nuevaVenta = ventaServicio.duplicarVenta(ventaOriginal);

            // Redirigir al formulario de edición de la nueva venta
            return "redirect:/ventas/editar/" + nuevaVenta.getId() + "?duplicada=true";

        } catch (Exception e) {
            return manejarErrorDuplicacion(e, model);
        }
    }

    private String manejarVentaNoEncontrada(Model model) {
        model.addAttribute("mensaje", "Venta no encontrada");
        model.addAttribute("tipoMensaje", "error");
        return "redirect:/ventas/historial";
    }

    private String manejarErrorDuplicacion(Exception e, Model model) {
        logger.error("Error al duplicar venta: {}", e.getMessage());
        model.addAttribute("mensaje", "Error al duplicar la venta: " + e.getMessage());
        model.addAttribute("tipoMensaje", "error");
        return "redirect:/ventas/historial";
    }

    /**
     * Procesa una devolución
     */
    /**
     * Procesa una devolución
     */
    /**
     * Procesa una devolución
     */
    @GetMapping("/devolucion/{id}")
    public String procesarDevolucion(@PathVariable Long id, Model model) {
        try {
            VentaDTO venta = ventaServicio.buscarPorId(id);
            if (venta == null) {
                return manejarVentaNoEncontrada(model);
            }

            if (!"completada".equals(venta.getEstado())) {
                return manejarEstadoInvalido(model);
            }

            // Cargar vista de devolución
            model.addAttribute("venta", venta);
            model.addAttribute("ventaDTO", venta);

            return "ventas/devolucion";

        } catch (Exception e) {
            return manejarErrorProcesamiento(e, model, "devolución");
        }
    }

    private String manejarEstadoInvalido(Model model) {
        model.addAttribute("mensaje", "Solo se pueden procesar devoluciones de ventas completadas");
        model.addAttribute("tipoMensaje", "error");
        return "redirect:/ventas/historial";
    }

    private String manejarErrorProcesamiento(Exception e, Model model, String operacion) {
        logger.error("Error al procesar {}: {}", operacion, e.getMessage());
        model.addAttribute("mensaje", "Error al procesar la " + operacion + ": " + e.getMessage());
        model.addAttribute("tipoMensaje", "error");
        return "redirect:/ventas/historial";
    }

    /**
     * Genera e imprime factura
     */
    @GetMapping("/factura/{id}")
    public String generarFactura(@PathVariable Long id, Model model) {
        try {
            VentaDTO venta = ventaServicio.buscarPorId(id);
            if (venta == null) {
                model.addAttribute("mensaje", "Venta no encontrada");
                model.addAttribute("tipoMensaje", "error");
                return "redirect:/ventas/historial";
            }

            model.addAttribute("venta", venta);

            return "ventas/factura";

        } catch (Exception e) {
            logger.error("Error al generar factura: {}", e.getMessage());
            model.addAttribute("mensaje", "Error al generar la factura: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
            return "redirect:/ventas/historial";
        }
    }

    /**
     * API REST para obtener estadísticas rápidas
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticasRapidas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            if (fechaInicio == null) fechaInicio = LocalDate.now().minusMonths(1);
            if (fechaFin == null) fechaFin = LocalDate.now();

            return calcularEstadisticasPeriodo(fechaInicio, fechaFin, null, null, null);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas rápidas: {}", e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Error al calcular estadísticas");
            return errorMap;
        }
    }

    /**
     * Calcula estadísticas para un período específico
     */
    private Map<String, Object> calcularEstadisticasPeriodo(
            LocalDate fechaInicio, LocalDate fechaFin,
            String cliente, String metodo, String estado) {

        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Obtener ventas del período para cálculos
            // Nota: Los parámetros cliente, metodo son nombres de String, no IDs
            // Para simplificar, usamos el método con solo fechas y filtros básicos
            List<VentaDTO> ventasPeriodo = ventaServicio.buscarVentasParaExportar(
                    fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), estado, metodo, null);

            // Calcular totales
            BigDecimal totalIngresos = ventasPeriodo.stream()
                    .filter(v -> "completada".equals(v.getEstado()))
                    .map(VentaDTO::getTotal)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long ventasCompletadas = ventasPeriodo.stream()
                    .mapToLong(v -> "completada".equals(v.getEstado()) ? 1 : 0)
                    .sum();

            BigDecimal promedioVenta = ventasCompletadas > 0 ?
                    totalIngresos.divide(BigDecimal.valueOf(ventasCompletadas), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            long clientesUnicos = ventasPeriodo.stream()
                    .map(VentaDTO::getClienteId)
                    .collect(Collectors.toSet())
                    .size();

            estadisticas.put("totalIngresos", totalIngresos.doubleValue());
            estadisticas.put("promedioVenta", promedioVenta.doubleValue());
            estadisticas.put("clientesUnicos", clientesUnicos);
            estadisticas.put("ventasCompletadas", ventasCompletadas);

        } catch (Exception e) {
            logger.error("Error al calcular estadísticas del período: {}", e.getMessage());
            estadisticas.put("totalIngresos", 0.0);
            estadisticas.put("promedioVenta", 0.0);
            estadisticas.put("clientesUnicos", 0L);
            estadisticas.put("ventasCompletadas", 0L);
        }

        return estadisticas;
    }
}