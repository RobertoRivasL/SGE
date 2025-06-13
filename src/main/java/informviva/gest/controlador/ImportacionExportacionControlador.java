package informviva.gest.controlador;


import informviva.gest.service.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para funcionalidades de importación y exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/admin/importacion-exportacion")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ImportacionExportacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionExportacionControlador.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private ProductoExportacionServicio productoExportacionServicio;

    @Autowired
    private VentaExportacionServicio ventaExportacionServicio;

    @Autowired
    private ProductoImportacionServicio productoImportacionServicio;

    @Autowired
    private ClienteImportacionServicio clienteImportacionServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    /**
     * Página principal de importación/exportación
     */
    @GetMapping
    public String mostrarPaginaPrincipal(Model model) {
        // Estadísticas básicas
        model.addAttribute("totalProductos", productoServicio.contarTodos());
        model.addAttribute("totalVentas", ventaServicio.listarTodas().size());
        model.addAttribute("fechaActual", LocalDate.now());

        return "admin/importacion-exportacion";
    }

    // ========== EXPORTACIONES ==========

    /**
     * Exportar productos a Excel
     */
    @GetMapping("/exportar/productos/excel")
    public ResponseEntity<byte[]> exportarProductosExcel(
            @RequestParam(defaultValue = "false") boolean soloActivos,
            @RequestParam(defaultValue = "false") boolean soloBajoStock) {

        try {
            var productos = soloActivos ? productoServicio.listarActivos() : productoServicio.listar();

            if (soloBajoStock) {
                productos = productoServicio.listarConBajoStock(5);
            }

            byte[] excelData = productoExportacionServicio.exportarProductosAExcel(productos);

            String filename = "productos_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            logger.error("Error exportando productos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exportar productos con bajo stock
     */
    @GetMapping("/exportar/productos/bajo-stock")
    public ResponseEntity<byte[]> exportarProductosBajoStock(
            @RequestParam(defaultValue = "5") int umbral) {

        try {
            var productos = productoServicio.listarConBajoStock(umbral);
            byte[] excelData = productoExportacionServicio.exportarProductosBajoStock(productos, umbral);

            String filename = "productos_bajo_stock_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            logger.error("Error exportando productos bajo stock: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exportar productos a CSV
     */
    @GetMapping("/exportar/productos/csv")
    public ResponseEntity<byte[]> exportarProductosCSV() {
        try {
            var productos = productoServicio.listar();
            byte[] csvData = productoExportacionServicio.exportarProductosACSV(productos);

            String filename = "productos_" + LocalDate.now().format(DATE_FORMATTER) + ".csv";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvData);

        } catch (Exception e) {
            logger.error("Error exportando productos CSV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exportar ventas a Excel
     */
    @GetMapping("/exportar/ventas/excel")
    public ResponseEntity<byte[]> exportarVentasExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            // Fechas por defecto: último mes
            if (fechaInicio == null) fechaInicio = LocalDate.now().minusMonths(1);
            if (fechaFin == null) fechaFin = LocalDate.now();

            var ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
            byte[] excelData = ventaExportacionServicio.exportarVentasAExcel(ventas, fechaInicio, fechaFin);

            String filename = "ventas_" + fechaInicio.format(DATE_FORMATTER) + "_" +
                    fechaFin.format(DATE_FORMATTER) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            logger.error("Error exportando ventas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exportar ventas a CSV
     */
    @GetMapping("/exportar/ventas/csv")
    public ResponseEntity<byte[]> exportarVentasCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            if (fechaInicio == null) fechaInicio = LocalDate.now().minusMonths(1);
            if (fechaFin == null) fechaFin = LocalDate.now();

            var ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
            byte[] csvData = ventaExportacionServicio.exportarVentasACSV(ventas);

            String filename = "ventas_" + fechaInicio.format(DATE_FORMATTER) + "_" +
                    fechaFin.format(DATE_FORMATTER) + ".csv";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvData);

        } catch (Exception e) {
            logger.error("Error exportando ventas CSV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== PLANTILLAS ==========

    /**
     * Descargar plantilla de productos
     */
    @GetMapping("/plantillas/productos")
    public ResponseEntity<byte[]> descargarPlantillaProductos() {
        try {
            byte[] plantilla = productoImportacionServicio.generarPlantillaImportacion();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_productos.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(plantilla);

        } catch (Exception e) {
            logger.error("Error generando plantilla productos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descargar plantilla de clientes
     */
    @GetMapping("/plantillas/clientes")
    public ResponseEntity<byte[]> descargarPlantillaClientes() {
        try {
            byte[] plantilla = clienteImportacionServicio.generarPlantillaImportacion();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_clientes.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(plantilla);

        } catch (Exception e) {
            logger.error("Error generando plantilla clientes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== IMPORTACIONES ==========

    /**
     * Página de importación de productos
     */
    @GetMapping("/importar/productos")
    public String mostrarImportacionProductos(Model model) {
        return "admin/importar-productos";
    }

    /**
     * Procesar importación de productos
     */
    @PostMapping("/importar/productos")
    public String procesarImportacionProductos(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "false") boolean confirmar,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo");
            return "redirect:/admin/importacion-exportacion/importar/productos";
        }

        try {
            var resultado = productoImportacionServicio.importarProductosDesdeExcel(archivo);

            if (!confirmar) {
                // Vista previa
                model.addAttribute("resultado", resultado);
                model.addAttribute("preview", true);
                return "admin/importar-productos";
            } else {
                // Guardar productos
                if (!resultado.getProductosExitosos().isEmpty()) {
                    var resultadoGuardado = productoImportacionServicio.guardarProductosImportados(
                            resultado.getProductosExitosos());

                    redirectAttributes.addFlashAttribute("success",
                            "Importación completada: " + resultadoGuardado.getTotalExitosos() +
                                    " productos guardados, " + resultadoGuardado.getTotalErrores() + " errores");
                } else {
                    redirectAttributes.addFlashAttribute("warning", "No se encontraron productos válidos para importar");
                }

                return "redirect:/admin/importacion-exportacion";
            }

        } catch (Exception e) {
            logger.error("Error en importación de productos: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error procesando archivo: " + e.getMessage());
            return "redirect:/admin/importacion-exportacion/importar/productos";
        }
    }

    /**
     * Página de importación de clientes
     */
    @GetMapping("/importar/clientes")
    public String mostrarImportacionClientes(Model model) {
        return "admin/importar-clientes";
    }

    /**
     * Procesar importación de clientes
     */
    @PostMapping("/importar/clientes")
    public String procesarImportacionClientes(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "excel") String formato,
            @RequestParam(defaultValue = "false") boolean confirmar,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo");
            return "redirect:/admin/importacion-exportacion/importar/clientes";
        }

        try {
            ResultadoImportacionCliente resultado;

            if ("csv".equals(formato)) {
                resultado = clienteImportacionServicio.importarClientesDesdeCSV(archivo);
            } else {
                resultado = clienteImportacionServicio.importarClientesDesdeExcel(archivo);
            }

            if (!confirmar) {
                // Vista previa
                model.addAttribute("resultado", resultado);
                model.addAttribute("formato", formato);
                model.addAttribute("preview", true);
                return "admin/importar-clientes";
            } else {
                // Guardar clientes
                if (!resultado.getClientesExitosos().isEmpty()) {
                    var resultadoGuardado = clienteImportacionServicio.guardarClientesImportados(
                            resultado.getClientesExitosos());

                    redirectAttributes.addFlashAttribute("success",
                            "Importación completada: " + resultadoGuardado.getTotalExitosos() +
                                    " clientes guardados, " + resultadoGuardado.getTotalErrores() + " errores");
                } else {
                    redirectAttributes.addFlashAttribute("warning", "No se encontraron clientes válidos para importar");
                }

                return "redirect:/admin/importacion-exportacion";
            }

        } catch (Exception e) {
            logger.error("Error en importación de clientes: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error procesando archivo: " + e.getMessage());
            return "redirect:/admin/importacion-exportacion/importar/clientes";
        }
    }

    // ========== API REST ENDPOINTS ==========

    /**
     * API para obtener estadísticas de exportación
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Object> obtenerEstadisticas() {
        try {
            var stats = new java.util.HashMap<String, Object>();
            stats.put("totalProductos", productoServicio.contarTodos());
            stats.put("productosActivos", productoServicio.contarActivos());
            stats.put("productosBajoStock", productoServicio.contarConBajoStock(5));
            stats.put("totalVentas", ventaServicio.listarTodas().size());
            stats.put("ventasHoy", ventaServicio.contarVentasHoy());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para validar archivo antes de importar
     */
    @PostMapping("/api/validar-archivo")
    @ResponseBody
    public ResponseEntity<Object> validarArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) {

        try {
            var respuesta = new java.util.HashMap<String, Object>();

            if ("productos".equals(tipo)) {
                var resultado = productoImportacionServicio.importarProductosDesdeExcel(archivo);
                respuesta.put("valido", resultado.getTotalErrores() == 0);
                respuesta.put("errores", resultado.getErrores());
                respuesta.put("totalRegistros", resultado.getTotalProcesados());
            } else if ("clientes".equals(tipo)) {
                var resultado = clienteImportacionServicio.importarClientesDesdeExcel(archivo);
                respuesta.put("valido", resultado.getTotalErrores() == 0);
                respuesta.put("errores", resultado.getErrores());
                respuesta.put("totalRegistros", resultado.getTotalProcesados());
            } else {
                respuesta.put("error", "Tipo de archivo no soportado");
                return ResponseEntity.badRequest().body(respuesta);
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}