package informviva.gest.controlador;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.service.VentaServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador refactorizado para el manejo de ventas
 * Aplica principios SOLID, manejo robusto de errores y separación de responsabilidades
 *
 * @author Roberto Rivas
 * @version 4.0 - REFACTORIZADO COMPLETO
 */
@Controller
@RequestMapping("/ventas")
public class VentaControlador {

    private static final Logger logger = LoggerFactory.getLogger(VentaControlador.class);

    // ===== CONSTANTES DE VISTAS =====
    private static final String VISTA_LISTA = "ventas/lista";
    private static final String VISTA_NUEVA = "ventas/nueva";
    private static final String VISTA_DETALLE = "ventas/detalle";
    private static final String VISTA_EDITAR = "ventas/editar";
    private static final String VISTA_ERROR = "error/general";

    // ===== CONSTANTES DE PARÁMETROS =====
    private static final String PARAM_VENTA = "venta";
    private static final String PARAM_VENTAS = "ventas";
    private static final String PARAM_CLIENTES = "clientes";
    private static final String PARAM_PRODUCTOS = "productos";
    private static final String PARAM_VENDEDORES = "vendedores";
    private static final String PARAM_ERROR = "error";
    private static final String PARAM_MENSAJE_EXITO = "mensajeExito";
    private static final String PARAM_MENSAJE_ADVERTENCIA = "mensajeAdvertencia";

    // ===== CONSTANTES DE REDIRECCIÓN =====
    private static final String REDIRECT_LISTA = "redirect:/ventas/lista";
    private static final String REDIRECT_DETALLE = "redirect:/ventas/detalle/";

    // ===== CONSTANTES DE MENSAJES =====
    private static final String MENSAJE_VENTA_GUARDADA = "Venta guardada exitosamente con ID: ";
    private static final String MENSAJE_VENTA_ACTUALIZADA = "Venta actualizada exitosamente";
    private static final String MENSAJE_VENTA_ELIMINADA = "Venta eliminada exitosamente";
    private static final String ERROR_VENTA_NO_ENCONTRADA = "Venta no encontrada con ID: ";
    private static final String ERROR_CLIENTE_NO_ENCONTRADO = "Cliente no encontrado con ID: ";
    private static final String ERROR_PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";
    private static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado con ID: ";
    private static final String ERROR_GENERICO = "Ha ocurrido un error inesperado";

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    private final VentaServicio ventaServicio;
    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    public VentaControlador(VentaServicio ventaServicio,
                            ClienteServicio clienteServicio,
                            ProductoServicio productoServicio,
                            UsuarioServicio usuarioServicio) {
        this.ventaServicio = ventaServicio;
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // ==================== ENDPOINTS PRINCIPALES ====================

    /**
     * Lista las ventas con filtros y paginación
     */
    @GetMapping({"/lista", ""})
    public String listarVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String metodoPago,
            Model model) {

        try {
            List<Venta> ventas = obtenerVentasFiltradas(fechaInicio, fechaFin, clienteId, vendedorId, estado, metodoPago);
            cargarDatosListado(model, ventas, fechaInicio, fechaFin, clienteId, vendedorId, estado, metodoPago);
            return VISTA_LISTA;

        } catch (Exception e) {
            logger.error("Error al listar ventas: {}", e.getMessage(), e);
            model.addAttribute(PARAM_ERROR, "Error al cargar las ventas: " + e.getMessage());
            model.addAttribute(PARAM_VENTAS, Collections.emptyList());
            return VISTA_LISTA;
        }
    }

