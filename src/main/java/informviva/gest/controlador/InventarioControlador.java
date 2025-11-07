package informviva.gest.controlador;

import informviva.gest.dto.AjusteInventarioRequestDTO;
import informviva.gest.dto.EstadisticasInventarioDTO;
import informviva.gest.dto.MovimientoInventarioDTO;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import informviva.gest.model.Usuario;
import informviva.gest.service.InventarioServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Controlador de vistas para el módulo de Inventario
 * Maneja todas las vistas relacionadas con inventario y movimientos
 *
 * Características:
 * - Retorna vistas Thymeleaf
 * - Usa Model para pasar datos
 * - Control de acceso por roles
 * - Manejo robusto de errores
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Controller
@RequestMapping("/inventario")
public class InventarioControlador {

    private static final Logger logger = LoggerFactory.getLogger(InventarioControlador.class);

    // ===== CONSTANTES DE VISTAS =====
    private static final String VISTA_MOVIMIENTOS = "inventario/movimientos";
    private static final String VISTA_ESTADISTICAS = "inventario/estadisticas";
    private static final String VISTA_FORMULARIO_AJUSTE = "inventario/formulario-ajuste";
    private static final String VISTA_MOVIMIENTOS_PRODUCTO = "inventario/movimientos-producto";
    private static final String VISTA_ALERTAS_STOCK = "inventario/alertas-stock";

    // ===== CONSTANTES DE REDIRECCIÓN =====
    private static final String REDIRECT_MOVIMIENTOS = "redirect:/inventario/movimientos";
    private static final String REDIRECT_ESTADISTICAS = "redirect:/inventario/estadisticas";

    // ===== CONSTANTES DE PARÁMETROS =====
    private static final String PARAM_MENSAJE_EXITO = "mensajeExito";
    private static final String PARAM_MENSAJE_ERROR = "mensajeError";
    private static final String PARAM_MENSAJE_ADVERTENCIA = "mensajeAdvertencia";

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    private final InventarioServicio inventarioServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    public InventarioControlador(InventarioServicio inventarioServicio,
                                 ProductoServicio productoServicio,
                                 UsuarioServicio usuarioServicio) {
        this.inventarioServicio = inventarioServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // ==================== ENDPOINTS PRINCIPALES ====================

    /**
     * Lista movimientos de inventario con filtros y paginación
     * GET /inventario/movimientos
     */
    @GetMapping({"/movimientos", ""})
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String listarMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long usuarioId,
            Model model) {

        try {
            // Configurar paginación - ordenar por fecha descendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

            // Convertir tipo String a TipoMovimiento
            TipoMovimiento tipoMovimiento = null;
            if (tipo != null && !tipo.trim().isEmpty()) {
                try {
                    tipoMovimiento = TipoMovimiento.valueOf(tipo);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de movimiento inválido: {}", tipo);
                }
            }

            // Convertir fechas a LocalDateTime
            LocalDateTime fechaInicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

            // Obtener movimientos con criterios
            Page<MovimientoInventarioDTO> movimientosPage = inventarioServicio.buscarConCriterios(
                    productoId, tipoMovimiento, fechaInicioDateTime, fechaFinDateTime, usuarioId, pageable);

            // Cargar datos para el modelo
            model.addAttribute("movimientosPage", movimientosPage);
            model.addAttribute("productos", productoServicio.buscarActivos());
            model.addAttribute("tiposMovimiento", TipoMovimiento.values());

            // Filtros actuales
            model.addAttribute("productoId", productoId);
            model.addAttribute("tipo", tipo);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("usuarioId", usuarioId);

            // Estadísticas rápidas
            if (fechaInicioDateTime != null && fechaFinDateTime != null) {
                long totalMovimientos = inventarioServicio.contarMovimientosPeriodo(fechaInicioDateTime, fechaFinDateTime);
                model.addAttribute("totalMovimientosPeriodo", totalMovimientos);
            }

            return VISTA_MOVIMIENTOS;

        } catch (Exception e) {
            logger.error("Error al listar movimientos: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar los movimientos: " + e.getMessage());
            model.addAttribute("movimientosPage", Page.empty());
            return VISTA_MOVIMIENTOS;
        }
    }

