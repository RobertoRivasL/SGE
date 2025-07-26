package informviva.gest.service.impl;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ProductoImportacionResultadoDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Categoria;
import informviva.gest.service.ProductoImportacionServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.CategoriaServicio;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio para importación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ProductoImportacionServicioImpl implements ProductoImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoImportacionServicioImpl.class);
    private static final String[] ENCABEZADOS_ESPERADOS = {
            "codigo", "nombre", "descripcion", "precio", "stock", "marca", "modelo"
    };

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private CategoriaServicio categoriaServicio;

    /**
     * Importa productos desde archivo Excel (incluye guardar en BD)
     */
    public ProductoImportacionResultadoDTO importarProductosDesdeExcel(MultipartFile archivo) {
        logger.info("Iniciando importación de productos desde Excel: {}", archivo.getOriginalFilename());

        ProductoImportacionResultadoDTO resultado = new ProductoImportacionResultadoDTO();
        resultado.setTipoEntidad("Producto");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            if (!esArchivoExcel(archivo.getOriginalFilename())) {
                resultado.agregarError("El archivo debe ser formato Excel (.xlsx)");
                return resultado;
            }

            List<Producto> productos = procesarArchivoExcel(archivo, resultado);

            // Los productos exitosos ya se agregaron durante el procesamiento del Excel
            // NO guardamos en BD aquí - eso se hace en guardarProductosImportados()

            resultado.calcularTotales();

        } catch (Exception e) {
            logger.error("Error durante importación de productos: {}", e.getMessage(), e);
            resultado.agregarError("Error general: " + e.getMessage());
        }

        logger.info("Importación completada. Procesados: {}, Exitosos: {}, Errores: {}",
                resultado.getRegistrosProcesados(), resultado.getRegistrosExitosos(), resultado.getRegistrosConError());
        return resultado;
    }

    /**
     * Método mantenido para compatibilidad - guarda productos en BD
     */
    @Transactional
    public ProductoImportacionResultadoDTO guardarProductosImportados(List<Producto> productos) {
        logger.info("Guardando {} productos importados en base de datos", productos.size());

        ProductoImportacionResultadoDTO resultado = new ProductoImportacionResultadoDTO();
        resultado.setTipoEntidad("Producto");
        resultado.setFechaImportacion(LocalDateTime.now());

        int exitosos = 0;
        int errores = 0;

        for (Producto producto : productos) {
            try {
                // Validar si el producto ya existe por código
                Producto existente = productoServicio.buscarPorCodigo(producto.getCodigo());

                if (existente != null) {
                    // Actualizar producto existente
                    actualizarProductoExistente(existente, producto);
                    productoServicio.actualizar(existente.getId(), existente);
                    logger.debug("Producto actualizado: {}", producto.getCodigo());
                } else {
                    // Crear nuevo producto
                    producto.setFechaCreacion(LocalDateTime.now());
                    producto.setFechaActualizacion(LocalDateTime.now());
                    producto.setActivo(true);
                    productoServicio.guardar(producto);
                    logger.debug("Producto creado: {}", producto.getCodigo());
                }

                resultado.agregarProductoExitoso(producto);
                exitosos++;

            } catch (Exception e) {
                logger.error("Error guardando producto {}: {}", producto.getCodigo(), e.getMessage());
                resultado.agregarError("Error guardando " + producto.getCodigo() + ": " + e.getMessage());
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
     * Guarda productos en la base de datos (método interno)
     */
    @Transactional
    public void guardarProductosEnBaseDatos(List<Producto> productos, ProductoImportacionResultadoDTO resultado) {
        int productosGuardados = 0;
        int erroresGuardado = 0;

        for (Producto producto : productos) {
            try {
                // Validar si el producto ya existe por código
                Producto existente = productoServicio.buscarPorCodigo(producto.getCodigo());

                if (existente != null) {
                    // Actualizar producto existente
                    actualizarProductoExistente(existente, producto);
                    productoServicio.actualizar(existente.getId(), existente);
                    logger.debug("Producto actualizado: {}", producto.getCodigo());
                } else {
                    // Crear nuevo producto
                    producto.setFechaCreacion(LocalDateTime.now());
                    producto.setFechaActualizacion(LocalDateTime.now());
                    producto.setActivo(true);
                    productoServicio.guardar(producto);
                    logger.debug("Producto creado: {}", producto.getCodigo());
                }

                productosGuardados++;

            } catch (Exception e) {
                logger.error("Error guardando producto {}: {}", producto.getCodigo(), e.getMessage());
                resultado.agregarError("Error guardando producto " + producto.getCodigo() + ": " + e.getMessage());
                erroresGuardado++;
            }
        }

        logger.info("Guardado en BD completado. Guardados: {}, Errores: {}", productosGuardados, erroresGuardado);
    }

    /**
     * Procesa archivo Excel y extrae productos
     */
    private List<Producto> procesarArchivoExcel(MultipartFile archivo, ProductoImportacionResultadoDTO resultado)
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
                        resultado.agregarProductoExitoso(producto);
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

        return productos;
    }

    /**
     * Valida encabezados del archivo Excel
     */
    private boolean validarEncabezados(Row headerRow, ProductoImportacionResultadoDTO resultado) {
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
     * Procesa una fila de Excel y crea un producto
     */
    private Producto procesarFilaExcel(Row row, int numeroFila) {
        Producto producto = new Producto();

        try {
            // Código (requerido)
            String codigo = limpiarTexto(obtenerValorCelda(row.getCell(0)));
            if (codigo.isEmpty()) {
                throw new IllegalArgumentException("El código del producto es obligatorio");
            }
            producto.setCodigo(codigo.toUpperCase());

            // Nombre (requerido)
            String nombre = limpiarTexto(obtenerValorCelda(row.getCell(1)));
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre del producto es obligatorio");
            }
            producto.setNombre(nombre);

            // Descripción (opcional)
            producto.setDescripcion(limpiarTexto(obtenerValorCelda(row.getCell(2))));

            // Precio (requerido)
            double precio = obtenerValorNumerico(row.getCell(3), "precio");
            if (precio <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a 0");
            }
            producto.setPrecio(precio);

            // Stock (requerido)
            double stockDouble = obtenerValorNumerico(row.getCell(4), "stock");
            if (stockDouble < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }
            producto.setStock((int) stockDouble);

            // Marca (opcional)
            producto.setMarca(limpiarTexto(obtenerValorCelda(row.getCell(5))));

            // Modelo (opcional)
            producto.setModelo(limpiarTexto(obtenerValorCelda(row.getCell(6))));

            // Establecer valores por defecto
            producto.setActivo(true);
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setFechaActualizacion(LocalDateTime.now());

            return producto;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    /**
     * Valida un producto antes de agregarlo a la lista
     */
    private boolean validarProducto(Producto producto, int numeroFila, ProductoImportacionResultadoDTO resultado) {
        List<String> errores = new ArrayList<>();

        // Validar código único
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            errores.add("Código requerido");
        }

        // Validar nombre
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            errores.add("Nombre requerido");
        }

        // Validar precio
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            errores.add("Precio debe ser mayor a 0");
        }

        // Validar stock
        if (producto.getStock() == null || producto.getStock() < 0) {
            errores.add("Stock no puede ser negativo");
        }

        if (!errores.isEmpty()) {
            resultado.agregarError("Fila " + numeroFila + ": " + String.join(", ", errores));
            return false;
        }

        return true;
    }

    /**
     * Actualiza un producto existente con los datos importados
     */
    private void actualizarProductoExistente(Producto existente, Producto nuevo) {
        existente.setNombre(nuevo.getNombre());
        existente.setDescripcion(nuevo.getDescripcion());
        existente.setPrecio(nuevo.getPrecio());
        existente.setStock(nuevo.getStock());
        existente.setMarca(nuevo.getMarca());
        existente.setModelo(nuevo.getModelo());
        existente.setFechaActualizacion(LocalDateTime.now());
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
     * Obtiene el valor numérico de una celda
     */
    private double obtenerValorNumerico(Cell cell, String nombreCampo) {
        if (cell == null) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es requerido");
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String valor = cell.getStringCellValue().trim();
                    if (valor.isEmpty()) {
                        throw new IllegalArgumentException("El campo " + nombreCampo + " no puede estar vacío");
                    }
                    return Double.parseDouble(valor);
                default:
                    throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser numérico");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " tiene formato inválido");
        }
    }

    /**
     * Limpia y formatea texto
     */
    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ");
    }

    @Override
    public ProductoImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        return importarProductosDesdeExcel(archivo);
    }

    @Override
    public boolean validarFormatoArchivo(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        return esArchivoExcel(nombre) || esArchivoCSV(nombre);
    }

    @Override
    public ProductoImportacionResultadoDTO obtenerVistaPreviaProductos(MultipartFile archivo, int limite) {
        logger.info("Obteniendo vista previa de productos del archivo: {}", archivo.getOriginalFilename());

        ProductoImportacionResultadoDTO resultado = new ProductoImportacionResultadoDTO();
        resultado.setTipoEntidad("Producto");
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            if (!esArchivoExcel(archivo.getOriginalFilename())) {
                resultado.agregarError("El archivo debe ser formato Excel (.xlsx)");
                return resultado;
            }

            try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                if (sheet.getPhysicalNumberOfRows() == 0) {
                    resultado.agregarError("El archivo está vacío");
                    return resultado;
                }

                if (!validarEncabezados(sheet.getRow(0), resultado)) {
                    return resultado;
                }

                int filasProcesadas = 0;
                for (int i = 1; i <= sheet.getLastRowNum() && filasProcesadas < limite; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || esFilaVacia(row)) {
                        continue;
                    }

                    try {
                        Producto producto = procesarFilaExcel(row, i + 1);
                        if (validarProducto(producto, i + 1, resultado)) {
                            resultado.agregarProductoExitoso(producto);
                        } else {
                            resultado.incrementarErrores();
                        }
                    } catch (Exception e) {
                        resultado.agregarError("Fila " + (i + 1) + ": " + e.getMessage());
                        resultado.incrementarErrores();
                    }
                    filasProcesadas++;
                }
            }

            resultado.calcularTotales();

        } catch (Exception e) {
            logger.error("Error general obteniendo vista previa: {}", e.getMessage(), e);
            resultado.agregarError("Error general: " + e.getMessage());
        }

        logger.info("Vista previa completada. Filas procesadas: {}, Exitosos: {}, Errores: {}",
                resultado.getRegistrosProcesados(), resultado.getRegistrosExitosos(), resultado.getRegistrosConError());

        return resultado;
    }

    @Override
    public byte[] generarPlantillaImportacion() throws IOException {
        logger.info("Generando plantilla de importación para productos");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");

            // Crear encabezados
            Row header = sheet.createRow(0);
            for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(ENCABEZADOS_ESPERADOS[i]);

                // Opcional: Aplicar estilo a los encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < ENCABEZADOS_ESPERADOS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo a un array de bytes
            try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
                workbook.write(bos);
                logger.info("Plantilla de importación generada exitosamente");
                return bos.toByteArray();
            }
        } catch (IOException e) {
            logger.error("Error generando plantilla de importación: {}", e.getMessage(), e);
            throw e; // Re-lanzar la excepción ya que está declarada en la interfaz
        }
    }
}