    /**
     * Muestra el formulario para crear una nueva venta
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaVenta(Model model) {
        try {
            VentaDTO ventaDTO = crearVentaDTOVacia();
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_NUEVA;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nueva venta: {}", e.getMessage(), e);
            model.addAttribute(PARAM_ERROR, "Error al cargar el formulario de nueva venta");
            return REDIRECT_LISTA;
        }
    }

    /**
     * Procesa la creación de una nueva venta
     */
    @PostMapping("/guardar")
    public String guardarVenta(
            @Valid @ModelAttribute(PARAM_VENTA) VentaDTO ventaDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Asegurar que la fecha esté establecida
        if (ventaDTO.getFecha() == null) {
            ventaDTO.setFecha(LocalDateTime.now());
        }

        if (result.hasErrors()) {
            logger.warn("Errores de validación al guardar venta: {}", result.getAllErrors());
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_NUEVA;
        }

        try {
            Venta ventaGuardada = procesarGuardadoVenta(ventaDTO);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    MENSAJE_VENTA_GUARDADA + ventaGuardada.getId());
            return REDIRECT_DETALLE + ventaGuardada.getId();

        } catch (StockInsuficienteException e) {
            return manejarErrorStock(e, model, ventaDTO);
        } catch (RecursoNoEncontradoException e) {
            return manejarErrorRecursoNoEncontrado(e, model, ventaDTO);
        } catch (Exception e) {
            return manejarErrorGeneral(e, model, ventaDTO, "Error al guardar la venta");
        }
    }

    /**
     * Muestra los detalles de una venta específica
     */
    @GetMapping("/detalle/{id}")
    public String mostrarDetalleVenta(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Venta venta = buscarVentaPorIdSeguro(id);
            model.addAttribute(PARAM_VENTA, venta);
            return VISTA_DETALLE;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        } catch (Exception e) {
            logger.error("Error al mostrar detalle de venta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_ERROR, "Error al cargar los detalles de la venta");
            return REDIRECT_LISTA;
        }
    }

    /**
     * Muestra el formulario para editar una venta
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarVenta(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Venta venta = buscarVentaPorIdSeguro(id);

            // Verificar si la venta se puede editar
            if (!ventaEsEditable(venta)) {
                redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA,
                        "No se puede editar una venta en estado: " + venta.getEstado());
                return REDIRECT_DETALLE + id;
            }

            VentaDTO ventaDTO = ventaServicio.convertirADTO(venta);
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_EDITAR;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para editar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        } catch (Exception e) {
            logger.error("Error al mostrar formulario de edición: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_ERROR, "Error al cargar el formulario de edición");
            return REDIRECT_LISTA;
        }
    }

    /**
     * Procesa la actualización de una venta
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarVenta(
            @PathVariable Long id,
            @Valid @ModelAttribute(PARAM_VENTA) VentaDTO ventaDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            logger.warn("Errores de validación al actualizar venta ID {}: {}", id, result.getAllErrors());
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_EDITAR;
        }

        try {
            Venta ventaActualizada = procesarActualizacionVenta(id, ventaDTO);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO, MENSAJE_VENTA_ACTUALIZADA);
            return REDIRECT_DETALLE + ventaActualizada.getId();

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para actualizar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        } catch (StockInsuficienteException e) {
            return manejarErrorStock(e, model, ventaDTO);
        } catch (Exception e) {
            return manejarErrorGeneral(e, model, ventaDTO, "Error al actualizar la venta");
        }
    }

    /**
     * Elimina (anula) una venta
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarVenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Venta venta = buscarVentaPorIdSeguro(id);

            // Verificar si la venta se puede eliminar
            if (!ventaEsEliminable(venta)) {
                redirectAttributes.addFlashAttribute(PARAM_MENSAJE_ADVERTENCIA,
                        "No se puede eliminar una venta en estado: " + venta.getEstado());
                return REDIRECT_DETALLE + id;
            }

            ventaServicio.anular(id);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO, MENSAJE_VENTA_ELIMINADA);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para eliminar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar venta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_ERROR, "Error al eliminar la venta: " + e.getMessage());
        }

        return REDIRECT_LISTA;
    }

    /**
     * Duplica una venta existente
     */
    @PostMapping("/duplicar/{id}")
    public String duplicarVenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Venta ventaOriginal = buscarVentaPorIdSeguro(id);
            Venta ventaDuplicada = ventaServicio.duplicarVenta(ventaOriginal);

            redirectAttributes.addFlashAttribute(PARAM_MENSAJE_EXITO,
                    "Venta duplicada exitosamente con ID: " + ventaDuplicada.getId());
            return REDIRECT_DETALLE + ventaDuplicada.getId();

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para duplicar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        } catch (Exception e) {
            logger.error("Error al duplicar venta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(PARAM_ERROR, "Error al duplicar la venta: " + e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    // ==================== MÉTODOS DE PROCESAMIENTO ====================

    private List<Venta> obtenerVentasFiltradas(LocalDate fechaInicio, LocalDate fechaFin,
                                               Long clienteId, Long vendedorId,
                                               String estado, String metodoPago) {
        try {
            // Filtro por rango de fechas
            if (fechaInicio != null && fechaFin != null) {
                LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
                LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);

                return ventaServicio.buscarVentasParaExportar(inicioDateTime, finDateTime, estado, metodoPago, null);
            }

            // Filtro por cliente
            if (clienteId != null && clienteId > 0) {
                List<Venta> ventasCliente = ventaServicio.buscarPorCliente(clienteId);
                return aplicarFiltrosAdicionales(ventasCliente, estado, metodoPago);
            }

            // Filtro por vendedor
            if (vendedorId != null && vendedorId > 0) {
                LocalDateTime inicioRango = LocalDateTime.now().minusYears(1);
                LocalDateTime finRango = LocalDateTime.now();
                List<Venta> ventasVendedor = ventaServicio.buscarPorVendedorYFechas(vendedorId, inicioRango, finRango);
                return aplicarFiltrosAdicionales(ventasVendedor, estado, metodoPago);
            }

            // Sin filtros específicos - obtener todas las ventas recientes
            List<Venta> todasLasVentas = ventaServicio.listarTodas();
            return aplicarFiltrosAdicionales(todasLasVentas, estado, metodoPago);

        } catch (Exception e) {
            logger.error("Error al obtener ventas filtradas: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Venta> aplicarFiltrosAdicionales(List<Venta> ventas, String estado, String metodoPago) {
        return ventas.stream()
                .filter(venta -> estado == null || estado.isEmpty() || estado.equals(venta.getEstado()))
                .filter(venta -> metodoPago == null || metodoPago.isEmpty() || metodoPago.equals(venta.getMetodoPago()))
                .collect(java.util.stream.Collectors.toList());
    }

    private Venta procesarGuardadoVenta(VentaDTO ventaDTO) {
        asignarVendedorSiNoEspecificado(ventaDTO);
        return ventaServicio.guardar(ventaDTO);
    }

    private Venta procesarActualizacionVenta(Long id, VentaDTO ventaDTO) {
        return ventaServicio.actualizar(id, ventaDTO);
    }

    private VentaDTO crearVentaDTOVacia() {
        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setFecha(LocalDateTime.now());
        return ventaDTO;
    }

    private void asignarVendedorSiNoEspecificado(VentaDTO ventaDTO) {
        if (ventaDTO.getVendedorId() == null) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());
                    if (usuarioActual != null) {
                        ventaDTO.setVendedorId(usuarioActual.getId());
                    }
                }
            } catch (Exception e) {
                logger.warn("No se pudo asignar vendedor automáticamente: {}", e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA SEGURA ====================

    private Venta buscarVentaPorIdSeguro(Long id) {
        if (id == null || id <= 0) {
            throw new RecursoNoEncontradoException(ERROR_VENTA_NO_ENCONTRADA + id);
        }

        return ventaServicio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(ERROR_VENTA_NO_ENCONTRADA + id));
    }

    private Cliente buscarClientePorIdSeguro(Long id) {
        if (id == null || id <= 0) {
            throw new RecursoNoEncontradoException(ERROR_CLIENTE_NO_ENCONTRADO + id);
        }

        Cliente cliente = clienteServicio.buscarPorId(id);
        if (cliente == null) {
            throw new RecursoNoEncontradoException(ERROR_CLIENTE_NO_ENCONTRADO + id);
        }
        return cliente;
    }

    private Usuario buscarUsuarioPorIdSeguro(Long id) {
        if (id == null || id <= 0) {
            throw new RecursoNoEncontradoException(ERROR_USUARIO_NO_ENCONTRADO + id);
        }

        Usuario usuario = usuarioServicio.buscarPorId(id);
        if (usuario == null) {
            throw new RecursoNoEncontradoException(ERROR_USUARIO_NO_ENCONTRADO + id);
        }
        return usuario;
    }

    private Producto buscarProductoPorIdSeguro(Long id) {
        if (id == null || id <= 0) {
            throw new RecursoNoEncontradoException(ERROR_PRODUCTO_NO_ENCONTRADO + id);
        }

        Producto producto = productoServicio.buscarPorId(id);
        if (producto == null) {
            throw new RecursoNoEncontradoException(ERROR_PRODUCTO_NO_ENCONTRADO + id);
        }
        return producto;
    }

    // ==================== MÉTODOS DE CARGA DE DATOS ====================

    private void cargarDatosFormulario(Model model, VentaDTO ventaDTO) {
        model.addAttribute(PARAM_VENTA, ventaDTO);

        // Cargar clientes con manejo seguro de errores
        try {
            List<Cliente> clientes = clienteServicio.buscarTodos();
            model.addAttribute(PARAM_CLIENTES, clientes != null ? clientes : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("No se pudieron cargar clientes: {}", e.getMessage());
            model.addAttribute(PARAM_CLIENTES, Collections.emptyList());
        }

        // Cargar productos con manejo seguro de errores
        try {
            List<Producto> productos = productoServicio.listar();
            model.addAttribute(PARAM_PRODUCTOS, productos != null ? productos : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("No se pudieron cargar productos: {}", e.getMessage());
            model.addAttribute(PARAM_PRODUCTOS, Collections.emptyList());
        }

        // Cargar vendedores con manejo seguro de errores
        try {
            List<Usuario> vendedores = usuarioServicio.listarVendedores();
            model.addAttribute(PARAM_VENDEDORES, vendedores != null ? vendedores : Collections.emptyList());
        } catch (Exception e) {
            logger.debug("No se pudieron cargar vendedores: {}", e.getMessage());
            model.addAttribute(PARAM_VENDEDORES, Collections.emptyList());
        }

        // Agregar opciones para selects
        model.addAttribute("estadosVenta", obtenerEstadosVenta());
        model.addAttribute("metodosPago", obtenerMetodosPago());
    }

    private void cargarDatosListado(Model model, List<Venta> ventas,
                                    LocalDate fechaInicio, LocalDate fechaFin,
                                    Long clienteId, Long vendedorId,
                                    String estado, String metodoPago) {

        model.addAttribute(PARAM_VENTAS, ventas != null ? ventas : Collections.emptyList());

        // Cargar datos para filtros
        try {
            List<Cliente> clientes = clienteServicio.buscarTodos();
            model.addAttribute(PARAM_CLIENTES, clientes != null ? clientes : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("No se pudieron cargar clientes para listado: {}", e.getMessage());
            model.addAttribute(PARAM_CLIENTES, Collections.emptyList());
        }

        try {
            List<Usuario> vendedores = usuarioServicio.listarVendedores();
            model.addAttribute(PARAM_VENDEDORES, vendedores != null ? vendedores : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("No se pudieron cargar vendedores para listado: {}", e.getMessage());
            model.addAttribute(PARAM_VENDEDORES, Collections.emptyList());
        }

        // Parámetros de filtros
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("vendedorId", vendedorId);
        model.addAttribute("estado", estado);
        model.addAttribute("metodoPago", metodoPago);

        // Estadísticas básicas
        if (ventas != null && !ventas.isEmpty()) {
            model.addAttribute("totalVentas", ventas.size());
            double totalMonto = ventas.stream()
                    .filter(v -> v.getTotal() != null)
                    .mapToDouble(Venta::getTotal)
                    .sum();
            model.addAttribute("totalMonto", totalMonto);
            model.addAttribute("promedioVenta", ventas.size() > 0 ? totalMonto / ventas.size() : 0.0);
        } else {
            model.addAttribute("totalVentas", 0);
            model.addAttribute("totalMonto", 0.0);
            model.addAttribute("promedioVenta", 0.0);
        }

        // Opciones para filtros
        model.addAttribute("estadosVenta", obtenerEstadosVenta());
        model.addAttribute("metodosPago", obtenerMetodosPago());
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    private boolean ventaEsEditable(Venta venta) {
        return venta != null && !"ANULADA".equals(venta.getEstado()) && !"COMPLETADA".equals(venta.getEstado());
    }

    private boolean ventaEsEliminable(Venta venta) {
        return venta != null && !"ANULADA".equals(venta.getEstado());
    }

    // ==================== MÉTODOS DE MANEJO DE ERRORES ====================

    private String manejarErrorStock(StockInsuficienteException e, Model model, VentaDTO ventaDTO) {
        logger.warn("Stock insuficiente: {}", e.getMessage());
        model.addAttribute(PARAM_ERROR, e.getMessage());
        cargarDatosFormulario(model, ventaDTO);
        return VISTA_NUEVA;
    }

    private String manejarErrorRecursoNoEncontrado(RecursoNoEncontradoException e, Model model, VentaDTO ventaDTO) {
        logger.warn("Recurso no encontrado: {}", e.getMessage());
        model.addAttribute(PARAM_ERROR, e.getMessage());
        cargarDatosFormulario(model, ventaDTO);
        return VISTA_NUEVA;
    }

    private String manejarErrorGeneral(Exception e, Model model, VentaDTO ventaDTO, String mensajeBase) {
        logger.error("{}: {}", mensajeBase, e.getMessage(), e);
        model.addAttribute(PARAM_ERROR, mensajeBase + ": " + e.getMessage());
        cargarDatosFormulario(model, ventaDTO);
        return VISTA_NUEVA;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private List<String> obtenerEstadosVenta() {
        return List.of("PENDIENTE", "COMPLETADA", "ANULADA", "EN_PROCESO");
    }

    private List<String> obtenerMetodosPago() {
        return List.of("EFECTIVO", "TARJETA_CREDITO", "TARJETA_DEBITO", "TRANSFERENCIA", "CHEQUE");
    }
}