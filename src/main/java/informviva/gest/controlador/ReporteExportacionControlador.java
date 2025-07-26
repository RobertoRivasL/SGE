package informviva.gest.controlador;



/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.dto.ProductoReporteDTO;
import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para la exportación de reportes en diferentes formatos
 */
@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
public class ReporteExportacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ReporteExportacionControlador.class);

    @Autowired
    private ReporteServicio reporteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    /**
     * Muestra el reporte de productos
     */
    @GetMapping("/productos")
    public String mostrarReporteProductos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) String ventas,
            Model modelo) {

        try {
            // Obtener todos los productos
            List<Producto> productos = productoServicio.listar();

            // Aplicar filtros
            if (categoria != null && !categoria.isEmpty()) {
                productos = productos.stream()
                        .filter(p -> p.getCategoria() != null &&
                                p.getCategoria().getNombre().equalsIgnoreCase(categoria))
                        .collect(Collectors.toList());
            }

            if (stock != null && !stock.isEmpty()) {
                productos = aplicarFiltroStock(productos, stock);
            }

            // Obtener estadísticas
            Map<String, Object> estadisticas = generarEstadisticasProductos(productos);

            // Top productos más vendidos
            List<ProductoReporteDTO> topVendidos = obtenerTopProductosVendidos(10);

            // Productos con bajo stock
            List<Producto> productosBajoStock = productoServicio.listarConBajoStock(10);

            // Categorías disponibles
            List<String> categorias = productoServicio.listarCategorias();

            modelo.addAttribute("productos", productos);
            modelo.addAttribute("estadisticas", estadisticas);
            modelo.addAttribute("topVendidos", topVendidos);
            modelo.addAttribute("productosBajoStock", productosBajoStock);
            modelo.addAttribute("categorias", categorias);

            return "reportes/productos";

        } catch (Exception e) {
            logger.error("Error al generar reporte de productos: {}", e.getMessage());
            modelo.addAttribute("mensajeError", "Error al generar el reporte");
            return "error/default";
        }
    }

    /**
     * Exporta reportes de ventas
     */
    @GetMapping("/exportar/ventas")
    public ResponseEntity<byte[]> exportarReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "excel") String formato) {

        try {
            VentaResumenDTO resumen = reporteServicio.generarResumenVentas(startDate, endDate);
            List<Venta> ventas = ventaServicio.buscarPorRangoFechas(startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1).minusNanos(1));

            byte[] data;
            String fileName;
            String contentType;

            if ("pdf".equalsIgnoreCase(formato)) {
                data = generarReporteVentasPDF(resumen, ventas);
                fileName = "reporte_ventas_" + startDate + "_" + endDate + ".pdf";
                contentType = "application/pdf";
            } else {
                data = generarReporteVentasExcel(resumen, ventas);
                fileName = "reporte_ventas_" + startDate + "_" + endDate + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);

        } catch (Exception e) {
            logger.error("Error al exportar reporte de ventas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporta reportes de productos
     */
    @GetMapping("/productos/exportar")
    public ResponseEntity<byte[]> exportarReporteProductos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String stock,
            @RequestParam(defaultValue = "excel") String formato) {

        try {
            // Obtener productos con filtros aplicados
            List<Producto> productos = productoServicio.listar();

            if (categoria != null && !categoria.isEmpty()) {
                productos = productos.stream()
                        .filter(p -> p.getCategoria() != null &&
                                p.getCategoria().getNombre().equalsIgnoreCase(categoria))
                        .collect(Collectors.toList());
            }

            if (stock != null && !stock.isEmpty()) {
                productos = aplicarFiltroStock(productos, stock);
            }

            byte[] data;
            String fileName;
            String contentType;

            if ("pdf".equalsIgnoreCase(formato)) {
                data = generarReporteProductosPDF(productos);
                fileName = "reporte_productos_" + LocalDate.now() + ".pdf";
                contentType = "application/pdf";
            } else {
                data = generarReporteProductosExcel(productos);
                fileName = "reporte_productos_" + LocalDate.now() + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);

        } catch (Exception e) {
            logger.error("Error al exportar reporte de productos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para obtener datos de dashboard en tiempo real
     */
    @GetMapping("/api/dashboard-data")
    public ResponseEntity<Map<String, Object>> obtenerDatosDashboard(
            @RequestParam(defaultValue = "semana") String periodo) {

        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicio;
            LocalDate fin = hoy;

            switch (periodo) {
                case "hoy":
                    inicio = hoy;
                    break;
                case "mes":
                    inicio = hoy.withDayOfMonth(1);
                    break;
                case "trimestre":
                    int quarterMonth = (hoy.getMonthValue() - 1) / 3 * 3 + 1;
                    inicio = hoy.withMonth(quarterMonth).withDayOfMonth(1);
                    break;
                case "año":
                    inicio = hoy.withDayOfYear(1);
                    break;
                case "semana":
                default:
                    inicio = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                    break;
            }

            VentaResumenDTO resumen = reporteServicio.generarResumenVentas(inicio, fin);
            Map<String, Object> estadisticasProductos = generarEstadisticasProductos(productoServicio.listar());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ventas", resumen);
            respuesta.put("productos", estadisticasProductos);
            respuesta.put("periodo", periodo);
            respuesta.put("fechaInicio", inicio);
            respuesta.put("fechaFin", fin);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al obtener datos del dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Métodos auxiliares

    private List<Producto> aplicarFiltroStock(List<Producto> productos, String filtro) {
        return productos.stream().filter(p -> {
            int stock = p.getStock() != null ? p.getStock() : 0;
            switch (filtro) {
                case "alto":
                    return stock > 50;
                case "medio":
                    return stock >= 10 && stock <= 50;
                case "bajo":
                    return stock > 0 && stock < 10;
                case "agotado":
                    return stock == 0;
                default:
                    return true;
            }
        }).collect(Collectors.toList());
    }

    private Map<String, Object> generarEstadisticasProductos(List<Producto> productos) {
        Map<String, Object> estadisticas = new HashMap<>();

        long totalProductos = productos.size();
        long productosActivos = productos.stream().mapToLong(p -> p.isActivo() ? 1 : 0).sum();
        long productosBajoStock = productos.stream().mapToLong(p ->
                (p.getStock() != null && p.getStock() > 0 && p.getStock() < 10) ? 1 : 0).sum();
        long productosAgotados = productos.stream().mapToLong(p ->
                (p.getStock() == null || p.getStock() == 0) ? 1 : 0).sum();

        estadisticas.put("totalProductos", totalProductos);
        estadisticas.put("productosActivos", productosActivos);
        estadisticas.put("productosBajoStock", productosBajoStock);
        estadisticas.put("productosAgotados", productosAgotados);

        // Distribución por categorías
        Map<String, Long> distribucionCategorias = productos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                        Collectors.counting()
                ));

        // Distribución por stock
        Map<String, Long> distribucionStock = new HashMap<>();
        distribucionStock.put("alto", productos.stream().mapToLong(p ->
                (p.getStock() != null && p.getStock() > 50) ? 1 : 0).sum());
        distribucionStock.put("medio", productos.stream().mapToLong(p ->
                (p.getStock() != null && p.getStock() >= 10 && p.getStock() <= 50) ? 1 : 0).sum());
        distribucionStock.put("bajo", productosBajoStock);
        distribucionStock.put("agotado", productosAgotados);

        estadisticas.put("distribucionCategorias", distribucionCategorias);
        estadisticas.put("distribucionStock", distribucionStock);

        return estadisticas;
    }

    private List<ProductoReporteDTO> obtenerTopProductosVendidos(int limite) {
        // Esta sería una implementación simplificada
        // En un escenario real, consultarías la base de datos para obtener las estadísticas de ventas
        return productoServicio.listar().stream()
                .limit(limite)
                .map(p -> {
                    ProductoReporteDTO dto = new ProductoReporteDTO();
                    dto.setId(p.getId());
                    dto.setCodigo(p.getCodigo());
                    dto.setNombre(p.getNombre());
                    dto.setCategoria(p.getCategoria());
                    dto.setStock(p.getStock());
                    // Simular datos de ventas (en producción estos vendrían de la BD)
                    dto.setUnidadesVendidas((int) (Math.random() * 100));
                    dto.setIngresos(java.math.BigDecimal.valueOf(Math.random() * 10000));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private byte[] generarReporteVentasExcel(VentaResumenDTO resumen, List<Venta> ventas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear encabezados de resumen
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("RESUMEN DE VENTAS");
            headerRow.getCell(0).setCellStyle(headerStyle);

            Row summaryRow1 = sheet.createRow(2);
            summaryRow1.createCell(0).setCellValue("Total Ventas:");
            summaryRow1.createCell(1).setCellValue(resumen.getTotalVentas().doubleValue());

            Row summaryRow2 = sheet.createRow(3);
            summaryRow2.createCell(0).setCellValue("Total Transacciones:");
            summaryRow2.createCell(1).setCellValue(resumen.getTotalTransacciones());

            Row summaryRow3 = sheet.createRow(4);
            summaryRow3.createCell(0).setCellValue("Ticket Promedio:");
            summaryRow3.createCell(1).setCellValue(resumen.getTicketPromedio().doubleValue());

            // Crear tabla de ventas detalladas
            Row detailHeaderRow = sheet.createRow(7);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Total", "Estado"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = detailHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos de ventas
            int rowIdx = 8;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(venta.getId());
                row.createCell(1).setCellValue(venta.getFecha().toString());
                row.createCell(2).setCellValue(venta.getCliente().getNombreCompleto());
                row.createCell(3).setCellValue(venta.getVendedor().getNombreCompleto());
                row.createCell(4).setCellValue(venta.getTotal());
                row.createCell(5).setCellValue(venta.getEstado() != null ? venta.getEstado().toString() : "COMPLETADA");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] generarReporteProductosExcel(List<Producto> productos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Productos");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Código", "Nombre", "Descripción", "Categoría", "Precio", "Stock", "Estado"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(producto.getCodigo());
                row.createCell(1).setCellValue(producto.getNombre());
                row.createCell(2).setCellValue(producto.getDescripcion());
                row.createCell(3).setCellValue(producto.getCategoria() != null ?
                        producto.getCategoria().getNombre() : "Sin categoría");
                row.createCell(4).setCellValue(producto.getPrecio());
                row.createCell(5).setCellValue(producto.getStock());
                row.createCell(6).setCellValue(producto.isActivo() ? "Activo" : "Inactivo");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] generarReporteVentasPDF(VentaResumenDTO resumen, List<Venta> ventas) {
        // Implementación simplificada para PDF
        // En producción usarías iText o similar
        return "PDF de ventas no implementado aún".getBytes();
    }

    private byte[] generarReporteProductosPDF(List<Producto> productos) {
        // Implementación simplificada para PDF
        // En producción usarías iText o similar
        return "PDF de productos no implementado aún".getBytes();
    }
}