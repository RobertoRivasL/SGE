package informviva.gest.controlador;

import informviva.gest.dto.DetalleOrdenCompraDTO;
import informviva.gest.dto.OrdenCompraDTO;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.dto.ProveedorDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.OrdenCompra.EstadoOrden;
import informviva.gest.model.Usuario;
import informviva.gest.service.OrdenCompraServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ProveedorServicio;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de vistas para el módulo de Órdenes de Compra
 * Maneja todas las vistas relacionadas con gestión de órdenes de compra
 *
 * Características:
 * - Retorna vistas Thymeleaf
 * - Usa Model para pasar datos
 * - Control de acceso por roles (ADMIN, GERENTE, COMPRAS)
 * - Manejo robusto de errores
 * - Gestión completa del ciclo de vida de las órdenes
 * - Exportación a Excel
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Controller
@RequestMapping("/ordenes-compra")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
public class OrdenCompraControlador {

    private static final Logger logger = LoggerFactory.getLogger(OrdenCompraControlador.class);

    // ===== CONSTANTES DE VISTAS =====
    private static final String VISTA_LISTAR = "ordenes-compra/listar";
    private static final String VISTA_FORMULARIO = "ordenes-compra/formulario";
    private static final String VISTA_DETALLE = "ordenes-compra/detalle";
    private static final String VISTA_RECEPCION = "ordenes-compra/recepcion";

    // ===== CONSTANTES DE REDIRECCIÓN =====
    private static final String REDIRECT_LISTAR = "redirect:/ordenes-compra";
    private static final String REDIRECT_DETALLE = "redirect:/ordenes-compra/";