    /**
     * Muestra dashboard de estadísticas del inventario
     * GET /inventario/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String verEstadisticas(Model model) {
        try {
            // Obtener estadísticas completas
            EstadisticasInventarioDTO estadisticas = inventarioServicio.obtenerEstadisticas();

            // Obtener alertas de stock
            List<EstadisticasInventarioDTO.ProductoStockDTO> productosStockBajo =
                    inventarioServicio.obtenerProductosStockBajo();

            List<EstadisticasInventarioDTO.ProductoStockDTO> productosSinStock =
                    inventarioServicio.obtenerProductosSinStock();

            // Agregar al modelo
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("productosStockBajo", productosStockBajo);
            model.addAttribute("productosSinStock", productosSinStock);
            model.addAttribute("fechaActual", LocalDateTime.now());

            logger.info("Estadísticas de inventario generadas exitosamente");
            return VISTA_ESTADISTICAS;

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de inventario: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar las estadísticas: " + e.getMessage());
            model.addAttribute("estadisticas", new EstadisticasInventarioDTO(LocalDateTime.now()));
            return VISTA_ESTADISTICAS;
        }
    }

    /**
     * Muestra formulario para registrar un ajuste de inventario
     * GET /inventario/ajuste
     */
    @GetMapping("/ajuste")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String mostrarFormularioAjuste(Model model) {
        try {
            model.addAttribute("ajusteForm", new AjusteInventarioRequestDTO());
            model.addAttribute("productos", productoServicio.buscarActivos());
            model.addAttribute("titulo", "Registrar Ajuste de Inventario");

            return VISTA_FORMULARIO_AJUSTE;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de ajuste: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar el formulario: " + e.getMessage());
            return REDIRECT_MOVIMIENTOS;
        }
    }

    /**
     * Procesa el registro de un ajuste de inventario
     * POST /inventario/ajuste
     */
    @PostMapping("/ajuste")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String registrarAjuste(
            @Valid @ModelAttribute("ajusteForm") AjusteInventarioRequestDTO ajusteForm,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            logger.warn("Errores de validación en formulario de ajuste: {}", result.getAllErrors());
            model.addAttribute("productos", productoServicio.buscarActivos());
            model.addAttribute("titulo", "Registrar Ajuste de Inventario");
            return VISTA_FORMULARIO_AJUSTE;
        }

        try {
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            if (usuarioActual == null) {
                throw new IllegalStateException("No se pudo identificar el usuario actual");
            }

            // Validar producto
            ProductoDTO producto = productoServicio.buscarPorId(ajusteForm.getProductoId());
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado");
            }

            // Validar stock disponible para ajustes negativos
            if (ajusteForm.getCantidad() < 0) {
                Integer stockActual = inventarioServicio.obtenerStockActual(ajusteForm.getProductoId());
                if (stockActual + ajusteForm.getCantidad() < 0) {
                    model.addAttribute(PARAM_MENSAJE_ERROR,
                            "Stock insuficiente. Stock actual: " + stockActual +
                            ", Intento de restar: " + Math.abs(ajusteForm.getCantidad()));
                    model.addAttribute("productos", productoServicio.buscarActivos());
                    model.addAttribute("titulo", "Registrar Ajuste de Inventario");
                    return VISTA_FORMULARIO_AJUSTE;
                }
            }

            // Registrar el ajuste
            MovimientoInventarioDTO movimiento = inventarioServicio.registrarAjuste(
                    ajusteForm.getProductoId(),
                    ajusteForm.getCantidad(),
                    ajusteForm.getMotivo(),
                    usuarioActual.getId()
            );

