package informviva.gest.service.impl;


import informviva.gest.dto.ImportacionResultadoDTO;
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
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de importaciones
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
                        if (clienteServicio.existeClienteConRut(cliente.getRut())) {
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
                        if (usuarioServicio.buscarPorUsername(usuario.getUsername()) != null) {
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
                errores.add("Formato de archivo no soportado. Use CSV o Excel (.xlsx, .xls)");
                resultado.put("valido", false);
                resultado.put("errores", errores);
                return resultado;
            }

            // Validar tamaño
            if (archivo.getSize() == 0) {
                errores.add("El archivo está vacío");
            } else if (archivo.getSize() > 10 * 1024 * 1024) { // 10MB
                advertencias.add("El archivo es muy grande (>10MB), el procesamiento puede ser lento");
            }

            // Procesar una muestra del archivo
            List<Map<String, Object>> muestra = obtenerVistaPreviaArchivo(archivo, 5);

            if (muestra.isEmpty()) {
                errores.add("No se pudieron leer datos del archivo");
            } else {
                // Validar columnas requeridas
                Set<String> columnasArchivo = muestra.get(0).keySet();
                List<String> columnasRequeridas = obtenerColumnasRequeridas(tipoEntidad);

                List<String> columnasFaltantes = columnasRequeridas.stream()
                        .filter(col -> !columnasArchivo.contains(col))
                        .collect(Collectors.toList());

                if (!columnasFaltantes.isEmpty()) {
                    errores.add("Faltan las siguientes columnas requeridas: " + String.join(", ", columnasFaltantes));
                }

                resultado.put("totalFilas", muestra.size());
                resultado.put("columnas", new ArrayList<>(columnasArchivo));
                resultado.put("muestra", muestra);
            }

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            errores.add("Error procesando el archivo: " + e.getMessage());
        }

        resultado.put("valido", errores.isEmpty());
        resultado.put("errores", errores);
        resultado.put("advertencias", advertencias);

        return resultado;
    }

    @Override
    public byte[] generarPlantillaImportacion(String tipoEntidad, String formato) {
        try {
            List<String> columnas = obtenerColumnasRequeridas(tipoEntidad);
            List<List<String>> datosEjemplo = obtenerDatosEjemplo(tipoEntidad);

            if ("CSV".equalsIgnoreCase(formato)) {
                return generarPlantillaCSV(columnas, datosEjemplo);
            } else {
                return generarPlantillaExcel(columnas, datosEjemplo, tipoEntidad);
            }
        } catch (Exception e) {
            logger.error("Error generando plantilla: {}", e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public List<Map<String, Object>> obtenerVistaPreviaArchivo(MultipartFile archivo, int limite) {
        try {
            return procesarArchivo(archivo).stream()
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo vista previa: {}", e.getMessage());
            return new ArrayList<>();
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
                headers[i] = headers[i].trim().replace("\"", "");
            }

            // Leer datos
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] valores = line.split(",");
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

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                return datos;
            }

            // Leer header
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();

            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell).trim());
            }

            // Leer datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> fila = new HashMap<>();

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String valor = cell != null ? getCellValueAsString(cell) : "";
                    fila.put(headers.get(j), valor);
                }

                datos.add(fila);
            }
        }

        return datos;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
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
            // 🔧 CORRECCIÓN: Cambiar LocalDateTime.now() por LocalDate.now()
            cliente.setFechaRegistro(LocalDate.now());

            return cliente;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando cliente: " + e.getMessage());
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
                Double precio = Double.parseDouble(precioStr);
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
            producto.setActivo(true);
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setFechaActualizacion(LocalDateTime.now());

            return producto;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando producto: " + e.getMessage());
        }
    }

    private Usuario mapearUsuario(Map<String, Object> fila, int numeroFila) {
        try {
            Usuario usuario = new Usuario();

            String username = obtenerValorString(fila, "username");
            String password = obtenerValorString(fila, "password");
            String nombre = obtenerValorString(fila, "nombre");
            String apellido = obtenerValorString(fila, "apellido");
            String email = obtenerValorString(fila, "email");
            String rolesStr = obtenerValorString(fila, "roles");
            String activoStr = obtenerValorString(fila, "activo");

            if (username.isEmpty() || password.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Campos obligatorios faltantes");
            }

            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);

            // Procesar roles
            Set<String> roles = new HashSet<>();
            if (!rolesStr.isEmpty()) {
                String[] rolesArray = rolesStr.split(";");
                for (String rol : rolesArray) {
                    roles.add(rol.trim().toUpperCase());
                }
            } else {
                roles.add("USER");
            }
            usuario.setRoles(roles);

            // Procesar estado activo
            boolean activo = activoStr.isEmpty() || "true".equalsIgnoreCase(activoStr) || "1".equals(activoStr) || "sí".equalsIgnoreCase(activoStr);
            usuario.setActivo(activo);

            // 🔧 CORRECCIÓN: Cambiar LocalDateTime.now() por LocalDate.now()
            usuario.setFechaCreacion(LocalDate.now());
            usuario.setUltimoAcceso(LocalDate.now());

            return usuario;
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando usuario: " + e.getMessage());
        }
    }

    private String obtenerValorString(Map<String, Object> fila, String columna) {
        Object valor = fila.get(columna);
        return valor != null ? valor.toString().trim() : "";
    }

    private boolean esFormatoValido(String nombreArchivo) {
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        return FORMATOS_SOPORTADOS.contains(extension);
    }

    private List<List<String>> obtenerDatosEjemplo(String tipoEntidad) {
        List<List<String>> ejemplos = new ArrayList<>();

        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                ejemplos.add(Arrays.asList("Juan", "Pérez", "juan.perez@email.com", "12345678-9", "987654321", "Av. Principal 123", "VIP"));
                ejemplos.add(Arrays.asList("María", "González", "maria.gonzalez@email.com", "98765432-1", "912345678", "Calle Secundaria 456", "Regular"));
                break;
            case "producto":
                ejemplos.add(Arrays.asList("PROD001", "Laptop Dell", "Laptop Dell Inspiron 15", "599990", "10", "Dell", "Inspiron 15"));
                ejemplos.add(Arrays.asList("PROD002", "Mouse Logitech", "Mouse óptico inalámbrico", "25990", "50", "Logitech", "M220"));
                break;
            case "usuario":
                ejemplos.add(Arrays.asList("jperez", "password123", "Juan", "Pérez", "juan.perez@empresa.com", "VENTAS", "true"));
                ejemplos.add(Arrays.asList("mgonzalez", "password456", "María", "González", "maria.gonzalez@empresa.com", "ADMIN;GERENTE", "true"));
                break;
        }

        return ejemplos;
    }

    private byte[] generarPlantillaCSV(List<String> columnas, List<List<String>> datosEjemplo) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append(String.join(",", columnas)).append("\n");

        // Datos de ejemplo
        for (List<String> fila : datosEjemplo) {
            csv.append(String.join(",", fila)).append("\n");
        }

        return csv.toString().getBytes();
    }

    private byte[] generarPlantillaExcel(List<String> columnas, List<List<String>> datosEjemplo, String tipoEntidad) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Plantilla " + tipoEntidad);

            // Crear header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas.get(i));
            }

            // Crear filas de ejemplo
            for (int i = 0; i < datosEjemplo.size(); i++) {
                Row row = sheet.createRow(i + 1);
                List<String> fila = datosEjemplo.get(i);
                for (int j = 0; j < fila.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(fila.get(j));
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}