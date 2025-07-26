package informviva.gest.service.impl;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ResultadoImportacionCliente;
import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementación del servicio para importación de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ClienteImportacionServicioImpl {

    private static final Logger logger = LoggerFactory.getLogger(ClienteImportacionServicioImpl.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final String[] ENCABEZADOS_ESPERADOS = {
            "rut", "nombre", "apellido", "email", "telefono", "direccion", "categoria"
    };

    @Autowired
    private ClienteServicio clienteServicio;

    /**
     * Importa clientes desde archivo Excel
     */
    public ResultadoImportacionCliente importarClientesDesdeExcel(MultipartFile archivo) {
        logger.info("Iniciando importación de clientes desde Excel: {}", archivo.getOriginalFilename());

        ResultadoImportacionCliente resultado = new ResultadoImportacionCliente();
        resultado.setTipoEntidad("Cliente");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            if (!esArchivoExcel(archivo.getOriginalFilename())) {
                resultado.agregarError("El archivo debe ser formato Excel (.xlsx)");
                return resultado;
            }

            List<Cliente> clientes = procesarArchivoExcel(archivo, resultado);

            // Los clientes exitosos ya se agregaron durante el procesamiento del Excel
            resultado.calcularTotales();

        } catch (Exception e) {
            logger.error("Error durante importación de clientes: {}", e.getMessage(), e);
            resultado.agregarError("Error general: " + e.getMessage());
        }

        logger.info("Importación completada. Procesados: {}, Exitosos: {}, Errores: {}",
                resultado.getRegistrosProcesados(), resultado.getRegistrosExitosos(), resultado.getRegistrosConError());
        return resultado;
    }

    /**
     * Importa clientes desde archivo CSV
     */
    public ResultadoImportacionCliente importarClientesDesdeCSV(MultipartFile archivo) {
        logger.info("Iniciando importación de clientes desde CSV: {}", archivo.getOriginalFilename());

        ResultadoImportacionCliente resultado = new ResultadoImportacionCliente();
        resultado.setTipoEntidad("Cliente");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            if (!esArchivoCSV(archivo.getOriginalFilename())) {
                resultado.agregarError("El archivo debe ser formato CSV (.csv)");
                return resultado;
            }

            List<Cliente> clientes = procesarArchivoCSV(archivo, resultado);

            // Los clientes exitosos ya se agregaron durante el procesamiento del CSV
            resultado.calcularTotales();

        } catch (Exception e) {
            logger.error("Error durante importación de clientes: {}", e.getMessage(), e);
            resultado.agregarError("Error general: " + e.getMessage());
        }

        logger.info("Importación completada. Procesados: {}, Exitosos: {}, Errores: {}",
                resultado.getRegistrosProcesados(), resultado.getRegistrosExitosos(), resultado.getRegistrosConError());
        return resultado;
    }

    /**
     * Guarda clientes importados en la base de datos
     */
    @Transactional
    public ResultadoImportacionCliente guardarClientesImportados(List<Cliente> clientes) {
        logger.info("Guardando {} clientes importados en base de datos", clientes.size());

        ResultadoImportacionCliente resultado = new ResultadoImportacionCliente();
        resultado.setTipoEntidad("Cliente");
        resultado.setFechaImportacion(LocalDateTime.now());

        int exitosos = 0;
        int errores = 0;

        for (Cliente cliente : clientes) {
            try {
                // Validar si el cliente ya existe por RUT
                if (clienteServicio.existeClienteConRut(cliente.getRut())) {
                    // Buscar cliente existente y actualizar
                    Cliente existente = buscarClientePorRut(cliente.getRut());
                    if (existente != null) {
                        actualizarClienteExistente(existente, cliente);
                        clienteServicio.guardar(existente);
                        logger.debug("Cliente actualizado: {}", cliente.getRut());
                    }
                } else {
                    // Crear nuevo cliente
                    cliente.setFechaRegistro(LocalDateTime.now());
                    clienteServicio.guardar(cliente);
                    logger.debug("Cliente creado: {}", cliente.getRut());
                }

                resultado.agregarClienteExitoso(cliente);
                exitosos++;

            } catch (Exception e) {
                logger.error("Error guardando cliente {}: {}", cliente.getRut(), e.getMessage());
                resultado.agregarError("Error guardando " + cliente.getRut() + ": " + e.getMessage());
                errores++;
            }
        }

        // Establecer contadores
        resultado.setRegistrosExitosos(exitosos);
        resultado.setRegistrosConError(errores);
        resultado.setRegistrosProcesados(exitosos + errores);
        resultado.calcularTotales();

        logger.info("Guardado completado. Exitosos: {}, Errores: {}", exitosos, errores);
        return resultado;
    }

    /**
     * Procesa archivo Excel y extrae clientes
     */
    private List<Cliente> procesarArchivoExcel(MultipartFile archivo, ResultadoImportacionCliente resultado)
            throws IOException {
        List<Cliente> clientes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                resultado.agregarError("El archivo está vacío");
                return clientes;
            }

            // Validar encabezados
            if (!validarEncabezados(sheet.getRow(0), resultado)) {
                return clientes;
            }

            // Procesar filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }

                try {
                    Cliente cliente = procesarFilaExcel(row, i + 1);
                    if (validarCliente(cliente, i + 1, resultado)) {
                        clientes.add(cliente);
                        resultado.agregarClienteExitoso(cliente);
                    } else {
                        resultado.incrementarErrores();
                    }
                } catch (Exception e) {
                    logger.warn("Error procesando fila {}: {}", i + 1, e.getMessage());
                    resultado.agregarError("Fila " + (i + 1) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }
        }

        return clientes;
    }

    /**
     * Procesa archivo CSV y extrae clientes
     */
    private List<Cliente> procesarArchivoCSV(MultipartFile archivo, ResultadoImportacionCliente resultado)
            throws IOException {
        List<Cliente> clientes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), "UTF-8"))) {
            String line;
            int numeroFila = 0;
            String[] encabezados = null;

            while ((line = reader.readLine()) != null) {
                numeroFila++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] valores = parsearLineaCSV(line);

                if (numeroFila == 1) {
                    // Validar encabezados
                    encabezados = valores;
                    if (!validarEncabezadosCSV(encabezados, resultado)) {
                        return clientes;
                    }
                } else {
                    try {
                        Cliente cliente = procesarFilaCSV(valores, numeroFila);
                        if (validarCliente(cliente, numeroFila, resultado)) {
                            clientes.add(cliente);
                            resultado.agregarClienteExitoso(cliente);
                        } else {
                            resultado.incrementarErrores();
                        }
                    } catch (Exception e) {
                        logger.warn("Error procesando fila {}: {}", numeroFila, e.getMessage());
                        resultado.agregarError("Fila " + numeroFila + ": " + e.getMessage());
                        resultado.incrementarErrores();
                    }
                }
            }
        }

        return clientes;
    }

    /**
     * Valida encabezados del archivo Excel
     */
    private boolean validarEncabezados(Row headerRow, ResultadoImportacionCliente resultado) {
        if (headerRow == null) {
            resultado.agregarError("No se encontraron encabezados en el archivo");
            return false;
        }

        String[] encabezados = new String[ENCABEZADOS_ESPERADOS.length];

        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                resultado.agregarError("Falta encabezado en columna " + (i + 1));
                return false;
            }
            encabezados[i] = obtenerValorCelda(cell).toLowerCase().trim();
        }

        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            if (!ENCABEZADOS_ESPERADOS[i].equals(encabezados[i])) {
                resultado.agregarError("Encabezado incorrecto en columna " + (i + 1) +
                        ". Se esperaba: " + ENCABEZADOS_ESPERADOS[i] + ", se encontró: " + encabezados[i]);
                return false;
            }
        }

        return true;
    }

    /**
     * Valida encabezados del archivo CSV
     */
    private boolean validarEncabezadosCSV(String[] encabezados, ResultadoImportacionCliente resultado) {
        if (encabezados.length < ENCABEZADOS_ESPERADOS.length) {
            resultado.agregarError("Faltan columnas en el archivo CSV. " +
                    "Se esperaban: " + String.join(", ", ENCABEZADOS_ESPERADOS));
            return false;
        }

        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            String valor = encabezados[i].toLowerCase().trim().replace("\"", "");
            if (!ENCABEZADOS_ESPERADOS[i].equals(valor)) {
                resultado.agregarError("Encabezado incorrecto en columna " + (i + 1) +
                        ". Se esperaba: " + ENCABEZADOS_ESPERADOS[i] + ", se encontró: " + valor);
                return false;
            }
        }

        return true;
    }

    /**
     * Procesa una fila de Excel y crea un cliente
     */
    private Cliente procesarFilaExcel(Row row, int numeroFila) {
        Cliente cliente = new Cliente();

        try {
            // RUT (requerido)
            String rut = limpiarTexto(obtenerValorCelda(row.getCell(0)));
            if (rut.isEmpty()) {
                throw new IllegalArgumentException("El RUT del cliente es obligatorio");
            }
            cliente.setRut(rut);

            // Nombre (requerido)
            String nombre = limpiarTexto(obtenerValorCelda(row.getCell(1)));
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre del cliente es obligatorio");
            }
            cliente.setNombre(nombre);

            // Apellido (requerido)
            String apellido = limpiarTexto(obtenerValorCelda(row.getCell(2)));
            if (apellido.isEmpty()) {
                throw new IllegalArgumentException("El apellido del cliente es obligatorio");
            }
            cliente.setApellido(apellido);

            // Email (opcional)
            String email = limpiarTexto(obtenerValorCelda(row.getCell(3)));
            if (!email.isEmpty()) {
                cliente.setEmail(email.toLowerCase());
            }

            // Teléfono (opcional)
            cliente.setTelefono(limpiarTexto(obtenerValorCelda(row.getCell(4))));

            // Dirección (opcional)
            cliente.setDireccion(limpiarTexto(obtenerValorCelda(row.getCell(5))));

            // Categoría (opcional)
            cliente.setCategoria(limpiarTexto(obtenerValorCelda(row.getCell(6))));

            // Establecer fecha de registro
            cliente.setFechaRegistro(LocalDateTime.now());

            return cliente;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    /**
     * Procesa una fila de CSV y crea un cliente
     */
    private Cliente procesarFilaCSV(String[] valores, int numeroFila) {
        if (valores.length < ENCABEZADOS_ESPERADOS.length) {
            throw new IllegalArgumentException("Faltan columnas en la fila");
        }

        Cliente cliente = new Cliente();

        try {
            // RUT (requerido)
            String rut = limpiarTexto(valores[0]);
            if (rut.isEmpty()) {
                throw new IllegalArgumentException("El RUT del cliente es obligatorio");
            }
            cliente.setRut(rut);

            // Nombre (requerido)
            String nombre = limpiarTexto(valores[1]);
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre del cliente es obligatorio");
            }
            cliente.setNombre(nombre);

            // Apellido (requerido)
            String apellido = limpiarTexto(valores[2]);
            if (apellido.isEmpty()) {
                throw new IllegalArgumentException("El apellido del cliente es obligatorio");
            }
            cliente.setApellido(apellido);

            // Email (opcional)
            String email = limpiarTexto(valores[3]);
            if (!email.isEmpty()) {
                cliente.setEmail(email.toLowerCase());
            }

            // Teléfono (opcional)
            cliente.setTelefono(limpiarTexto(valores[4]));

            // Dirección (opcional)
            cliente.setDireccion(limpiarTexto(valores[5]));

            // Categoría (opcional)
            cliente.setCategoria(limpiarTexto(valores[6]));

            // Establecer fecha de registro
            cliente.setFechaRegistro(LocalDateTime.now());

            return cliente;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    /**
     * Valida un cliente antes de agregarlo a la lista
     */
    private boolean validarCliente(Cliente cliente, int numeroFila, ResultadoImportacionCliente resultado) {
        List<String> errores = new ArrayList<>();

        // Validar RUT
        if (cliente.getRut() == null || cliente.getRut().trim().isEmpty()) {
            errores.add("RUT requerido");
        } else if (!ValidadorRutUtil.validar(cliente.getRut())) {
            errores.add("RUT inválido: " + cliente.getRut());
        }

        // Validar nombre
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            errores.add("Nombre requerido");
        }

        // Validar apellido
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            errores.add("Apellido requerido");
        }

        // Validar email si está presente
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(cliente.getEmail()).matches()) {
                errores.add("Email inválido: " + cliente.getEmail());
            }
        }

        if (!errores.isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": " + String.join(", ", errores));
            return false;
        }

        return true;
    }

    /**
     * Busca un cliente por RUT (método helper)
     */
    private Cliente buscarClientePorRut(String rut) {
        // Como ClienteServicio no tiene buscarPorRut, buscar en todos los clientes
        List<Cliente> todosLosClientes = clienteServicio.obtenerTodos();
        return todosLosClientes.stream()
                .filter(cliente -> rut.equals(cliente.getRut()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualiza un cliente existente con los datos importados
     */
    private void actualizarClienteExistente(Cliente existente, Cliente nuevo) {
        existente.setNombre(nuevo.getNombre());
        existente.setApellido(nuevo.getApellido());
        if (nuevo.getEmail() != null && !nuevo.getEmail().trim().isEmpty()) {
            existente.setEmail(nuevo.getEmail());
        }
        if (nuevo.getTelefono() != null && !nuevo.getTelefono().trim().isEmpty()) {
            existente.setTelefono(nuevo.getTelefono());
        }
        if (nuevo.getDireccion() != null && !nuevo.getDireccion().trim().isEmpty()) {
            existente.setDireccion(nuevo.getDireccion());
        }
        if (nuevo.getCategoria() != null && !nuevo.getCategoria().trim().isEmpty()) {
            existente.setCategoria(nuevo.getCategoria());
        }
    }

    /**
     * Verifica si es un archivo Excel
     */
    private boolean esArchivoExcel(String nombreArchivo) {
        if (nombreArchivo == null) return false;
        return nombreArchivo.toLowerCase().endsWith(".xlsx") ||
                nombreArchivo.toLowerCase().endsWith(".xls");
    }

    /**
     * Verifica si es un archivo CSV
     */
    private boolean esArchivoCSV(String nombreArchivo) {
        if (nombreArchivo == null) return false;
        return nombreArchivo.toLowerCase().endsWith(".csv");
    }

    /**
     * Verifica si una fila está vacía
     */
    private boolean esFilaVacia(Row row) {
        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !obtenerValorCelda(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parsea una línea CSV considerando comillas
     */
    private String[] parsearLineaCSV(String linea) {
        List<String> valores = new ArrayList<>();
        boolean dentroComillas = false;
        StringBuilder valor = new StringBuilder();

        for (char c : linea.toCharArray()) {
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                valores.add(valor.toString());
                valor = new StringBuilder();
            } else {
                valor.append(c);
            }
        }
        valores.add(valor.toString());

        return valores.toArray(new String[0]);
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private String obtenerValorCelda(Cell cell) {
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

    /**
     * Limpia y formatea texto
     */
    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ").replace("\"", "");
    }
}