            logger.info("Ajuste de inventario registrado exitosamente. Movimiento ID: {}, Producto: {}, Cantidad: {}",
                    movimiento.getId(), producto.getNombre(), ajusteForm.getCantidad());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Ajuste registrado exitosamente. " +
                    "Producto: " + producto.getNombre() + ", " +
                    "Cantidad: " + ajusteForm.getCantidad() + ", " +
                    "Stock nuevo: " + movimiento.getStockNuevo());

            return REDIRECT_MOVIMIENTOS;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado al registrar ajuste: {}", e.getMessage());
            model.addAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            model.addAttribute("productos", productoServicio.buscarActivos());
            model.addAttribute("titulo", "Registrar Ajuste de Inventario");
            return VISTA_FORMULARIO_AJUSTE;

        } catch (Exception e) {
            logger.error("Error al registrar ajuste: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al registrar el ajuste: " + e.getMessage());
            model.addAttribute("productos", productoServicio.buscarActivos());
            model.addAttribute("titulo", "Registrar Ajuste de Inventario");
            return VISTA_FORMULARIO_AJUSTE;
        }
    }

    /**
     * Ver movimientos de un producto específico
     * GET /inventario/producto/{id}/movimientos
     */
    @GetMapping("/producto/{id}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String verMovimientosProducto(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Buscar producto
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado con ID: " + id);
            }

            // Configurar paginación
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

            // Obtener movimientos del producto
            Page<MovimientoInventarioDTO> movimientosPage = inventarioServicio.buscarPorProducto(id, pageable);

            // Estadísticas del producto
            long totalMovimientos = inventarioServicio.contarMovimientosProducto(id);
            Integer stockActual = inventarioServicio.obtenerStockActual(id);

            // Agregar al modelo
            model.addAttribute("producto", producto);
            model.addAttribute("movimientosPage", movimientosPage);
            model.addAttribute("totalMovimientos", totalMovimientos);
            model.addAttribute("stockActual", stockActual);

            return VISTA_MOVIMIENTOS_PRODUCTO;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Producto no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_MOVIMIENTOS;

        } catch (Exception e) {
            logger.error("Error al obtener movimientos del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cargar movimientos del producto: " + e.getMessage());
            return REDIRECT_MOVIMIENTOS;
        }
    }

    /**
     * Ver productos con stock bajo (alertas)
     * GET /inventario/alertas/stock-bajo
     */
    @GetMapping("/alertas/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String verProductosStockBajo(Model model) {
        try {
            // Obtener productos con stock bajo
            List<EstadisticasInventarioDTO.ProductoStockDTO> productosStockBajo =
                    inventarioServicio.obtenerProductosStockBajo();

            // Obtener productos sin stock
            List<EstadisticasInventarioDTO.ProductoStockDTO> productosSinStock =
                    inventarioServicio.obtenerProductosSinStock();

            model.addAttribute("productosStockBajo", productosStockBajo);
            model.addAttribute("productosSinStock", productosSinStock);
            model.addAttribute("totalAlertasBajo", productosStockBajo.size());
            model.addAttribute("totalAlertasSinStock", productosSinStock.size());
            model.addAttribute("fechaConsulta", LocalDateTime.now());

            logger.info("Consulta de alertas de stock: {} con stock bajo, {} sin stock",
                    productosStockBajo.size(), productosSinStock.size());

            return VISTA_ALERTAS_STOCK;

        } catch (Exception e) {
            logger.error("Error al obtener productos con stock bajo: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar las alertas: " + e.getMessage());
            model.addAttribute("productosStockBajo", Collections.emptyList());
            model.addAttribute("productosSinStock", Collections.emptyList());
            return VISTA_ALERTAS_STOCK;
        }
    }

    /**
     * Exportar movimientos a Excel
     * GET /inventario/exportar/excel
     */
    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public void exportarMovimientos(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        try {
            logger.info("Iniciando exportación de movimientos a Excel");

            // Convertir tipo String a TipoMovimiento
            TipoMovimiento tipoMovimiento = null;
            if (tipo != null && !tipo.trim().isEmpty()) {
                try {
                    tipoMovimiento = TipoMovimiento.valueOf(tipo);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de movimiento inválido para exportación: {}", tipo);
                }
            }

            // Convertir fechas
            LocalDateTime fechaInicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

            // Obtener todos los movimientos sin paginación
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("fecha").descending());
            Page<MovimientoInventarioDTO> movimientosPage = inventarioServicio.buscarConCriterios(
                    productoId, tipoMovimiento, fechaInicioDateTime, fechaFinDateTime, null, pageable);

            // Crear workbook de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Movimientos Inventario");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Producto", "Código", "Tipo", "Cantidad",
                               "Stock Anterior", "Stock Nuevo", "Motivo", "Usuario"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (MovimientoInventarioDTO movimiento : movimientosPage.getContent()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(movimiento.getId());

                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(movimiento.getFecha().format(formatter));
                dateCell.setCellStyle(dateStyle);

                row.createCell(2).setCellValue(movimiento.getProductoNombre() != null ?
                        movimiento.getProductoNombre() : "N/A");
                row.createCell(3).setCellValue(movimiento.getProductoCodigo() != null ?
                        movimiento.getProductoCodigo() : "N/A");
                row.createCell(4).setCellValue(movimiento.getTipo().toString());
                row.createCell(5).setCellValue(movimiento.getCantidad());
                row.createCell(6).setCellValue(movimiento.getStockAnterior() != null ?
                        movimiento.getStockAnterior() : 0);
                row.createCell(7).setCellValue(movimiento.getStockNuevo() != null ?
                        movimiento.getStockNuevo() : 0);
                row.createCell(8).setCellValue(movimiento.getMotivo() != null ?
                        movimiento.getMotivo() : "");
                row.createCell(9).setCellValue(movimiento.getUsuarioNombre() != null ?
                        movimiento.getUsuarioNombre() : "N/A");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Configurar respuesta HTTP
            String nombreArchivo = "movimientos_inventario_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);

            // Escribir workbook a la respuesta
            workbook.write(response.getOutputStream());
            workbook.close();

            logger.info("Exportación completada exitosamente: {} movimientos exportados",
                    movimientosPage.getContent().size());

        } catch (Exception e) {
            logger.error("Error al exportar movimientos: {}", e.getMessage(), e);
            throw new IOException("Error al generar el archivo Excel", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Crea estilo para encabezados de Excel
     */
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

    /**
     * Crea estilo para celdas de fecha en Excel
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}
