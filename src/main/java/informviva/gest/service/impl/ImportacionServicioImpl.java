package informviva.gest.service.impl;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ImportacionServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.validador.ValidadorRutClase;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final List<String> FORMATOS_SOPORTADOS = Arrays.asList("csv", "xlsx", "xls");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER_SIMPLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        logger.info("Iniciando importación de clientes desde archivo: {}", archivo.getOriginalFilename());

        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad("Cliente");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivo(archivo);

            if (datos.isEmpty()) {
                resultado.agregarError("El archivo está vacío o no contiene datos válidos");
                return resultado;
            }

            // Validar columnas requeridas
            Set<String> columnasArchivo = datos.get(0).keySet();
            List<String> columnasRequeridas = obtenerColumnasRequeridas("cliente");

            for (String columnaRequerida : columnasRequeridas) {
                if (!columnasArchivo.contains(columnaRequerida)) {
                    resultado.agregarError("Falta la columna requerida: " + columnaRequerida);
                }
            }

            if (resultado.tieneErrores()) {
                return resultado;
            }

            // Procesar cada fila
            for (int i = 0; i < datos.size(); i++) {
                try {
                    Map<String, Object> fila = datos.get(i);
                    Cliente cliente = mapearCliente(fila, i + 2); // +2 porque empezamos en fila 2 (después del header)

                    if (cliente != null) {
                        // Verificar si ya existe
                        if (clienteServicio.existePorRut(cliente.getRut())) {
                            resultado.agregarAdvertencia("Fila " + (i + 2) + ": Cliente con RUT " + cliente.getRut() + " ya existe, se omite");
                            resultado.incrementarOmitidos();
                        } else {
                            clienteServicio.guardar(cliente);
                            resultado.incrementarExitosos();
                        }
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + (i + 2) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }

        } catch (Exception e) {
            logger.error("Error durante la importación de clientes: {}", e.getMessage());
            resultado.agregarError("Error general: " + e.getMessage());
        }

        resultado.calcularTotales();
        logger.info("Importación de clientes completada: {} exitosos, {} errores, {} omitidos",
                resultado.getRegistrosExitosos(), resultado.getRegistrosConError(), resultado.getRegistrosOmitidos());

        return resultado;
    }

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        logger.info("Iniciando importación de productos desde archivo: {}", archivo.getOriginalFilename());

        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad("Producto");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivo(archivo);

            if (datos.isEmpty()) {
                resultado.agregarError("El archivo está vacío o no contiene datos válidos");
                return resultado;
            }

            // Validar columnas requeridas
            Set<String> columnasArchivo = datos.get(0).keySet();
            List<String> columnasRequeridas = obtenerColumnasRequeridas("producto");

            for (String columnaRequerida : columnasRequeridas) {
                if (!columnasArchivo.contains(columnaRequerida)) {
                    resultado.agregarError("Falta la columna requerida: " + columnaRequerida);
                }
            }

            if (resultado.tieneErrores()) {
                return resultado;
            }

            // Procesar cada fila
            for (int i = 0; i < datos.size(); i++) {
                try {
                    Map<String, Object> fila = datos.get(i);
                    Producto producto = mapearProducto(fila, i + 2);

                    if (producto != null) {
                        // Verificar si ya existe
                        if (productoServicio.existePorCodigo(producto.getCodigo())) {
                            resultado.agregarAdvertencia("Fila " + (i + 2) + ": Producto con código " + producto.getCodigo() + " ya existe, se omite");
                            resultado.incrementarOmitidos();
                        } else {
                            productoServicio.guardar(producto);
                            resultado.incrementarExitosos();
                        }
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + (i + 2) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }

        } catch (Exception e) {
            logger.error("Error durante la importación de productos: {}", e.getMessage());
            resultado.agregarError("Error general: " + e.getMessage());
        }

        resultado.calcularTotales();
        logger.info("Importación de productos completada: {} exitosos, {} errores, {} omitidos",
                resultado.getRegistrosExitosos(), resultado.getRegistrosConError(), resultado.getRegistrosOmitidos());

        return resultado;
    }

    @Override
    public ImportacionResultadoDTO importarUsuarios(MultipartFile archivo) {
        logger.info("Iniciando importación de usuarios desde archivo: {}", archivo.getOriginalFilename());

        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad("Usuario");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Map<String, Object>> datos = procesarArchivo(archivo);

            if (datos.isEmpty()) {
                resultado.agregarError("El archivo está vacío o no contiene datos válidos");
                return resultado;
            }

            // Validar columnas requeridas
            Set<String> columnasArchivo = datos.get(0).keySet();
            List<String> columnasRequeridas = obtenerColumnasRequeridas("usuario");

            for (String columnaRequerida : columnasRequeridas) {
                if (!columnasArchivo.contains(columnaRequerida)) {
                    resultado.agregarError("Falta la columna requerida: " + columnaRequerida);
                }
            }

            if (resultado.tieneErrores()) {
                return resultado;
            }

            // Procesar cada fila
            for (int i = 0; i < datos.size(); i++) {
                try {
                    Map<String, Object> fila = datos.get(i);
                    Usuario usuario = mapearUsuario(fila, i + 2);

                    if (usuario != null) {
                        // Verificar si ya existe
                        if (usuarioServicio.existePorUsername(usuario.getUsername())) {
                            resultado.agregarAdvertencia("Fila " + (i + 2) + ": Usuario " + usuario.getUsername() + " ya existe, se omite");
                            resultado.incrementarOmitidos();
                        } else {
                            usuarioServicio.guardar(usuario);
                            resultado.incrementarExitosos();
                        }
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + (i + 2) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }

        } catch (Exception e) {
            logger.error("Error durante la importación de usuarios: {}", e.getMessage());
            resultado.agregarError("Error general: " + e.getMessage());
        }

        resultado.calcularTotales();
        logger.info("Importación de usuarios completada: {} exitosos, {} errores, {} omitidos",
                resultado.getRegistrosExitosos(), resultado.getRegistrosConError(), resultado.getRegistrosOmitidos());

        return resultado;
    }

    @Override
    public Map<String, Object> validarArchivoImportacion(MultipartFile archivo, String tipoEntidad) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        try {
            // Validar formato de archivo
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null || !esFormatoValido(nombreArchivo)) {
                errores.add("Formato de archivo no soportado. Use: " + String.join(", ", FORMATOS_SOPORTADOS));
                resultado.put("valido", false);
                resultado.put("errores", errores);
                return resultado;
            }

            // Validar tamaño del archivo (máximo 10MB)
            if (archivo.getSize() > 10 * 1024 * 1024) {
                errores.add("El archivo es demasiado grande. Tamaño máximo: 10MB");
            }

            // Validar contenido del archivo
            List<Map<String, Object>> datos = procesarArchivo(archivo);

            if (datos.isEmpty()) {
                errores.add("El archivo está vacío o no contiene datos válidos");
            } else {
                // Validar columnas requeridas
                Set<String> columnasArchivo = datos.get(0).keySet();
                List<String> columnasRequeridas = obtenerColumnasRequeridas(tipoEntidad);

                List<String> columnasFaltantes = new ArrayList<>();
                for (String columnaRequerida : columnasRequeridas) {
                    if (!columnasArchivo.contains(columnaRequerida)) {
                        columnasFaltantes.add(columnaRequerida);
                    }
                }

                if (!columnasFaltantes.isEmpty()) {
                    errores.add("Faltan columnas requeridas: " + String.join(", ", columnasFaltantes));
                }

                // Información adicional
                resultado.put("totalFilas", datos.size());
                resultado.put("columnasEncontradas", new ArrayList<>(columnasArchivo));
                resultado.put("columnasRequeridas", columnasRequeridas);
            }

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            errores.add("Error procesando archivo: " + e.getMessage());
        }

        resultado.put("valido", errores.isEmpty());
        resultado.put("errores", errores);
        resultado.put("advertencias", advertencias);
        resultado.put("nombreArchivo", archivo.getOriginalFilename());
        resultado.put("tamahoArchivo", archivo.getSize());

        return resultado;
    }

    @Override
    public byte[] generarPlantillaImportacion(String tipoEntidad, String formato) {
        try {
            if ("CSV".equalsIgnoreCase(formato)) {
                return generarPlantillaCSV(tipoEntidad);
            } else if ("EXCEL".equalsIgnoreCase(formato)) {
                return generarPlantillaExcel(tipoEntidad);
            } else {
                throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error generando plantilla {}: {}", tipoEntidad, e.getMessage());
            throw new RuntimeException("Error generando plantilla: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> obtenerVistaPreviaArchivo(MultipartFile archivo, int limite) {
        try {
            List<Map<String, Object>> datos = procesarArchivo(archivo);

            if (datos.size() <= limite) {
                return datos;
            } else {
                return datos.subList(0, limite);
            }
        } catch (Exception e) {
            logger.error("Error obteniendo vista previa: {}", e.getMessage());
            throw new RuntimeException("Error procesando archivo: " + e.getMessage());
        }
    }

    @Override
    public List<String> obtenerFormatosSoportados() {
        return new ArrayList<>(FORMATOS_SOPORTADOS);
    }

    @Override
    public List<String> obtenerColumnasRequeridas(String tipoEntidad) {
        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                return Arrays.asList("nombre", "apellido", "email", "rut", "telefono", "direccion", "categoria");
            case "producto":
                return Arrays.asList("codigo", "nombre", "descripcion", "precio", "stock", "marca", "modelo");
            case "usuario":
                return Arrays.asList("username", "password", "nombre", "apellido", "email", "roles", "activo");
            default:
                return new ArrayList<>();
        }
    }

    // Métodos auxiliares privados

    private List<Map<String, Object>> procesarArchivo(MultipartFile archivo) throws IOException {
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }

        if (nombreArchivo.toLowerCase().endsWith(".csv")) {
            return procesarCSV(archivo);
        } else if (nombreArchivo.toLowerCase().endsWith(".xlsx") || nombreArchivo.toLowerCase().endsWith(".xls")) {
            return procesarExcel(archivo);
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado");
        }
    }

    private List<Map<String, Object>> procesarCSV(MultipartFile archivo) throws IOException {
        List<Map<String, Object>> datos = new ArrayList<>();

        try (Scanner scanner = new Scanner(new InputStreamReader(archivo.getInputStream(), "UTF-8"))) {
            if (!scanner.hasNextLine()) {
                return datos;
            }

            // Leer header
            String headerLine = scanner.nextLine();
            String[] headers = headerLine.split(",");

            // Limpiar headers
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim().toLowerCase().replace("\"", "");
            }

            // Leer datos
            while (scanner.hasNextLine()) {
                String dataLine = scanner.nextLine();
                if (dataLine.trim().isEmpty()) continue;

                String[] valores = dataLine.split(",");
                Map<String, Object> fila = new HashMap<>();

                for (int i = 0; i < headers.length && i < valores.length; i++) {
                    String valor = valores[i].trim().replace("\"", "");
                    fila.put(headers[i], valor);
                }
                datos.add(fila);
            }
        }

        return datos;
    }

    private List<Map<String, Object>> procesarExcel(MultipartFile archivo) throws IOException {
        List<Map<String, Object>> datos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Obtener encabezados de la primera fila
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("El archivo no contiene encabezados");
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

            if (!ValidadorRutClase.validar(rut)) {
                throw new IllegalArgumentException("RUT inválido: " + rut);
            }

            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setEmail(email);
            cliente.setRut(rut);
            cliente.setTelefono(obtenerValorString(fila, "telefono"));
            cliente.setDireccion(obtenerValorString(fila, "direccion"));
            cliente.setCategoria(obtenerValorString(fila, "categoria"));
            cliente.setFechaRegistro(LocalDateTime.now());

            return cliente;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando cliente en fila " + numeroFila + ": " + e.getMessage());
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
            producto.setMarca(obtenerValorString(fila, "marca"));
            producto.setModelo(obtenerValorString(fila, "modelo"));
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setActivo(true);

            return producto;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando producto en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    private Usuario mapearUsuario(Map<String, Object> fila, int numeroFila) {
        try {
            Usuario usuario = new Usuario();

            String username = obtenerValorString(fila, "username");
            String nombre = obtenerValorString(fila, "nombre");
            String apellido = obtenerValorString(fila, "apellido");
            String email = obtenerValorString(fila, "email");
            String password = obtenerValorString(fila, "password");

            if (username.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Campos obligatorios faltantes (username, nombre, email, password)");
            }

            usuario.setUsername(username);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setActivo(true);
            usuario.setFechaCreacion(LocalDateTime.now());

            // Rol por defecto
            String roles = obtenerValorString(fila, "roles");
            if (roles.isEmpty()) {
                roles = "USER";
            }
            usuario.setRoles(Collections.singleton(roles.toUpperCase()));

            return usuario;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando usuario en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    private boolean esFormatoValido(String nombreArchivo) {
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        return FORMATOS_SOPORTADOS.contains(extension);
    }

    private byte[] generarPlantillaCSV(String tipoEntidad) throws IOException {
        List<String> columnas = obtenerColumnasRequeridas(tipoEntidad);
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append(String.join(",", columnas)).append("\n");

        // Ejemplo de fila
        for (int i = 0; i < columnas.size(); i++) {
            if (i > 0) csv.append(",");
            csv.append("ejemplo_").append(columnas.get(i));
        }

        return csv.toString().getBytes("UTF-8");
    }

    private byte[] generarPlantillaExcel(String tipoEntidad) throws IOException {
        List<String> columnas = obtenerColumnasRequeridas(tipoEntidad);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(tipoEntidad.substring(0, 1).toUpperCase() + tipoEntidad.substring(1));

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas.get(i));

                // Estilo para encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Fila de ejemplo
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < columnas.size(); i++) {
                exampleRow.createCell(i).setCellValue("ejemplo_" + columnas.get(i));
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}