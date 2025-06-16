package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.Producto;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio especializado para importación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ProductoImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoImportacionServicio.class);
    private static final String[] ENCABEZADOS_ESPERADOS = {
            "codigo", "nombre", "descripcion", "precio", "stock", "marca", "modelo"
    };

    private final ProductoServicio productoServicio;

    public ProductoImportacionServicio(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    /**
     * Importa productos desde archivo Excel o CSV
     */
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad("Producto");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            List<Producto> productos;

            if (esArchivoExcel(archivo.getOriginalFilename())) {
                productos = procesarArchivoExcel(archivo, resultado);
            } else if (esArchivoCSV(archivo.getOriginalFilename())) {
                productos = procesarArchivoCSV(archivo, resultado);
            } else {
                resultado.agregarError("Formato de archivo no soportado. Use Excel (.xlsx) o CSV.");
                return resultado;
            }

            // Guardar productos válidos
            guardarProductos(productos, resultado);

        } catch (Exception e) {
            logger.error("Error durante importación de productos: {}", e.getMessage());
            resultado.agregarError("Error general: " + e.getMessage());
        }

        resultado.calcularTotales();
        return resultado;
    }

    /**
     * Procesa archivo Excel
     */
    private List<Producto> procesarArchivoExcel(MultipartFile archivo, ImportacionResultadoDTO resultado)
            throws IOException {
        List<Producto> productos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                resultado.agregarError("El archivo está vacío");
                return productos;
            }

            // Validar encabezados
            if (!validarEncabezados(sheet.getRow(0), resultado)) {
                return productos;
            }

            // Procesar filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }

                try {
                    Producto producto = procesarFilaExcel(row, i + 1);
                    if (validarProducto(producto, i + 1, resultado)) {
                        productos.add(producto);
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + (i + 1) + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }
        }

        return productos;
    }

    /**
     * Procesa archivo CSV
     */
    private List<Producto> procesarArchivoCSV(MultipartFile archivo, ImportacionResultadoDTO resultado)
            throws IOException {
        List<Producto> productos = new ArrayList<>();

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
                        return productos;
                    }
                    continue;
                }

                try {
                    Producto producto = procesarFilaCSV(valores, numeroFila);
                    if (validarProducto(producto, numeroFila, resultado)) {
                        productos.add(producto);
                    }
                } catch (Exception e) {
                    resultado.agregarError("Fila " + numeroFila + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }
        }

        return productos;
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
    private Producto procesarFilaExcel(Row row, int numeroFila) {
        Producto producto = new Producto();

        producto.setCodigo(limpiarTexto(obtenerValorCelda(row.getCell(0))).toUpperCase());
        producto.setNombre(limpiarTexto(obtenerValorCelda(row.getCell(1))));
        producto.setDescripcion(limpiarTexto(obtenerValorCelda(row.getCell(2))));
        producto.setPrecio(obtenerValorNumerico(row.getCell(3), "precio"));
        producto.setStock((int) obtenerValorNumerico(row.getCell(4), "stock"));
        producto.setMarca(limpiarTexto(obtenerValorCelda(row.getCell(5))));
        producto.setModelo(limpiarTexto(obtenerValorCelda(row.getCell(6))));

        // Establecer valores por defecto
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());

        return producto;
    }

    /**
     * Procesa una fila de CSV
     */
    private Producto procesarFilaCSV(String[] valores, int numeroFila) {
        if (valores.length < ENCABEZADOS_ESPERADOS.length) {
            throw new IllegalArgumentException("Faltan columnas en la fila");
        }

        Producto producto = new Producto();

        producto.setCodigo(limpiarTexto(valores[0]).toUpperCase());
        producto.setNombre(limpiarTexto(valores[1]));
        producto.setDescripcion(limpiarTexto(valores[2]));
        producto.setPrecio(parseDouble(valores[3], "precio"));
        producto.setStock((int) parseDouble(valores[4], "stock"));
        producto.setMarca(limpiarTexto(valores[5]));
        producto.setModelo(limpiarTexto(valores[6]));

        // Establecer valores por defecto
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());

        return producto;
    }

    /**
     * Valida un producto antes de importar
     */
    private boolean validarProducto(Producto producto, int numeroFila, ImportacionResultadoDTO resultado) {
        boolean esValido = true;

        // Validaciones obligatorias
        if (producto.getCodigo().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": Código es obligatorio");
            esValido = false;
        } else if (productoServicio.existePorCodigo(producto.getCodigo())) {
            resultado.agregarAdvertencia("Fila " + numeroFila + ": Producto con código " + producto.getCodigo() + " ya existe, se omite");
            resultado.incrementarOmitidos();
            return false;
        }

        if (producto.getNombre().isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": Nombre es obligatorio");
            esValido = false;
        }

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            resultado.agregarError("Fila " + numeroFila + ": Precio debe ser mayor que cero");
            esValido = false;
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            resultado.agregarError("Fila " + numeroFila + ": Stock no puede ser negativo");
            esValido = false;
        }

        // Validaciones de longitud
        if (producto.getCodigo().length() > 50) {
            resultado.agregarError("Fila " + numeroFila + ": Código no puede superar 50 caracteres");
            esValido = false;
        }

        if (producto.getNombre().length() > 200) {
            resultado.agregarError("Fila " + numeroFila + ": Nombre no puede superar 200 caracteres");
            esValido = false;
        }

        if (producto.getDescripcion() != null && producto.getDescripcion().length() > 500) {
            resultado.agregarError("Fila " + numeroFila + ": Descripción no puede superar 500 caracteres");
            esValido = false;
        }

        if (producto.getMarca() != null && producto.getMarca().length() > 100) {
            resultado.agregarError("Fila " + numeroFila + ": Marca no puede superar 100 caracteres");
            esValido = false;
        }

        if (producto.getModelo() != null && producto.getModelo().length() > 100) {
            resultado.agregarError("Fila " + numeroFila + ": Modelo no puede superar 100 caracteres");
            esValido = false;
        }

        // Validaciones de rango
        if (producto.getPrecio() != null && producto.getPrecio() > 999999999.99) {
            resultado.agregarError("Fila " + numeroFila + ": Precio excede el límite máximo");
            esValido = false;
        }

        if (producto.getStock() != null && producto.getStock() > 999999) {
            resultado.agregarError("Fila " + numeroFila + ": Stock excede el límite máximo");
            esValido = false;
        }

        if (!esValido) {
            resultado.incrementarErrores();
        }

        return esValido;
    }

    /**
     * Guarda la lista de productos válidos
     */
    private void guardarProductos(List<Producto> productos, ImportacionResultadoDTO resultado) {
        for (Producto producto : productos) {
            try {
                productoServicio.guardar(producto);
                resultado.incrementarExitosos();
            } catch (Exception e) {
                resultado.agregarError("Error guardando producto " + producto.getCodigo() + ": " + e.getMessage());
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

    private double obtenerValorNumerico(Cell cell, String nombreCampo) {
        if (cell == null) return 0.0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                return parseDouble(cell.getStringCellValue(), nombreCampo);
            default:
                throw new IllegalArgumentException("Valor no numérico en campo: " + nombreCampo);
        }
    }

    private double parseDouble(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Limpiar el valor: quitar espacios, comas como separadores de miles, etc.
            String valorLimpio = valor.trim()
                    .replace(",", "")  // Quitar comas de separadores de miles
                    .replace("$", "")  // Quitar símbolo de moneda
                    .replace("\"", ""); // Quitar comillas

            return Double.parseDouble(valorLimpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor no numérico en campo " + nombreCampo + ": " + valor);
        }
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
                    // Para números enteros, no mostrar decimales
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
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
