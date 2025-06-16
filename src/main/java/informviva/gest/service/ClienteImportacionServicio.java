package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.Cliente;
import informviva.gest.validador.ValidadorRutUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio especializado para importación de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ClienteImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteImportacionServicio.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final String[] ENCABEZADOS_ESPERADOS = {
            "rut", "nombre", "apellido", "email", "telefono", "direccion", "categoria"
    };

    private final ClienteServicio clienteServicio;

    public ClienteImportacionServicio(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    /**
     * Importa clientes desde archivo Excel o CSV
     */
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad("Cliente");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Cliente> clientes;

            if (esArchivoExcel(archivo.getOriginalFilename())) {
                clientes = procesarArchivoExcel(archivo, resultado);
            } else if (esArchivoCSV(archivo.getOriginalFilename())) {
                clientes = procesarArchivoCSV(archivo, resultado);
            } else {
                resultado.agregarError("Formato de archivo no soportado. Use Excel (.xlsx) o CSV.");
                return resultado;
            }

            // Guardar clientes válidos
            guardarClientes(clientes, resultado);

        } catch (Exception e) {
            logger.error("Error durante importación de clientes: {}", e.getMessage());
            resultado.agregarError("Error general: " + e.getMessage());
        }

        resultado.calcularTotales();
        return resultado;
    }

    /**
     * Procesa archivo Excel
     */
    private List<Cliente> procesarArchivoExcel(MultipartFile archivo, ImportacionResultadoDTO resultado)
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
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + (i + 1) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }
        }

        return clientes;
    }

    /**
     * Procesa archivo CSV
     */
    private List<Cliente> procesarArchivoCSV(MultipartFile archivo, ImportacionResultadoDTO resultado)
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

                String[] valores = line.split(",");

                if (numeroFila == 1) {
                    // Validar encabezados
                    encabezados = valores;
                    if (!validarEncabezadosCSV(encabezados, resultado)) {
                        return clientes;
                    }
                    continue;
                }

                try {
                    Cliente cliente = procesarFilaCSV(valores, numeroFila);
                    if (validarCliente(cliente, numeroFila, resultado)) {
                        clientes.add(cliente);
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + numeroFila + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }
        }

        return clientes;
    }

    /**
     * Valida encabezados de Excel
     */
    private boolean validarEncabezados(Row headerRow, ImportacionResultadoDTO resultado) {
        if (headerRow == null) {
            resultado.agregarError("El archivo no tiene encabezados válidos");
            return false;
        }

        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String valor = obtenerValorCelda(cell).toLowerCase().trim();

            if (!ENCABEZADOS_ESPERADOS[i].equals(valor)) {
                resultado.agregarError("Encabezado incorrecto en columna " + (i + 1) +
                        ". Se esperaba: " + ENCABEZADOS_ESPERADOS[i] + ", se encontró: " + valor);
                return false;
            }
        }

        return true;
    }

    /**
     * Valida encabezados de CSV
     */
    private boolean validarEncabezadosCSV(String[] encabezados, ImportacionResultadoDTO resultado) {
        if (encabezados.length < ENCABEZADOS_ESPERADOS.length) {
            resultado.agregarError("Faltan columnas. Se esperaban: " + String.join(", ", ENCABEZADOS_ESPERADOS));
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
     * Procesa una fila de Excel
     */
    private Cliente procesarFilaExcel(Row row, int numeroFila) {
        Cliente cliente = new Cliente();

        cliente.setRut(limpiarTexto(obtenerValorCelda(row.getCell(0))));
        cliente.setNombre(limpiarTexto(obtenerValorCelda(row.getCell(1))));
        cliente.setApellido(limpiarTexto(obtenerValorCelda(row.getCell(2))));
        cliente.setEmail(limpiarTexto(obtenerValorCelda(row.getCell(3))).toLowerCase());
        cliente.setTelefono(limpiarTexto(obtenerValorCelda(row.getCell(4))));
        cliente.setDireccion(limpiarTexto(obtenerValorCelda(row.getCell(5))));
        cliente.setCategoria(limpiarTexto(obtenerValorCelda(row.getCell(6))));
        cliente.setFechaRegistro(LocalDate.now());

        return cliente;
    }

    /**
     * Procesa una fila de CSV
     */
    private Cliente procesarFilaCSV(String[] valores, int numeroFila) {
        if (valores.length < ENCABEZADOS_ESPERADOS.length) {
            throw new IllegalArgumentException("Faltan columnas en la fila");
        }

        Cliente cliente = new Cliente();

        cliente.setRut(limpiarTexto(valores[0]));
        cliente.setNombre(limpiarTexto(valores[1]));
        cliente.setApellido(limpiarTexto(valores[2]));
        cliente.setEmail(limpiarTexto(valores[3]).toLowerCase());
        cliente.setTelefono(limpiarTexto(valores[4]));
        cliente.setDireccion(limpiarTexto(valores[5]));
        cliente.setCategoria(limpiarTexto(valores[6]));
        cliente.setFechaRegistro(LocalDate.now());

        return cliente;
    }

    /**
     * Valida un cliente antes de importar
     */
    private boolean validarCliente(Cliente cliente, int numeroFila, ImportacionResultadoDTO resultado) {
        boolean esValido = true;

        // Validaciones obligatorias
        if (cliente.getRut().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": RUT es obligatorio");
            esValido = false;
        } else if (!ValidadorRutUtil.validar(cliente.getRut())) {
            resultado.agregarError("Fila " + numeroFila + ": RUT no válido: " + cliente.getRut());
            esValido = false;
        } else if (clienteServicio.existeClienteConRut(cliente.getRut())) {
            resultado.agregarAdvertencia("Fila " + numeroFila + ": Cliente con RUT " + cliente.getRut() + " ya existe, se omite");
            resultado.incrementarOmitidos();
            return false;
        }

        if (cliente.getNombre().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": Nombre es obligatorio");
            esValido = false;
        }

        if (cliente.getApellido().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": Apellido es obligatorio");
            esValido = false;
        }

        if (cliente.getEmail().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": Email es obligatorio");
            esValido = false;
        } else if (!EMAIL_PATTERN.matcher(cliente.getEmail()).matches()) {
            resultado.agregarError("Fila " + numeroFila + ": Formato de email no válido: " + cliente.getEmail());
            esValido = false;
        } else if (clienteServicio.existeClienteConEmail(cliente.getEmail())) {
            resultado.agregarAdvertencia("Fila " + numeroFila + ": Cliente con email " + cliente.getEmail() + " ya existe, se omite");
            resultado.incrementarOmitidos();
            return false;
        }

        // Validaciones de longitud
        if (cliente.getNombre().length() > 100) {
            resultado.agregarError("Fila " + numeroFila + ": Nombre no puede superar 100 caracteres");
            esValido = false;
        }

        if (cliente.getApellido().length() > 100) {
            resultado.agregarError("Fila " + numeroFila + ": Apellido no puede superar 100 caracteres");
            esValido = false;
        }

        if (cliente.getEmail().length() > 150) {
            resultado.agregarError("Fila " + numeroFila + ": Email no puede superar 150 caracteres");
            esValido = false;
        }

        if (cliente.getTelefono() != null && cliente.getTelefono().length() > 20) {
            resultado.agregarError("Fila " + numeroFila + ": Teléfono no puede superar 20 caracteres");
            esValido = false;
        }

        if (!esValido) {
            resultado.incrementarErrores();
        }

        return esValido;
    }

    /**
     * Guarda la lista de clientes válidos
     */
    private void guardarClientes(List<Cliente> clientes, ImportacionResultadoDTO resultado) {
        for (Cliente cliente : clientes) {
            try {
                clienteServicio.guardar(cliente);
                resultado.incrementarExitosos();
            } catch (Exception e) {
                resultado.agregarError("Error guardando cliente " + cliente.getRut() + ": " + e.getMessage());
                resultado.incrementarErrores();
            }
        }
    }

    // Métodos auxiliares
    private boolean esArchivoExcel(String nombreArchivo) {
        return nombreArchivo != null &&
                (nombreArchivo.toLowerCase().endsWith(".xlsx") || nombreArchivo.toLowerCase().endsWith(".xls"));
    }

    private boolean esArchivoCSV(String nombreArchivo) {
        return nombreArchivo != null && nombreArchivo.toLowerCase().endsWith(".csv");
    }

    private boolean esFilaVacia(Row row) {
        for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !obtenerValorCelda(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String limpiarTexto(String texto) {
        return texto != null ? texto.trim().replace("\"", "") : "";
    }

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
}
