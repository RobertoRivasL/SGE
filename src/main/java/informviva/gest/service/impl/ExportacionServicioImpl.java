package informviva.gest.service.impl;


import informviva.gest.dto.*;
import informviva.gest.model.*;
import informviva.gest.service.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de exportación unificado
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ExportacionServicioImpl implements ExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionServicioImpl.class);

    // Formatos soportados
    public static final String FORMATO_PDF = "PDF";
    public static final String FORMATO_EXCEL = "EXCEL";
    public static final String FORMATO_CSV = "CSV";

    // Tipos MIME
    private static final Map<String, String> MIME_TYPES = Map.of(
            FORMATO_PDF, "application/pdf",
            FORMATO_EXCEL, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            FORMATO_CSV, "text/csv"
    );

    // Extensiones de archivo
    private static final Map<String, String> EXTENSIONES = Map.of(
            FORMATO_PDF, ".pdf",
            FORMATO_EXCEL, ".xlsx",
            FORMATO_CSV, ".csv"
    );

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ARCHIVO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    @Qualifier("productoServicioImpl") // Especifica el bean que deseas usar
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ReporteServicio reporteServicio;

    @Override
    public byte[] exportarClientes(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        logger.info("Exportando clientes en formato: {}", formato);

        try {
            List<ClienteExportDTO> clientes = obtenerClientesParaExportacion(fechaInicio, fechaFin, filtros);

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFClientes(clientes);
                case FORMATO_EXCEL -> generarExcelClientes(clientes);
                case FORMATO_CSV -> generarCSVClientes(clientes);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar clientes: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de clientes", e);
        }
    }

    @Override
    public byte[] exportarProductos(String formato, Map<String, Object> filtros) {
        logger.info("Exportando productos en formato: {}", formato);

        try {
            List<ProductoExportDTO> productos = obtenerProductosParaExportacion(filtros);

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFProductos(productos);
                case FORMATO_EXCEL -> generarExcelProductos(productos);
                case FORMATO_CSV -> generarCSVProductos(productos);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar productos: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de productos", e);
        }
    }

    @Override
    public byte[] exportarVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        logger.info("Exportando ventas en formato: {} para período: {} - {}", formato, fechaInicio, fechaFin);

        try {
            List<VentaExportDTO> ventas = obtenerVentasParaExportacion(fechaInicio, fechaFin, filtros);

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFVentas(ventas);
                case FORMATO_EXCEL -> generarExcelVentas(ventas);
                case FORMATO_CSV -> generarCSVVentas(ventas);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar ventas: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de ventas", e);
        }
    }

    @Override
    public byte[] exportarUsuarios(String formato, Map<String, Object> filtros) {
        logger.info("Exportando usuarios en formato: {}", formato);

        try {
            List<UsuarioExportDTO> usuarios = obtenerUsuariosParaExportacion(filtros);

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFUsuarios(usuarios);
                case FORMATO_EXCEL -> generarExcelUsuarios(usuarios);
                case FORMATO_CSV -> generarCSVUsuarios(usuarios);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar usuarios: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de usuarios", e);
        }
    }

    @Override
    public byte[] exportarReporteVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoReporte) {
        logger.info("Exportando reporte de ventas tipo: {} en formato: {}", tipoReporte, formato);

        try {
            // Convertir LocalDateTime a LocalDate para compatibilidad con ReporteServicio existente
            VentaResumenDTO resumen = reporteServicio.generarResumenVentas(
                    fechaInicio.toLocalDate(),
                    fechaFin.toLocalDate()
            );

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFReporteVentas(resumen, tipoReporte, fechaInicio, fechaFin);
                case FORMATO_EXCEL -> generarExcelReporteVentas(resumen, tipoReporte, fechaInicio, fechaFin);
                case FORMATO_CSV -> generarCSVReporteVentas(resumen, tipoReporte, fechaInicio, fechaFin);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar reporte de ventas: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación del reporte de ventas", e);
        }
    }

    @Override
    public byte[] exportarInventario(String formato, boolean incluirBajoStock, Integer umbralStock) {
        logger.info("Exportando inventario en formato: {}, bajo stock: {}", formato, incluirBajoStock);

        try {
            List<ProductoExportDTO> productos = incluirBajoStock ?
                    obtenerProductosBajoStock(umbralStock) :
                    obtenerProductosParaExportacion(new HashMap<>());

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFInventario(productos, incluirBajoStock, umbralStock);
                case FORMATO_EXCEL -> generarExcelInventario(productos, incluirBajoStock, umbralStock);
                case FORMATO_CSV -> generarCSVInventario(productos, incluirBajoStock, umbralStock);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar inventario: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación del inventario", e);
        }
    }

    @Override
    public byte[] exportarReporteFinanciero(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean incluirComparativo) {
        logger.info("Exportando reporte financiero en formato: {}", formato);

        try {
            ReporteFinancieroDTO reporte = generarReporteFinanciero(fechaInicio, fechaFin, incluirComparativo);

            return switch (formato.toUpperCase()) {
                case FORMATO_PDF -> generarPDFReporteFinanciero(reporte);
                case FORMATO_EXCEL -> generarExcelReporteFinanciero(reporte);
                case FORMATO_CSV -> generarCSVReporteFinanciero(reporte);
                default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
            };

        } catch (Exception e) {
            logger.error("Error al exportar reporte financiero: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación del reporte financiero", e);
        }
    }

    // Métodos auxiliares para obtener datos

    private List<ClienteExportDTO> obtenerClientesParaExportacion(LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        List<Cliente> clientes = clienteServicio.obtenerTodos();

        return clientes.stream()
                .filter(cliente -> aplicarFiltrosCliente(cliente, fechaInicio, fechaFin, filtros))
                .map(this::convertirAClienteExportDTO)
                .collect(Collectors.toList());
    }

    private List<ProductoExportDTO> obtenerProductosParaExportacion(Map<String, Object> filtros) {
        List<Producto> productos = productoServicio.listar();

        return productos.stream()
                .filter(producto -> aplicarFiltrosProducto(producto, filtros))
                .map(this::convertirAProductoExportDTO)
                .collect(Collectors.toList());
    }

    private List<ProductoExportDTO> obtenerProductosBajoStock(Integer umbral) {
        if (umbral == null) umbral = 5;
        List<Producto> productos = productoServicio.listarConBajoStock(umbral);

        return productos.stream()
                .map(this::convertirAProductoExportDTO)
                .collect(Collectors.toList());
    }

    private List<VentaExportDTO> obtenerVentasParaExportacion(LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        List<Venta> ventas = ventaServicio.buscarPorRangoFechas(
                fechaInicio.toLocalDate(),
                fechaFin.toLocalDate()
        );

        return ventas.stream()
                .filter(venta -> aplicarFiltrosVenta(venta, filtros))
                .map(this::convertirAVentaExportDTO)
                .collect(Collectors.toList());
    }

    private List<UsuarioExportDTO> obtenerUsuariosParaExportacion(Map<String, Object> filtros) {
        List<Usuario> usuarios = usuarioServicio.listarTodos();

        return usuarios.stream()
                .filter(usuario -> aplicarFiltrosUsuario(usuario, filtros))
                .map(this::convertirAUsuarioExportDTO)
                .collect(Collectors.toList());
    }

    // Métodos de filtrado

    private boolean aplicarFiltrosCliente(Cliente cliente, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        // Filtro por fechas usando fechaRegistro (LocalDate) convertida a LocalDateTime
        if (fechaInicio != null && cliente.getFechaRegistro() != null) {
            LocalDateTime fechaRegistroDateTime = cliente.getFechaRegistro().atStartOfDay();
            if (fechaRegistroDateTime.isBefore(fechaInicio)) {
                return false;
            }
        }

        if (fechaFin != null && cliente.getFechaRegistro() != null) {
            LocalDateTime fechaRegistroDateTime = cliente.getFechaRegistro().atTime(23, 59, 59);
            if (fechaRegistroDateTime.isAfter(fechaFin)) {
                return false;
            }
        }

        // Aplicar otros filtros
        if (filtros != null) {
            String busqueda = (String) filtros.get("busqueda");
            if (busqueda != null && !busqueda.isEmpty()) {
                String busquedaLower = busqueda.toLowerCase();
                return cliente.getNombre().toLowerCase().contains(busquedaLower) ||
                        cliente.getApellido().toLowerCase().contains(busquedaLower) ||
                        (cliente.getEmail() != null && cliente.getEmail().toLowerCase().contains(busquedaLower));
            }
        }

        return true;
    }

    private boolean aplicarFiltrosProducto(Producto producto, Map<String, Object> filtros) {
        if (filtros == null) return true;

        Boolean soloActivos = (Boolean) filtros.get("soloActivos");
        if (soloActivos != null && soloActivos && !producto.isActivo()) {
            return false;
        }

        Boolean soloConStock = (Boolean) filtros.get("soloConStock");
        if (soloConStock != null && soloConStock && (producto.getStock() == null || producto.getStock() <= 0)) {
            return false;
        }

        String categoria = (String) filtros.get("categoria");
        if (categoria != null && !categoria.isEmpty() && producto.getCategoria() != null) {
            return producto.getCategoria().getNombre().equalsIgnoreCase(categoria);
        }

        return true;
    }

    private boolean aplicarFiltrosVenta(Venta venta, Map<String, Object> filtros) {
        if (filtros == null) return true;

        String estado = (String) filtros.get("estado");
        if (estado != null && !estado.isEmpty()) {
            return estado.equalsIgnoreCase(venta.getEstado());
        }

        return true;
    }

    private boolean aplicarFiltrosUsuario(Usuario usuario, Map<String, Object> filtros) {
        if (filtros == null) return true;

        Boolean soloActivos = (Boolean) filtros.get("soloActivos");
        return soloActivos == null || !soloActivos || usuario.isActivo();
    }

    // Métodos de conversión a DTOs

    private ClienteExportDTO convertirAClienteExportDTO(Cliente cliente) {
        ClienteExportDTO dto = new ClienteExportDTO();
        dto.setId(cliente.getId());
        dto.setRut(cliente.getRut());
        dto.setNombreCompleto(cliente.getNombreCompleto());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setFechaRegistro(cliente.getFechaRegistro() != null ?
                cliente.getFechaRegistro().atStartOfDay() : null);
        dto.setCategoria(cliente.getCategoria());

        // Obtener estadísticas de compras
        Long totalCompras = ventaServicio.contarVentasPorCliente(cliente.getId());
        Double montoTotalCompras = ventaServicio.calcularTotalVentasPorCliente(cliente.getId());

        dto.setTotalCompras(totalCompras != null ? totalCompras : 0L);
        dto.setMontoTotalCompras(montoTotalCompras != null ? montoTotalCompras : 0.0);

        return dto;
    }

    private ProductoExportDTO convertirAProductoExportDTO(Producto producto) {
        ProductoExportDTO dto = new ProductoExportDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría");
        dto.setMarca(producto.getMarca());
        dto.setModelo(producto.getModelo());
        dto.setActivo(producto.isActivo());
        dto.setFechaCreacion(producto.getFechaCreacion());
        dto.setFechaActualizacion(producto.getFechaActualizacion());

        return dto;
    }

    private VentaExportDTO convertirAVentaExportDTO(Venta venta) {
        VentaExportDTO dto = new VentaExportDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setClienteNombre(venta.getCliente().getNombreCompleto());
        dto.setClienteRut(venta.getCliente().getRut());
        dto.setVendedorNombre(venta.getVendedor().getNombreCompleto());
        dto.setSubtotal(venta.getSubtotal());
        dto.setImpuesto(venta.getImpuesto());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setEstado(venta.getEstado());
        dto.setObservaciones(venta.getObservaciones());

        return dto;
    }

    private UsuarioExportDTO convertirAUsuarioExportDTO(Usuario usuario) {
        UsuarioExportDTO dto = new UsuarioExportDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion() != null ?
                usuario.getFechaCreacion().atStartOfDay() : null);
        dto.setUltimoAcceso(usuario.getUltimoAcceso() != null ?
                usuario.getUltimoAcceso().atStartOfDay() : null);
        dto.setRoles(String.join(", ", usuario.getRoles()));

        return dto;
    }

    // Métodos de generación de reportes específicos

    private ReporteFinancieroDTO generarReporteFinanciero(LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean incluirComparativo) {
        ReporteFinancieroDTO reporte = new ReporteFinancieroDTO();

        // Calcular período actual
        Double ventasActuales = ventaServicio.calcularTotalVentas(fechaInicio.toLocalDate(), fechaFin.toLocalDate());
        Long transaccionesActuales = ventaServicio.contarTransacciones(fechaInicio.toLocalDate(), fechaFin.toLocalDate());

        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setVentasTotales(ventasActuales != null ? ventasActuales : 0.0);
        reporte.setTransaccionesTotales(transaccionesActuales != null ? transaccionesActuales : 0L);

        if (incluirComparativo) {
            // Calcular período anterior
            long diasPeriodo = java.time.Duration.between(fechaInicio, fechaFin).toDays();
            LocalDateTime inicioAnterior = fechaInicio.minusDays(diasPeriodo);
            LocalDateTime finAnterior = fechaInicio.minusDays(1);

            Double ventasAnteriores = ventaServicio.calcularTotalVentas(inicioAnterior.toLocalDate(), finAnterior.toLocalDate());
            Long transaccionesAnteriores = ventaServicio.contarTransacciones(inicioAnterior.toLocalDate(), finAnterior.toLocalDate());

            reporte.setVentasAnteriores(ventasAnteriores != null ? ventasAnteriores : 0.0);
            reporte.setTransaccionesAnteriores(transaccionesAnteriores != null ? transaccionesAnteriores : 0L);

            // Calcular porcentajes de cambio
            Double porcentajeCambioVentas = ventaServicio.calcularPorcentajeCambio(ventasActuales, ventasAnteriores);
            Double porcentajeCambioTransacciones = ventaServicio.calcularPorcentajeCambio(
                    transaccionesActuales != null ? transaccionesActuales.doubleValue() : 0.0,
                    transaccionesAnteriores != null ? transaccionesAnteriores.doubleValue() : 0.0
            );

            reporte.setPorcentajeCambioVentas(porcentajeCambioVentas);
            reporte.setPorcentajeCambioTransacciones(porcentajeCambioTransacciones);
        }

        return reporte;
    }

    // Métodos de generación PDF (simplificados - implementar según necesidades)

    private byte[] generarPDFClientes(List<ClienteExportDTO> clientes) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, output);
            document.open();

            // Título
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("Listado de Clientes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Tabla
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Encabezados
            String[] headers = {"RUT", "Nombre", "Email", "Teléfono", "Fecha Registro", "Total Compras"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, java.awt.Color.RED)));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Datos
            for (ClienteExportDTO cliente : clientes) {
                table.addCell(cliente.getRut() != null ? cliente.getRut() : "");
                table.addCell(cliente.getNombreCompleto());
                table.addCell(cliente.getEmail() != null ? cliente.getEmail() : "");
                table.addCell(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                table.addCell(cliente.getFechaRegistro() != null ?
                        cliente.getFechaRegistro().format(FECHA_FORMATTER) : "");
                table.addCell(String.valueOf(cliente.getTotalCompras()));
            }

            document.add(table);
            document.close();
            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de clientes", e);
        }
    }

    // Implementar métodos similares para otros tipos de PDF...
    private byte[] generarPDFProductos(List<ProductoExportDTO> productos) {
        // Implementación similar a generarPDFClientes
        return new byte[0]; // Placeholder
    }

    private byte[] generarPDFVentas(List<VentaExportDTO> ventas) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarPDFUsuarios(List<UsuarioExportDTO> usuarios) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarPDFReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Implementación para reportes de ventas
        return new byte[0]; // Placeholder
    }

    private byte[] generarPDFInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) {
        // Implementación para inventario
        return new byte[0]; // Placeholder
    }

    private byte[] generarPDFReporteFinanciero(ReporteFinancieroDTO reporte) {
        // Implementación para reporte financiero
        return new byte[0]; // Placeholder
    }

    // Métodos de generación Excel

    private byte[] generarExcelClientes(List<ClienteExportDTO> clientes) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Clientes");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "RUT", "Nombre Completo", "Email", "Teléfono", "Dirección",
                    "Fecha Registro", "Categoría", "Total Compras", "Monto Total"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 1;
            for (ClienteExportDTO cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(cliente.getId());
                row.createCell(1).setCellValue(cliente.getRut() != null ? cliente.getRut() : "");
                row.createCell(2).setCellValue(cliente.getNombreCompleto());
                row.createCell(3).setCellValue(cliente.getEmail() != null ? cliente.getEmail() : "");
                row.createCell(4).setCellValue(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                row.createCell(5).setCellValue(cliente.getDireccion() != null ? cliente.getDireccion() : "");
                row.createCell(6).setCellValue(cliente.getFechaRegistro() != null ?
                        cliente.getFechaRegistro().format(FECHA_FORMATTER) : "");
                row.createCell(7).setCellValue(cliente.getCategoria() != null ? cliente.getCategoria() : "");
                row.createCell(8).setCellValue(cliente.getTotalCompras());
                row.createCell(9).setCellValue(cliente.getMontoTotalCompras());
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de clientes", e);
        }
    }

    // Implementar métodos similares para otros tipos de Excel...
    private byte[] generarExcelProductos(List<ProductoExportDTO> productos) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarExcelVentas(List<VentaExportDTO> ventas) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarExcelUsuarios(List<UsuarioExportDTO> usuarios) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarExcelReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Implementación para reportes de ventas
        return new byte[0]; // Placeholder
    }

    private byte[] generarExcelInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) {
        // Implementación para inventario
        return new byte[0]; // Placeholder
    }

    private byte[] generarExcelReporteFinanciero(ReporteFinancieroDTO reporte) {
        // Implementación para reporte financiero
        return new byte[0]; // Placeholder
    }

    // Métodos de generación CSV

    private byte[] generarCSVClientes(List<ClienteExportDTO> clientes) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,RUT,Nombre Completo,Email,Teléfono,Dirección,Fecha Registro,Categoría,Total Compras,Monto Total\n");

        // Datos
        for (ClienteExportDTO cliente : clientes) {
            csv.append(cliente.getId()).append(",");
            csv.append("\"").append(cliente.getRut() != null ? cliente.getRut() : "").append("\",");
            csv.append("\"").append(cliente.getNombreCompleto()).append("\",");
            csv.append("\"").append(cliente.getEmail() != null ? cliente.getEmail() : "").append("\",");
            csv.append("\"").append(cliente.getTelefono() != null ? cliente.getTelefono() : "").append("\",");
            csv.append("\"").append(cliente.getDireccion() != null ? cliente.getDireccion() : "").append("\",");
            csv.append(cliente.getFechaRegistro() != null ?
                    cliente.getFechaRegistro().format(FECHA_FORMATTER) : "").append(",");
            csv.append("\"").append(cliente.getCategoria() != null ? cliente.getCategoria() : "").append("\",");
            csv.append(cliente.getTotalCompras()).append(",");
            csv.append(cliente.getMontoTotalCompras());
            csv.append("\n");
        }

        return csv.toString().getBytes();
    }

    // Implementar métodos similares para otros tipos de CSV...
    private byte[] generarCSVProductos(List<ProductoExportDTO> productos) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarCSVVentas(List<VentaExportDTO> ventas) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarCSVUsuarios(List<UsuarioExportDTO> usuarios) {
        // Implementación similar
        return new byte[0]; // Placeholder
    }

    private byte[] generarCSVReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Implementación para reportes de ventas
        return new byte[0]; // Placeholder
    }

    private byte[] generarCSVInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) {
        // Implementación para inventario
        return new byte[0]; // Placeholder
    }

    private byte[] generarCSVReporteFinanciero(ReporteFinancieroDTO reporte) {
        // Implementación para reporte financiero
        return new byte[0]; // Placeholder
    }

    @Override
    public List<String> getFormatosSoportados() {
        return List.of(FORMATO_PDF, FORMATO_EXCEL, FORMATO_CSV);
    }

    @Override
    public boolean isFormatoSoportado(String formato) {
        return getFormatosSoportados().contains(formato.toUpperCase());
    }

    @Override
    public String getMimeType(String formato) {
        return MIME_TYPES.get(formato.toUpperCase());
    }

    @Override
    public String getExtensionArchivo(String formato) {
        return EXTENSIONES.get(formato.toUpperCase());
    }

    @Override
    public byte[] exportarVentasExcel(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Implementación básica para exportar ventas en formato Excel
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Agregar datos
            int rowIdx = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(venta.getId());
                row.createCell(1).setCellValue(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                row.createCell(2).setCellValue(venta.getCliente().getNombreCompleto());
                row.createCell(3).setCellValue(venta.getVendedor().getNombreCompleto());
                row.createCell(4).setCellValue(venta.getSubtotal());
                row.createCell(5).setCellValue(venta.getImpuesto());
                row.createCell(6).setCellValue(venta.getTotal());
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de ventas", e);
        }
    }
}