    // ===== CONSTANTES DE PARÁMETROS =====
    private static final String PARAM_MENSAJE_EXITO = "mensajeExito";
    private static final String PARAM_MENSAJE_ERROR = "mensajeError";
    private static final String PARAM_MENSAJE_ADVERTENCIA = "mensajeAdvertencia";

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    private final OrdenCompraServicio ordenCompraServicio;
    private final ProveedorServicio proveedorServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    public OrdenCompraControlador(OrdenCompraServicio ordenCompraServicio,
                                  ProveedorServicio proveedorServicio,
                                  ProductoServicio productoServicio,
                                  UsuarioServicio usuarioServicio) {
        this.ordenCompraServicio = ordenCompraServicio;
        this.proveedorServicio = proveedorServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // ==================== ENDPOINTS PRINCIPALES ====================

    /**
     * Lista órdenes de compra con filtros y paginación
     * GET /ordenes-compra
     *
     * @param page Número de página
     * @param size Tamaño de página
     * @param proveedorId Filtro por proveedor
     * @param estado Filtro por estado
     * @param fechaInicio Filtro fecha inicial
     * @param fechaFin Filtro fecha final
     * @param model Modelo para la vista
     * @return Vista de listado
     */
    @GetMapping({"", "/"})
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        try {
            logger.info("Listando órdenes de compra - Página: {}, Tamaño: {}", page, size);

            // Configurar paginación - ordenar por fecha descendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaOrden").descending());

            // Convertir estado String a EstadoOrden
            EstadoOrden estadoOrden = null;
            if (estado != null && !estado.trim().isEmpty()) {
                try {
                    estadoOrden = EstadoOrden.valueOf(estado);
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado de orden inválido: {}", estado);
                }
            }

            // Obtener órdenes con criterios
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            Page<OrdenCompraDTO> ordenesPage = ordenCompraServicio.buscarConCriterios(
                    proveedorId, estadoOrden, fechaInicio, fechaFin, null, pageable);

            // Datos para la vista
            model.addAttribute("ordenesPage", ordenesPage);
            model.addAttribute("proveedorId", proveedorId);
            model.addAttribute("estado", estado);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);

            // Cargar datos para filtros
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("estados", EstadoOrden.values());

            // Estadísticas
            model.addAttribute("totalOrdenes", ordenCompraServicio.contarTodos());
            model.addAttribute("ordenesPendientes", ordenCompraServicio.contarPorEstado(EstadoOrden.PENDIENTE));
            model.addAttribute("ordenesEnviadas", ordenCompraServicio.contarPorEstado(EstadoOrden.ENVIADA));
            model.addAttribute("ordenesCompletadas", ordenCompraServicio.contarPorEstado(EstadoOrden.COMPLETADA));

            logger.info("Órdenes listadas exitosamente: {} resultados", ordenesPage.getTotalElements());
            return VISTA_LISTAR;

        } catch (Exception e) {
            logger.error("Error al listar órdenes de compra: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al cargar las órdenes: " + e.getMessage());
            model.addAttribute("ordenesPage", Page.empty());
            return VISTA_LISTAR;
        }
    }

    /**
     * Muestra formulario para crear nueva orden de compra
     * GET /ordenes-compra/nueva
     *
     * @param model Modelo para la vista
     * @return Vista del formulario
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        try {
            logger.info("Mostrando formulario para nueva orden de compra");

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO ordenCompraDTO = new OrdenCompraDTO();
            ordenCompraDTO.setFechaOrden(LocalDate.now());
            ordenCompraDTO.setEstado(EstadoOrden.BORRADOR);
            ordenCompraDTO.setUsuarioCompradorId(usuarioActual.getId());

            model.addAttribute("orden", ordenCompraDTO);
            model.addAttribute("esNueva", true);
            model.addAttribute("titulo", "Nueva Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());

            return VISTA_FORMULARIO;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nueva orden: {}", e.getMessage(), e);
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Procesa la creación de una nueva orden de compra
     * POST /ordenes-compra
     *
     * @param ordenCompraDTO Datos de la orden
     * @param result Resultado de validación
     * @param redirectAttributes Atributos de redirección
     * @param model Modelo para la vista
     * @return Redirección a detalle o formulario con errores
     */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("orden") OrdenCompraDTO ordenCompraDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Intentando crear nueva orden de compra para proveedor ID: {}", ordenCompraDTO.getProveedorId());

        // Validación de errores de formulario
        if (result.hasErrors()) {
            logger.warn("Errores de validación en formulario de orden: {}", result.getAllErrors());
            model.addAttribute("esNueva", true);
            model.addAttribute("titulo", "Nueva Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());
            return VISTA_FORMULARIO;
        }

        try {
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());
            ordenCompraDTO.setUsuarioCompradorId(usuarioActual.getId());

            // Validar que tenga al menos un detalle
            if (ordenCompraDTO.getDetalles() == null || ordenCompraDTO.getDetalles().isEmpty()) {
                model.addAttribute(PARAM_MENSAJE_ERROR, "La orden debe tener al menos un detalle");
                model.addAttribute("esNueva", true);
                model.addAttribute("titulo", "Nueva Orden de Compra");
                model.addAttribute("proveedores", proveedorServicio.buscarActivos());
                model.addAttribute("productos", productoServicio.buscarActivos());
                return VISTA_FORMULARIO;
            }

            // Crear orden
            OrdenCompraDTO ordenGuardada = ordenCompraServicio.crear(ordenCompraDTO);

            logger.info("Orden de compra creada exitosamente: ID={}, Número={}",
                    ordenGuardada.getId(), ordenGuardada.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden de compra creada exitosamente: " + ordenGuardada.getNumeroOrden());

            return REDIRECT_DETALLE + ordenGuardada.getId();

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            model.addAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            model.addAttribute("esNueva", true);
            model.addAttribute("titulo", "Nueva Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());
            return VISTA_FORMULARIO;

        } catch (Exception e) {
            logger.error("Error al crear orden de compra: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al crear la orden: " + e.getMessage());
            model.addAttribute("esNueva", true);
            model.addAttribute("titulo", "Nueva Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Vista detallada de una orden de compra
     * GET /ordenes-compra/{id}
     *
     * @param id ID de la orden
     * @param model Modelo para la vista
     * @param redirectAttributes Atributos de redirección
     * @return Vista de detalle
     */
    @GetMapping("/{id}")
    public String ver(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Visualizando orden de compra ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.buscarPorId(id);

            model.addAttribute("orden", orden);
            model.addAttribute("titulo", "Orden de Compra - " + orden.getNumeroOrden());
            model.addAttribute("esModificable", orden.esModificable());
            model.addAttribute("esRecibible", orden.esRecibible());
            model.addAttribute("esCancelable", orden.esCancelable());

            return VISTA_DETALLE;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al visualizar orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cargar la orden: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Muestra formulario para editar orden existente
     * GET /ordenes-compra/{id}/editar
     *
     * @param id ID de la orden
     * @param model Modelo para la vista
     * @param redirectAttributes Atributos de redirección
     * @return Vista del formulario
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Mostrando formulario de edición para orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.buscarPorId(id);

            // Verificar si es modificable
            if (!orden.esModificable()) {
                logger.warn("Intento de editar orden no modificable: {}", orden.getEstado());
                redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA,
                        "La orden no puede ser modificada en estado: " + orden.getEstado().getDescripcion());
                return REDIRECT_DETALLE + id;
            }

            model.addAttribute("orden", orden);
            model.addAttribute("esNueva", false);
            model.addAttribute("titulo", "Editar Orden de Compra - " + orden.getNumeroOrden());
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());

            return VISTA_FORMULARIO;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de edición: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cargar la orden: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Procesa la actualización de una orden existente
     * POST /ordenes-compra/{id}/actualizar
     *
     * @param id ID de la orden
     * @param ordenCompraDTO Datos actualizados
     * @param result Resultado de validación
     * @param redirectAttributes Atributos de redirección
     * @param model Modelo para la vista
     * @return Redirección a detalle o formulario con errores
     */
    @PostMapping("/{id}/actualizar")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("orden") OrdenCompraDTO ordenCompraDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Intentando actualizar orden ID: {}", id);

        // Validación de errores de formulario
        if (result.hasErrors()) {
            logger.warn("Errores de validación en formulario de orden: {}", result.getAllErrors());
            model.addAttribute("esNueva", false);
            model.addAttribute("titulo", "Editar Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());
            return VISTA_FORMULARIO;
        }

        try {
            // Actualizar orden
            OrdenCompraDTO ordenActualizada = ordenCompraServicio.actualizar(id, ordenCompraDTO);

            logger.info("Orden actualizada exitosamente: ID={}, Número={}",
                    ordenActualizada.getId(), ordenActualizada.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden actualizada exitosamente: " + ordenActualizada.getNumeroOrden());

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede actualizar orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al actualizar orden: {}", e.getMessage(), e);
            model.addAttribute(PARAM_MENSAJE_ERROR, "Error al actualizar la orden: " + e.getMessage());
            model.addAttribute("esNueva", false);
            model.addAttribute("titulo", "Editar Orden de Compra");
            model.addAttribute("proveedores", proveedorServicio.buscarActivos());
            model.addAttribute("productos", productoServicio.buscarActivos());
            return VISTA_FORMULARIO;
        }
    }

    // ==================== CAMBIOS DE ESTADO ====================

    /**
     * Aprueba una orden de compra
     * POST /ordenes-compra/{id}/aprobar
     *
     * @param id ID de la orden
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String aprobar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Aprobando orden ID: {}", id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.aprobar(id, usuarioActual.getId());

            logger.info("Orden aprobada exitosamente: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden aprobada exitosamente: " + orden.getNumeroOrden());

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede aprobar orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al aprobar orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al aprobar la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    /**
     * Envía una orden al proveedor
     * POST /ordenes-compra/{id}/enviar
     *
     * @param id ID de la orden
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/enviar")
    public String enviar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Enviando orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.enviar(id);

            logger.info("Orden enviada exitosamente: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden enviada exitosamente: " + orden.getNumeroOrden());

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede enviar orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al enviar orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al enviar la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    /**
     * Confirma una orden con el proveedor
     * POST /ordenes-compra/{id}/confirmar
     *
     * @param id ID de la orden
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/confirmar")
    public String confirmar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Confirmando orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.confirmar(id);

            logger.info("Orden confirmada exitosamente: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden confirmada exitosamente: " + orden.getNumeroOrden());

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede confirmar orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al confirmar orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al confirmar la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    // ==================== RECEPCIÓN DE MERCANCÍA ====================

    /**
     * Muestra formulario de recepción de mercancía
     * GET /ordenes-compra/{id}/recibir
     *
     * @param id ID de la orden
     * @param model Modelo para la vista
     * @param redirectAttributes Atributos de redirección
     * @return Vista de recepción
     */
    @GetMapping("/{id}/recibir")
    public String mostrarFormularioRecepcion(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Mostrando formulario de recepción para orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.buscarPorId(id);

            // Verificar si es recibible
            if (!orden.esRecibible()) {
                logger.warn("Intento de recibir orden no recibible: {}", orden.getEstado());
                redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA,
                        "La orden no puede ser recibida en estado: " + orden.getEstado().getDescripcion());
                return REDIRECT_DETALLE + id;
            }

            model.addAttribute("orden", orden);
            model.addAttribute("titulo", "Recepción de Mercancía - " + orden.getNumeroOrden());

            return VISTA_RECEPCION;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de recepción: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cargar el formulario: " + e.getMessage());
            return REDIRECT_LISTAR;
        }
    }

    /**
     * Recibe completamente una orden
     * POST /ordenes-compra/{id}/recibir-completa
     *
     * @param id ID de la orden
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/recibir-completa")
    public String recibirCompleta(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Recibiendo orden completa ID: {}", id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.recibirCompleta(id, usuarioActual.getId());

            logger.info("Orden recibida completamente: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden recibida completamente: " + orden.getNumeroOrden() +
                    ". El inventario ha sido actualizado.");

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede recibir orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al recibir orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al recibir la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    /**
     * Recibe parcialmente una orden
     * POST /ordenes-compra/{id}/recibir-parcial
     *
     * @param id ID de la orden
     * @param cantidades Map con cantidades recibidas por detalle
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/recibir-parcial")
    public String recibirParcial(
            @PathVariable Long id,
            @RequestParam Map<String, String> cantidades,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Recibiendo orden parcial ID: {}", id);

            // Convertir cantidades de String a Map<Long, Integer>
            Map<Long, Integer> cantidadesPorDetalle = new HashMap<>();
            for (Map.Entry<String, String> entry : cantidades.entrySet()) {
                if (entry.getKey().startsWith("cantidad_")) {
                    Long detalleId = Long.parseLong(entry.getKey().replace("cantidad_", ""));
                    Integer cantidad = Integer.parseInt(entry.getValue());
                    if (cantidad > 0) {
                        cantidadesPorDetalle.put(detalleId, cantidad);
                    }
                }
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.recibirParcial(id, cantidadesPorDetalle, usuarioActual.getId());

            logger.info("Orden recibida parcialmente: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Recepción parcial registrada para la orden: " + orden.getNumeroOrden() +
                    ". El inventario ha sido actualizado.");

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error en recepción parcial: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al recibir orden parcialmente: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al recibir la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    /**
     * Cancela una orden de compra
     * POST /ordenes-compra/{id}/cancelar
     *
     * @param id ID de la orden
     * @param motivo Motivo de cancelación
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String cancelar(
            @PathVariable Long id,
            @RequestParam String motivo,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Cancelando orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.cancelar(id, motivo);

            logger.info("Orden cancelada: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Orden cancelada: " + orden.getNumeroOrden());

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTAR;

        } catch (IllegalStateException e) {
            logger.warn("No se puede cancelar orden: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al cancelar orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al cancelar la orden: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    // ==================== GESTIÓN DE DETALLES ====================

    /**
     * Agrega un detalle a una orden
     * POST /ordenes-compra/{id}/agregar-detalle
     *
     * @param id ID de la orden
     * @param detalle Detalle a agregar
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/agregar-detalle")
    public String agregarDetalle(
            @PathVariable Long id,
            @ModelAttribute DetalleOrdenCompraDTO detalle,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Agregando detalle a orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.agregarDetalle(id, detalle);

            logger.info("Detalle agregado a orden: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Detalle agregado exitosamente");

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (IllegalStateException e) {
            logger.warn("No se puede agregar detalle: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al agregar detalle: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al agregar el detalle: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    /**
     * Elimina un detalle de una orden
     * POST /ordenes-compra/{id}/eliminar-detalle/{detalleId}
     *
     * @param id ID de la orden
     * @param detalleId ID del detalle
     * @param redirectAttributes Atributos de redirección
     * @return Redirección a detalle
     */
    @PostMapping("/{id}/eliminar-detalle/{detalleId}")
    public String eliminarDetalle(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Eliminando detalle {} de orden ID: {}", detalleId, id);

            OrdenCompraDTO orden = ordenCompraServicio.eliminarDetalle(id, detalleId);

            logger.info("Detalle eliminado de orden: {}", orden.getNumeroOrden());

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Detalle eliminado exitosamente");

            return REDIRECT_DETALLE + id;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar detalle: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA, e.getMessage());
            return REDIRECT_DETALLE + id;

        } catch (Exception e) {
            logger.error("Error al eliminar detalle: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ERROR,
                    "Error al eliminar el detalle: " + e.getMessage());
            return REDIRECT_DETALLE + id;
        }
    }

    // ==================== EXPORTACIÓN ====================

    /**
     * Exporta órdenes de compra a Excel con filtros aplicados
     * GET /ordenes-compra/exportar/excel
     *
     * @param proveedorId Filtro por proveedor
     * @param estado Filtro por estado
     * @param fechaInicio Filtro fecha inicial
     * @param fechaFin Filtro fecha final
     * @param response Respuesta HTTP
     * @throws IOException Si ocurre error al escribir el archivo
     */
    @GetMapping("/exportar/excel")
    public void exportar(
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        try {
            logger.info("Iniciando exportación de órdenes de compra a Excel");

            // Convertir estado String a EstadoOrden
            EstadoOrden estadoOrden = null;
            if (estado != null && !estado.trim().isEmpty()) {
                try {
                    estadoOrden = EstadoOrden.valueOf(estado);
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado inválido para exportación: {}", estado);
                }
            }

            // Obtener todas las órdenes con filtros (sin paginación)
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("fechaOrden").descending());
            Page<OrdenCompraDTO> ordenesPage = ordenCompraServicio.buscarConCriterios(
                    proveedorId, estadoOrden, fechaInicio, fechaFin, null, pageable);

            // Crear workbook de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Órdenes de Compra");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Número Orden", "Fecha", "Proveedor", "Estado", "Total Unidades",
                "Subtotal", "IVA", "Total", "Fecha Entrega Est.", "Comprador"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (OrdenCompraDTO orden : ordenesPage.getContent()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(orden.getNumeroOrden() != null ? orden.getNumeroOrden() : "");

                Cell dateCell = row.createCell(1);
                if (orden.getFechaOrden() != null) {
                    dateCell.setCellValue(orden.getFechaOrden().format(formatter));
                }
                dateCell.setCellStyle(dateStyle);

                row.createCell(2).setCellValue(orden.getProveedorNombre() != null ? orden.getProveedorNombre() : "");
                row.createCell(3).setCellValue(orden.getEstado() != null ? orden.getEstado().getDescripcion() : "");
                row.createCell(4).setCellValue(orden.getTotalUnidades() != null ? orden.getTotalUnidades() : 0);

                Cell subtotalCell = row.createCell(5);
                if (orden.getSubtotal() != null) {
                    subtotalCell.setCellValue(orden.getSubtotal().doubleValue());
                }
                subtotalCell.setCellStyle(currencyStyle);

                Cell ivaCell = row.createCell(6);
                if (orden.getMontoImpuesto() != null) {
                    ivaCell.setCellValue(orden.getMontoImpuesto().doubleValue());
                }
                ivaCell.setCellStyle(currencyStyle);

                Cell totalCell = row.createCell(7);
                if (orden.getTotal() != null) {
                    totalCell.setCellValue(orden.getTotal().doubleValue());
                }
                totalCell.setCellStyle(currencyStyle);

                Cell entregaCell = row.createCell(8);
                if (orden.getFechaEntregaEstimada() != null) {
                    entregaCell.setCellValue(orden.getFechaEntregaEstimada().format(formatter));
                }
                entregaCell.setCellStyle(dateStyle);

                row.createCell(9).setCellValue(orden.getUsuarioCompradorNombre() != null ?
                        orden.getUsuarioCompradorNombre() : "");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Configurar respuesta HTTP
            String nombreArchivo = "ordenes_compra_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);

            // Escribir workbook a la respuesta
            workbook.write(response.getOutputStream());
            workbook.close();

            logger.info("Exportación completada exitosamente: {} órdenes exportadas",
                    ordenesPage.getContent().size());

        } catch (Exception e) {
            logger.error("Error al exportar órdenes: {}", e.getMessage(), e);
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

    /**
     * Crea estilo para celdas de moneda en Excel
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}
