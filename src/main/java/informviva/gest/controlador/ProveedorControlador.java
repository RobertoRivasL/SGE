package informviva.gest.controlador;

import informviva.gest.dto.ProveedorDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.service.ProveedorServicio;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de vistas para el módulo de Proveedores
 * Maneja todas las vistas relacionadas con gestión de proveedores
 *
 * Características:
 * - Retorna vistas Thymeleaf
 * - Usa Model para pasar datos
 * - Control de acceso por roles (ADMIN, GERENTE)
 * - Manejo robusto de errores
 * - Exportación a Excel
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Controller
@RequestMapping("/proveedores")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ProveedorControlador {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorControlador.class);

    // ===== CONSTANTES DE VISTAS =====
    private static final String VISTA_LISTAR = "proveedores/listar";
    private static final String VISTA_FORMULARIO = "proveedores/formulario";
    private static final String VISTA_DETALLE = "proveedores/detalle";

    // ===== CONSTANTES DE REDIRECCIÓN =====
    private static final String REDIRECT_LISTAR = "redirect:/proveedores";

    // ===== CONSTANTES DE PARÁMETROS =====
    private static final String PARAM_MENSAJE_EXITO = "mensajeExito";
    private static final String PARAM_MENSAJE_ERROR = "mensajeError";
    private static final String PARAM_MENSAJE_ADVERTENCIA = "mensajeAdvertencia";

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    private final ProveedorServicio proveedorServicio;

    public ProveedorControlador(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }

    // ==================== ENDPOINTS PRINCIPALES ====================

    /**
     * Lista proveedores con filtros y paginación
     * GET /proveedores
     *
     * @param page Número de página (default: 0)
     * @param size Tamaño de página (default: 20)
     * @param nombre Filtro por nombre
     * @param categoria Filtro por categoría
     * @param ciudad Filtro por ciudad
     * @param soloActivos Filtro solo activos
     * @param model Modelo para la vista
     * @return Vista de listado de proveedores
     */
    @GetMapping({"", "/"})
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos,
            Model model) {

        try {
            logger.info("Listando proveedores - Página: {}, Tamaño: {}, Filtros: nombre={}, categoria={}, ciudad={}, soloActivos={}",
                    page, size, nombre, categoria, ciudad, soloActivos);

            // Configurar paginación - ordenar por nombre ascendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());

            // Aplicar filtros
            boolean activos = Boolean.TRUE.equals(soloActivos);
            Page<ProveedorDTO> proveedoresPage = proveedorServicio.buscarConCriterios(
                    nombre, categoria, ciudad, activos, pageable);

            // Datos para la vista
            model.addAttribute("proveedoresPage", proveedoresPage);
            model.addAttribute("nombre", nombre);
            model.addAttribute("categoria", categoria);
            model.addAttribute("ciudad", ciudad);
            model.addAttribute("soloActivos", soloActivos);

            // Cargar datos para filtros
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());

            // Estadísticas
            model.addAttribute("totalProveedores", proveedorServicio.contarTodos());
            model.addAttribute("proveedoresActivos", proveedorServicio.contarActivos());
            model.addAttribute("proveedoresInactivos", proveedorServicio.contarInactivos());

            logger.info("Proveedores listados exitosamente: {} resultados", proveedoresPage.getTotalElements());
            return VISTA_LISTAR;

        } catch (Exception e) {
            logger.error("Error al listar proveedores: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar los proveedores: " + e.getMessage());
            model.addAttribute("proveedoresPage", Page.empty());
            return VISTA_LISTAR;
        }
    }

    /**
     * Muestra formulario para crear nuevo proveedor
     * GET /proveedores/nuevo
     *
     * @param model Modelo para la vista
     * @return Vista del formulario
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        try {
            logger.info("Mostrando formulario para nuevo proveedor");

            ProveedorDTO proveedorDTO = new ProveedorDTO();
            proveedorDTO.setActivo(true);

            model.addAttribute("proveedor", proveedorDTO);
            model.addAttribute("esNuevo", true);
            model.addAttribute("titulo", "Nuevo Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());

            return VISTA_FORMULARIO;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nuevo proveedor: {}", e.getMessage(), e);
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Procesa el guardado de un nuevo proveedor
     * POST /proveedores
     *
     * @param proveedorDTO Datos del proveedor
     * @param result Resultado de validación
     * @param redirectAttributes Atributos de redirección
     * @param model Modelo para la vista
     * @return Redirección a listado o formulario con errores
     */
    @PostMapping
    public String guardar(
            @Valid @ModelAttribute("proveedor") ProveedorDTO proveedorDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Intentando guardar nuevo proveedor: {}", proveedorDTO.getNombre());

        // Validación de errores de formulario
        if (result.hasErrors()) {
            logger.warn("Errores de validación en formulario de proveedor: {}", result.getAllErrors());
            model.addAttribute("esNuevo", true);
            model.addAttribute("titulo", "Nuevo Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;
        }

        try {
            // Validar RUT único
            if (proveedorServicio.existeRut(proveedorDTO.getRut())) {
                logger.warn("RUT duplicado: {}", proveedorDTO.getRut());
                result.rejectValue("rut", "error.proveedor", "Ya existe un proveedor con este RUT");
                model.addAttribute("esNuevo", true);
                model.addAttribute("titulo", "Nuevo Proveedor");
                model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
                return VISTA_FORMULARIO;
            }

            // Guardar proveedor
            ProveedorDTO proveedorGuardado = proveedorServicio.guardar(proveedorDTO);

            logger.info("Proveedor guardado exitosamente: ID={}, Nombre={}",
                    proveedorGuardado.getId(), proveedorGuardado.getNombre());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Proveedor creado exitosamente: " + proveedorGuardado.getNombre());

            return REDIRECT_LISTAR;

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al guardar proveedor: {}", e.getMessage());
            model.addAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            model.addAttribute("esNuevo", true);
            model.addAttribute("titulo", "Nuevo Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;

        } catch (Exception e) {
            logger.error("Error al guardar proveedor: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al guardar el proveedor: " + e.getMessage());
            model.addAttribute("esNuevo", true);
            model.addAttribute("titulo", "Nuevo Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Muestra formulario para editar proveedor existente
     * GET /proveedores/{id}/editar
     *
     * @param id ID del proveedor
     * @param model Modelo para la vista
     * @param redirectAttributes Atributos de redirección
     * @return Vista del formulario o redirección
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Mostrando formulario de edición para proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.buscarPorId(id);

            model.addAttribute("proveedor", proveedor);
            model.addAttribute("esNuevo", false);
            model.addAttribute("titulo", "Editar Proveedor - " + proveedor.getNombre());
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());

            return VISTA_FORMULARIO;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de edición: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cargar el proveedor: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Procesa la actualización de un proveedor existente
     * POST /proveedores/{id}/actualizar
     *
     * @param id ID del proveedor
     * @param proveedorDTO Datos actualizados
     * @param result Resultado de validación
     * @param redirectAttributes Atributos de redirección
     * @param model Modelo para la vista
     * @return Redirección a listado o formulario con errores
     */
    @PostMapping("/{id}/actualizar")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("proveedor") ProveedorDTO proveedorDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Intentando actualizar proveedor ID: {}", id);

        // Validación de errores de formulario
        if (result.hasErrors()) {
            logger.warn("Errores de validación en formulario de proveedor: {}", result.getAllErrors());
            model.addAttribute("esNuevo", false);
            model.addAttribute("titulo", "Editar Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;
        }

        try {
            // Validar RUT único (excluyendo el mismo proveedor)
            if (proveedorServicio.existeRutExcluyendo(proveedorDTO.getRut(), id)) {
                logger.warn("RUT duplicado: {}", proveedorDTO.getRut());
                result.rejectValue("rut", "error.proveedor", "Ya existe otro proveedor con este RUT");
                model.addAttribute("esNuevo", false);
                model.addAttribute("titulo", "Editar Proveedor");
                model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
                return VISTA_FORMULARIO;
            }

            // Actualizar proveedor
            ProveedorDTO proveedorActualizado = proveedorServicio.actualizar(id, proveedorDTO);

            logger.info("Proveedor actualizado exitosamente: ID={}, Nombre={}",
                    proveedorActualizado.getId(), proveedorActualizado.getNombre());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Proveedor actualizado exitosamente: " + proveedorActualizado.getNombre());

            return REDIRECT_LISTAR;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al actualizar proveedor: {}", e.getMessage());
            model.addAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            model.addAttribute("esNuevo", false);
            model.addAttribute("titulo", "Editar Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;

        } catch (Exception e) {
            logger.error("Error al actualizar proveedor: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al actualizar el proveedor: " + e.getMessage());
            model.addAttribute("esNuevo", false);
            model.addAttribute("titulo", "Editar Proveedor");
            model.addAttribute("categorias", proveedorServicio.obtenerCategorias());
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Elimina un proveedor
     * POST /proveedores/{id}/eliminar
     *
     * @param id ID del proveedor
     * @param redirectAttributes Atributos de redirección
     * @return Redirección al listado
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Intentando eliminar proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.buscarPorId(id);

            // Verificar si tiene órdenes asociadas
            if (proveedorServicio.tieneOrdenesCompra(id)) {
                logger.warn("No se puede eliminar proveedor con órdenes asociadas: {}", proveedor.getNombre());
                redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA,
                        "No se puede eliminar el proveedor '" + proveedor.getNombre() +
                        "' porque tiene órdenes de compra asociadas. Puede desactivarlo en su lugar.");
                return REDIRECT_LISTAR;
            }

            proveedorServicio.eliminar(id);

            logger.info("Proveedor eliminado exitosamente: ID={}, Nombre={}", id, proveedor.getNombre());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Proveedor eliminado exitosamente: " + proveedor.getNombre());

            return REDIRECT_LISTAR;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar proveedor: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al eliminar proveedor: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al eliminar el proveedor: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Activa un proveedor
     * POST /proveedores/{id}/activar
     *
     * @param id ID del proveedor
     * @param redirectAttributes Atributos de redirección
     * @return Redirección al listado
     */
    @PostMapping("/{id}/activar")
    public String activar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Activando proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.activar(id);

            logger.info("Proveedor activado exitosamente: {}", proveedor.getNombre());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Proveedor activado exitosamente: " + proveedor.getNombre());

            return REDIRECT_LISTAR;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al activar proveedor: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al activar el proveedor: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Desactiva un proveedor
     * POST /proveedores/{id}/desactivar
     *
     * @param id ID del proveedor
     * @param redirectAttributes Atributos de redirección
     * @return Redirección al listado
     */
    @PostMapping("/{id}/desactivar")
    public String desactivar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Desactivando proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.desactivar(id);

            logger.info("Proveedor desactivado exitosamente: {}", proveedor.getNombre());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Proveedor desactivado exitosamente: " + proveedor.getNombre());

            return REDIRECT_LISTAR;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al desactivar proveedor: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al desactivar el proveedor: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Exporta proveedores a Excel con filtros aplicados
     * GET /proveedores/exportar/excel
     *
     * @param nombre Filtro por nombre
     * @param categoria Filtro por categoría
     * @param ciudad Filtro por ciudad
     * @param soloActivos Filtro solo activos
     * @param response Respuesta HTTP
     * @throws IOException Si ocurre error al escribir el archivo
     */
    @GetMapping("/exportar/excel")
    public void exportar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos,
            HttpServletResponse response) throws IOException {

        try {
            logger.info("Iniciando exportación de proveedores a Excel");

            // Obtener todos los proveedores con filtros (sin paginación)
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("nombre").ascending());
            boolean activos = Boolean.TRUE.equals(soloActivos);
            Page<ProveedorDTO> proveedoresPage = proveedorServicio.buscarConCriterios(
                    nombre, categoria, ciudad, activos, pageable);

            // Crear workbook de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Proveedores");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "RUT", "Nombre", "Nombre Fantasía", "Giro", "Categoría",
                "Ciudad", "Teléfono", "Email", "Contacto", "Días Crédito",
                "Calificación", "Estado", "Fecha Creación"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (ProveedorDTO proveedor : proveedoresPage.getContent()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(proveedor.getId());
                row.createCell(1).setCellValue(proveedor.getRut() != null ? proveedor.getRut() : "");
                row.createCell(2).setCellValue(proveedor.getNombre() != null ? proveedor.getNombre() : "");
                row.createCell(3).setCellValue(proveedor.getNombreFantasia() != null ? proveedor.getNombreFantasia() : "");
                row.createCell(4).setCellValue(proveedor.getGiro() != null ? proveedor.getGiro() : "");
                row.createCell(5).setCellValue(proveedor.getCategoria() != null ? proveedor.getCategoria() : "");
                row.createCell(6).setCellValue(proveedor.getCiudad() != null ? proveedor.getCiudad() : "");
                row.createCell(7).setCellValue(proveedor.getTelefono() != null ? proveedor.getTelefono() : "");
                row.createCell(8).setCellValue(proveedor.getEmail() != null ? proveedor.getEmail() : "");
                row.createCell(9).setCellValue(proveedor.getContactoNombre() != null ? proveedor.getContactoNombre() : "");
                row.createCell(10).setCellValue(proveedor.getDiasCredito() != null ? proveedor.getDiasCredito() : 0);
                row.createCell(11).setCellValue(proveedor.getCalificacion() != null ? proveedor.getCalificacion() : 0);
                row.createCell(12).setCellValue(proveedor.getActivo() ? "Activo" : "Inactivo");

                Cell dateCell = row.createCell(13);
                if (proveedor.getFechaCreacion() != null) {
                    dateCell.setCellValue(proveedor.getFechaCreacion().format(formatter));
                }
                dateCell.setCellStyle(dateStyle);
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Configurar respuesta HTTP
            String nombreArchivo = "proveedores_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);

            // Escribir workbook a la respuesta
            workbook.write(response.getOutputStream());
            workbook.close();

            logger.info("Exportación completada exitosamente: {} proveedores exportados",
                    proveedoresPage.getContent().size());

        } catch (Exception e) {
            logger.error("Error al exportar proveedores: {}", e.getMessage(), e);
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
