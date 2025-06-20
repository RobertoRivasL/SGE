package informviva.gest.service.impl;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.exception.ImportacionException;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ImportacionServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Implementación del servicio de importación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
@Transactional
public class ImportacionServicioImpl implements ImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionServicioImpl.class);

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoImportacion("Clientes");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivoExcel(archivo);

            for (int i = 0; i < datos.size(); i++) {
                try {
                    Cliente cliente = mapearCliente(datos.get(i), i + 2); // +2 porque la fila 1 son headers
                    clienteServicio.guardar(cliente);
                    resultado.incrementarExitosos();

                } catch (Exception e) {
                    logger.warn("Error procesando cliente en fila {}: {}", i + 2, e.getMessage());
                    resultado.agregarError(i + 2, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error general en importación de clientes: {}", e.getMessage());
            resultado.setErrorGeneral(e.getMessage());
        }

        return resultado;
    }

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoImportacion("Productos");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivoExcel(archivo);

            for (int i = 0; i < datos.size(); i++) {
                try {
                    Producto producto = mapearProducto(datos.get(i), i + 2);
                    productoServicio.guardar(producto);
                    resultado.incrementarExitosos();

                } catch (Exception e) {
                    logger.warn("Error procesando producto en fila {}: {}", i + 2, e.getMessage());
                    resultado.agregarError(i + 2, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error general en importación de productos: {}", e.getMessage());
            resultado.setErrorGeneral(e.getMessage());
        }

        return resultado;
    }

    @Override
    public ImportacionResultadoDTO importarUsuarios(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoImportacion("Usuarios");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivoExcel(archivo);

            for (int i = 0; i < datos.size(); i++) {
                try {
                    Usuario usuario = mapearUsuario(datos.get(i), i + 2);
                    usuarioServicio.guardar(usuario);
                    resultado.incrementarExitosos();

                } catch (Exception e) {
                    logger.warn("Error procesando usuario en fila {}: {}", i + 2, e.getMessage());
                    resultado.agregarError(i + 2, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error general en importación de usuarios: {}", e.getMessage());
            resultado.setErrorGeneral(e.getMessage());
        }

        return resultado;
    }

    @Override
    public byte[] generarPlantillaClientes() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"nombre", "apellido", "email", "rut", "telefono", "direccion", "categoria"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // Estilo para encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Ejemplo de datos
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Juan");
            exampleRow.createCell(1).setCellValue("Pérez");
            exampleRow.createCell(2).setCellValue("juan.perez@email.com");
            exampleRow.createCell(3).setCellValue("12.345.678-9");
            exampleRow.createCell(4).setCellValue("+56912345678");
            exampleRow.createCell(5).setCellValue("Av. Principal 123");
            exampleRow.createCell(6).setCellValue("VIP");

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] generarPlantillaProductos() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"codigo", "nombre", "descripcion", "precio", "stock", "categoria", "proveedor"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // Estilo para encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Ejemplo de datos
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("PROD001");
            exampleRow.createCell(1).setCellValue("Producto Ejemplo");
            exampleRow.createCell(2).setCellValue("Descripción del producto");
            exampleRow.createCell(3).setCellValue(19990);
            exampleRow.createCell(4).setCellValue(50);
            exampleRow.createCell(5).setCellValue("Electrónicos");
            exampleRow.createCell(6).setCellValue("Proveedor ABC");

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Métodos privados de utilidad

    private List<Map<String, Object>> procesarArchivoExcel(MultipartFile archivo) throws IOException {
        List<Map<String, Object>> datos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Obtener encabezados de la primera fila
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ImportacionException("El archivo no contiene encabezados");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().toLowerCase().trim());
            }

            // Procesar filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> fila = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object valor = obtenerValorCelda(cell);
                    fila.put(headers.get(j), valor);
                }
                datos.add(fila);
            }
        }

        return datos;
    }

    private Object obtenerValorCelda(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }

    private Cliente mapearCliente(Map<String, Object> fila, int numeroFila) {
        try {
            Cliente cliente = new Cliente();

            String nombre = obtenerValorString(fila, "nombre");
            String apellido = obtenerValorString(fila, "apellido");
            String email = obtenerValorString(fila, "email");
            String rut = obtenerValorString(fila, "rut");

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || rut.isEmpty()) {
                throw new IllegalArgumentException("Campos obligatorios faltantes (nombre, apellido, email, rut)");
            }

            if (!ValidadorRutUtil.validar(rut)) {
                throw new IllegalArgumentException("RUT inválido: " + rut);
            }

            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setEmail(email);
            cliente.setRut(rut);
            cliente.setTelefono(obtenerValorString(fila, "telefono"));
            cliente.setDireccion(obtenerValorString(fila, "direccion"));
            cliente.setCategoria(obtenerValorString(fila, "categoria"));
            cliente.setFechaRegistro(LocalDate.now());

            return cliente;
        } catch (Exception e) {
            throw new ImportacionException("Error mapeando cliente en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    private Producto mapearProducto(Map<String, Object> fila, int numeroFila) {
        try {
            Producto producto = new Producto();

            String codigo = obtenerValorString(fila, "codigo");
            String nombre = obtenerValorString(fila, "nombre");
            String precioStr = obtenerValorString(fila, "precio");
            String stockStr = obtenerValorString(fila, "stock");

            if (codigo.isEmpty() || nombre.isEmpty() || precioStr.isEmpty()) {
                throw new IllegalArgumentException("Campos obligatorios faltantes (codigo, nombre, precio)");
            }

            try {
                Double precio = Double.parseDouble(precioStr.replace(",", "."));
                if (precio <= 0) {
                    throw new IllegalArgumentException("El precio debe ser mayor que cero");
                }
                producto.setPrecio(precio);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Precio inválido: " + precioStr);
            }

            try {
                Integer stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
                if (stock < 0) {
                    throw new IllegalArgumentException("El stock no puede ser negativo");
                }
                producto.setStock(stock);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stock inválido: " + stockStr);
            }

            producto.setCodigo(codigo.toUpperCase());
            producto.setNombre(nombre);
            producto.setDescripcion(obtenerValorString(fila, "descripcion"));
            producto.setCategoria(obtenerValorString(fila, "categoria"));
            producto.setProveedor(obtenerValorString(fila, "proveedor"));
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setActivo(true);

            return producto;
        } catch (Exception e) {
            throw new ImportacionException("Error mapeando producto en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    private Usuario mapearUsuario(Map<String, Object> fila, int numeroFila) {
        try {
            Usuario usuario = new Usuario();

            String username = obtenerValorString(fila, "username");
            String nombre = obtenerValorString(fila, "nombre");
            String email = obtenerValorString(fila, "email");
            String password = obtenerValorString(fila, "password");

            if (username.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Campos obligatorios faltantes (username, nombre, email, password)");
            }

            // Validar que el username no exista
            if (usuarioServicio.existePorUsername(username)) {
                throw new IllegalArgumentException("El username ya existe: " + username);
            }

            // Validar que el email no exista
            if (usuarioServicio.existePorEmail(email)) {
                throw new IllegalArgumentException("El email ya existe: " + email);
            }

            usuario.setUsername(username);
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setActivo(true);
            usuario.setFechaCreacion(LocalDateTime.now());

            // Rol por defecto
            String rol = obtenerValorString(fila, "rol");
            if (rol.isEmpty()) {
                rol = "USER";
            }
            usuario.setRol(rol.toUpperCase());

            return usuario;
        } catch (Exception e) {
            throw new ImportacionException("Error mapeando usuario en fila " + numeroFila + ": " + e.getMessage());
        }
    }
}