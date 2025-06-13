package informviva.gest.service.impl;


import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para generar archivos en diferentes formatos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class GeneradorArchivosServicio {

    private static final Logger logger = LoggerFactory.getLogger(GeneradorArchivosServicio.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===============================
    // GENERADORES PARA USUARIOS
    // ===============================

    public byte[] generarUsuariosExcel(List<Usuario> usuarios) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Usuarios");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Username", "Nombre", "Apellido", "Email",
                    "Activo", "Fecha Creación", "Último Acceso", "Roles"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Usuario usuario : usuarios) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(usuario.getId());
                row.createCell(1).setCellValue(usuario.getUsername());
                row.createCell(2).setCellValue(usuario.getNombre());
                row.createCell(3).setCellValue(usuario.getApellido());
                row.createCell(4).setCellValue(usuario.getEmail());
                row.createCell(5).setCellValue(usuario.isActivo() ? "Sí" : "No");

                if (usuario.getFechaCreacion() != null) {
                    Cell dateCell = row.createCell(6);
                    dateCell.setCellValue(java.sql.Date.valueOf(usuario.getFechaCreacion()));
                    dateCell.setCellStyle(dateStyle);
                }

                if (usuario.getUltimoAcceso() != null) {
                    Cell lastAccessCell = row.createCell(7);
                    lastAccessCell.setCellValue(java.sql.Date.valueOf(usuario.getUltimoAcceso()));
                    lastAccessCell.setCellStyle(dateStyle);
                }

                row.createCell(8).setCellValue(String.join(", ", usuario.getRoles()));
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            logger.info("Archivo Excel de usuarios generado: {} registros", usuarios.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error generando Excel de usuarios: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo Excel", e);
        }
    }

    public byte[] generarUsuariosCSV(List<Usuario> usuarios) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,Username,Nombre,Apellido,Email,Activo,Fecha Creación,Último Acceso,Roles\n");

        // Datos
        for (Usuario usuario : usuarios) {
            csv.append(usuario.getId()).append(",");
            csv.append(escapeCSV(usuario.getUsername())).append(",");
            csv.append(escapeCSV(usuario.getNombre())).append(",");
            csv.append(escapeCSV(usuario.getApellido())).append(",");
            csv.append(escapeCSV(usuario.getEmail())).append(",");
            csv.append(usuario.isActivo() ? "Sí" : "No").append(",");
            csv.append(usuario.getFechaCreacion() != null ? usuario.getFechaCreacion().format(DATE_FORMATTER) : "").append(",");
            csv.append(usuario.getUltimoAcceso() != null ? usuario.getUltimoAcceso().format(DATE_FORMATTER) : "").append(",");
            csv.append(escapeCSV(String.join("; ", usuario.getRoles())));
            csv.append("\n");
        }

        logger.info("Archivo CSV de usuarios generado: {} registros", usuarios.size());
        return csv.toString().getBytes();
    }

    public byte[] generarUsuariosJSON(List<Usuario> usuarios) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            // Crear objetos simplificados para JSON (sin contraseñas)
            List<UsuarioExportDTO> usuariosExport = usuarios.stream()
                    .map(u -> new UsuarioExportDTO(
                            u.getId(),
                            u.getUsername(),
                            u.getNombre(),
                            u.getApellido(),
                            u.getEmail(),
                            u.isActivo(),
                            u.getFechaCreacion(),
                            u.getUltimoAcceso(),
                            u.getRoles()
                    )).toList();

            byte[] json = mapper.writeValueAsBytes(usuariosExport);
            logger.info("Archivo JSON de usuarios generado: {} registros", usuarios.size());
            return json;

        } catch (Exception e) {
            logger.error("Error generando JSON de usuarios: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo JSON", e);
        }
    }

    // ===============================
    // GENERADORES PARA PRODUCTOS
    // ===============================

    public byte[] generarProductosExcel(List<Producto> productos) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Código", "Nombre", "Descripción", "Precio",
                    "Stock", "Categoría", "Marca", "Modelo", "Activo", "Fecha Creación"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(producto.getId());
                row.createCell(1).setCellValue(producto.getCodigo());
                row.createCell(2).setCellValue(producto.getNombre());
                row.createCell(3).setCellValue(producto.getDescripcion());

                Cell precioCell = row.createCell(4);
                if (producto.getPrecio() != null) {
                    precioCell.setCellValue(producto.getPrecio());
                    precioCell.setCellStyle(currencyStyle);
                }

                row.createCell(5).setCellValue(producto.getStock() != null ? producto.getStock() : 0);
                row.createCell(6).setCellValue(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "");
                row.createCell(7).setCellValue(producto.getMarca());
                row.createCell(8).setCellValue(producto.getModelo());
                row.createCell(9).setCellValue(producto.getActivo() ? "Sí" : "No");

                if (producto.getFechaCreacion() != null) {
                    Cell dateCell = row.createCell(10);
                    dateCell.setCellValue(java.sql.Timestamp.valueOf(producto.getFechaCreacion()));
                    dateCell.setCellStyle(dateStyle);
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            logger.info("Archivo Excel de productos generado: {} registros", productos.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error generando Excel de productos: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo Excel", e);
        }
    }

    public byte[] generarProductosCSV(List<Producto> productos) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,Código,Nombre,Descripción,Precio,Stock,Categoría,Marca,Modelo,Activo,Fecha Creación\n");

        // Datos
        for (Producto producto : productos) {
            csv.append(producto.getId()).append(",");
            csv.append(escapeCSV(producto.getCodigo())).append(",");
            csv.append(escapeCSV(producto.getNombre())).append(",");
            csv.append(escapeCSV(producto.getDescripcion())).append(",");
            csv.append(producto.getPrecio() != null ? producto.getPrecio() : "").append(",");
            csv.append(producto.getStock() != null ? producto.getStock() : 0).append(",");
            csv.append(escapeCSV(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "")).append(",");
            csv.append(escapeCSV(producto.getMarca())).append(",");
            csv.append(escapeCSV(producto.getModelo())).append(",");
            csv.append(producto.getActivo() ? "Sí" : "No").append(",");
            csv.append(producto.getFechaCreacion() != null ?
                    producto.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
            csv.append("\n");
        }

        logger.info("Archivo CSV de productos generado: {} registros", productos.size());
        return csv.toString().getBytes();
    }

    public byte[] generarProductosJSON(List<Producto> productos) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            byte[] json = mapper.writeValueAsBytes(productos);
            logger.info("Archivo JSON de productos generado: {} registros", productos.size());
            return json;

        } catch (Exception e) {
            logger.error("Error generando JSON de productos: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo JSON", e);
        }
    }

    // ===============================
    // GENERADORES PARA VENTAS
    // ===============================

    public byte[] generarVentasExcel(List<Venta> ventas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateTimeStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal",
                    "Impuesto", "Total", "Método Pago", "Estado", "Observaciones"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(venta.getId());

                if (venta.getFecha() != null) {
                    Cell dateCell = row.createCell(1);
                    dateCell.setCellValue(java.sql.Timestamp.valueOf(venta.getFecha()));
                    dateCell.setCellStyle(dateStyle);
                }

                row.createCell(2).setCellValue(venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "");
                row.createCell(3).setCellValue(venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "");

                Cell subtotalCell = row.createCell(4);
                if (venta.getSubtotal() != null) {
                    subtotalCell.setCellValue(venta.getSubtotal());
                    subtotalCell.setCellStyle(currencyStyle);
                }

                Cell impuestoCell = row.createCell(5);
                if (venta.getImpuesto() != null) {
                    impuestoCell.setCellValue(venta.getImpuesto());
                    impuestoCell.setCellStyle(currencyStyle);
                }

                Cell totalCell = row.createCell(6);
                if (venta.getTotal() != null) {
                    totalCell.setCellValue(venta.getTotal());
                    totalCell.setCellStyle(currencyStyle);
                }

                row.createCell(7).setCellValue(venta.getMetodoPago());
                row.createCell(8).setCellValue(venta.getEstado());
                row.createCell(9).setCellValue(venta.getObservaciones());
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            logger.info("Archivo Excel de ventas generado: {} registros", ventas.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error generando Excel de ventas: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo Excel", e);
        }
    }

    public byte[] generarVentasCSV(List<Venta> ventas) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,Fecha,Cliente,Vendedor,Subtotal,Impuesto,Total,Método Pago,Estado,Observaciones\n");

        // Datos
        for (Venta venta : ventas) {
            csv.append(venta.getId()).append(",");
            csv.append(venta.getFecha() != null ?
                    venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "").append(",");
            csv.append(escapeCSV(venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "")).append(",");
            csv.append(escapeCSV(venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "")).append(",");
            csv.append(venta.getSubtotal() != null ? venta.getSubtotal() : "").append(",");
            csv.append(venta.getImpuesto() != null ? venta.getImpuesto() : "").append(",");
            csv.append(venta.getTotal() != null ? venta.getTotal() : "").append(",");
            csv.append(escapeCSV(venta.getMetodoPago())).append(",");
            csv.append(escapeCSV(venta.getEstado())).append(",");
            csv.append(escapeCSV(venta.getObservaciones()));
            csv.append("\n");
        }

        logger.info("Archivo CSV de ventas generado: {} registros", ventas.size());
        return csv.toString().getBytes();
    }

    public byte[] generarVentasJSON(List<Venta> ventas) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            byte[] json = mapper.writeValueAsBytes(ventas);
            logger.info("Archivo JSON de ventas generado: {} registros", ventas.size());
            return json;

        } catch (Exception e) {
            logger.error("Error generando JSON de ventas: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo JSON", e);
        }
    }

    // ===============================
    // GENERADORES PARA CLIENTES
    // ===============================

    public byte[] generarClientesJSON(List<ClienteReporteDTO> clientes) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            byte[] json = mapper.writeValueAsBytes(clientes);
            logger.info("Archivo JSON de clientes generado: {} registros", clientes.size());
            return json;

        } catch (Exception e) {
            logger.error("Error generando JSON de clientes: {}", e.getMessage());
            throw new RuntimeException("Error generando archivo JSON", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS
    // ===============================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createDateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";

        // Si contiene comas, comillas o saltos de línea, envolver en comillas
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Escapar comillas duplicándolas
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    // ===============================
    // CLASES DTO PARA EXPORTACIÓN
    // ===============================

    public static class UsuarioExportDTO {
        public Long id;
        public String username;
        public String nombre;
        public String apellido;
        public String email;
        public boolean activo;
        public LocalDate fechaCreacion;
        public LocalDate ultimoAcceso;
        public java.util.Set<String> roles;

        public UsuarioExportDTO(Long id, String username, String nombre, String apellido,
                                String email, boolean activo, LocalDate fechaCreacion,
                                LocalDate ultimoAcceso, java.util.Set<String> roles) {
            this.id = id;
            this.username = username;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.activo = activo;
            this.fechaCreacion = fechaCreacion;
            this.ultimoAcceso = ultimoAcceso;
            this.roles = roles;
        }
    }
}