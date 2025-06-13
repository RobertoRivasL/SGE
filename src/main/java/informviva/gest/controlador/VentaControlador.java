package informviva.gest.controlador;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.RutasConstantes;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaControlador {

    private static final Logger logger = LoggerFactory.getLogger(VentaControlador.class);

    private static final String VISTA_LISTA = "ventas/lista";
    private static final String VISTA_NUEVA = "ventas/nueva";
    private static final String VISTA_DETALLE = "ventas/detalle";
    private static final String VISTA_EDITAR = "ventas/editar";
    private static final String VISTA_ERROR = "default";


    private static final String PARAM_VENTA_DTO = "ventaDTO";
    private static final String PARAM_CLIENTES = "clientes";
    private static final String PARAM_PRODUCTOS = "productos";
    private static final String PARAM_VENDEDORES = "vendedores";
    private static final String PARAM_VENTAS_RECIENTES = "ventasRecientes";
    private static final String PARAM_FECHA_INICIO = "fechaInicio";
    private static final String PARAM_FECHA_FIN = "fechaFin";
    private static final String PARAM_MENSAJE = "mensaje";
    private static final String PARAM_ERROR = "error";

    private static final String REDIRECT_LISTA = "redirect:/ventas/lista";
    private static final String REDIRECT_DETALLE = "redirect:/ventas/detalle/";

    private static final String VENTA_CREADA = "Venta creada exitosamente con ID: ";
    private static final String VENTA_ACTUALIZADA = "Venta actualizada exitosamente";
    private static final String VENTA_ANULADA = "Venta anulada exitosamente";
    private static final String ERROR_CREAR_VENTA = "Error al crear la venta: ";
    private static final String ERROR_ACTUALIZAR_VENTA = "Error al actualizar la venta: ";
    private static final String ERROR_ANULAR_VENTA = "Error al anular la venta: ";

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

    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping
    public String redirigirALista() {
        final String RUTA_DEPRECADA_MSG = "Acceso a ruta deprecada /ventas, redirigiendo a /ventas/lista";
        logger.info(RUTA_DEPRECADA_MSG);
        return REDIRECT_LISTA;
    }

    @GetMapping("/lista")
    public String mostrarPaginaVentas(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioDeMes = hoy.withDayOfMonth(1);

        LocalDateTime inicioDeMesDT = inicioDeMes.atStartOfDay();
        LocalDateTime hoyDT = hoy.atTime(LocalTime.MAX);

        List<Venta> ventasRecientes = ventaServicio.buscarPorRangoFechas(inicioDeMesDT, hoyDT);
        model.addAttribute(PARAM_VENTAS_RECIENTES, ventasRecientes);
        model.addAttribute(PARAM_FECHA_INICIO, inicioDeMes);
        model.addAttribute(PARAM_FECHA_FIN, hoy);

        return VISTA_LISTA;
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaVenta(Model model) {
        // ✅ Crear DTO con fecha inicializada
        VentaDTO ventaDTO = new VentaDTO();
        LocalDateTime fechaActual = LocalDateTime.now();
        ventaDTO.setFecha(fechaActual);

        // Cargar datos del formulario
        cargarDatosFormulario(model, ventaDTO);

        // ✅ Agregar fecha al modelo para el template
        model.addAttribute("fecha", fechaActual);

        return VISTA_NUEVA; // Usar la constante ya definida
    }

    /**
     * Alternativa usando @ModelAttribute para inicializar la fecha
     */
    @ModelAttribute("fechaActual")
    public LocalDateTime fechaActual() {
        return LocalDateTime.now();
    }

    /**
     * Método para formatear fecha para datetime-local
     */
    @ModelAttribute("fechaFormateada")
    public String fechaFormateada() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    @PostMapping("/nueva")
    public String crearVenta(@Valid @ModelAttribute(PARAM_VENTA_DTO) VentaDTO ventaDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        // ✅ Validar que la fecha no sea nula
        if (ventaDTO.getFecha() == null) {
            ventaDTO.setFecha(LocalDateTime.now());
        }

        if (result.hasErrors()) {
            cargarDatosFormulario(model, ventaDTO);
            model.addAttribute("fecha", ventaDTO.getFecha());
            return VISTA_NUEVA; // Usar la constante ya definida
        }

        try {
            asignarVendedorSiNoEspecificado(ventaDTO);
            Venta venta = ventaServicio.guardar(ventaDTO);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE, VENTA_CREADA + venta.getId());
            return REDIRECT_DETALLE + venta.getId();
        } catch (StockInsuficienteException e) {
            manejarExcepcionStock(result, model, ventaDTO, e);
            // ✅ Re-agregar fecha al modelo
            model.addAttribute("fecha", ventaDTO.getFecha());
            return VISTA_NUEVA;
        } catch (RecursoNoEncontradoException e) {
            manejarExcepcionRecursoNoEncontrado(result, model, ventaDTO, e, "clienteId");
            // ✅ Re-agregar fecha al modelo
            model.addAttribute("fecha", ventaDTO.getFecha());
            return VISTA_NUEVA;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(PARAM_ERROR, ERROR_CREAR_VENTA + e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    @GetMapping("/detalle/{id}")
    public String mostrarDetalleVenta(@PathVariable Long id, Model model) {
        try {
            Venta venta = ventaServicio.buscarPorId(id);
            model.addAttribute("venta", venta);
            return VISTA_DETALLE;
        } catch (RecursoNoEncontradoException e) {
            model.addAttribute(PARAM_ERROR, e.getMessage());
            return VISTA_ERROR;
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarVenta(@PathVariable Long id, Model model) {
        try {
            Venta venta = ventaServicio.buscarPorId(id);
            VentaDTO ventaDTO = ventaServicio.convertirADTO(venta);
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_EDITAR;
        } catch (RecursoNoEncontradoException e) {
            model.addAttribute(PARAM_ERROR, e.getMessage());
            return VISTA_ERROR;
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarVenta(@PathVariable Long id,
                                  @Valid @ModelAttribute(PARAM_VENTA_DTO) VentaDTO ventaDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            cargarDatosFormulario(model, ventaDTO);
            return VISTA_EDITAR;
        }

        try {
            Venta venta = ventaServicio.actualizar(id, ventaDTO);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE, VENTA_ACTUALIZADA);
            return REDIRECT_DETALLE + venta.getId();
        } catch (StockInsuficienteException e) {
            manejarExcepcionStock(result, model, ventaDTO, e);
            return VISTA_EDITAR;
        } catch (RecursoNoEncontradoException e) {
            model.addAttribute(PARAM_ERROR, e.getMessage());
            return VISTA_ERROR;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(PARAM_ERROR, ERROR_ACTUALIZAR_VENTA + e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    @PostMapping("/anular/{id}")
    public String anularVenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ventaServicio.anular(id);
            redirectAttributes.addFlashAttribute(PARAM_MENSAJE, VENTA_ANULADA);
            return REDIRECT_LISTA;
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute(PARAM_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(PARAM_ERROR, ERROR_ANULAR_VENTA + e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    @GetMapping("/filtrar")
    public String filtrarVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        LocalDateTime fechaInicioDT = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDT = fechaFin.atTime(LocalTime.MAX);

        List<Venta> ventasFiltradas = ventaServicio.buscarPorRangoFechas(fechaInicioDT, fechaFinDT);
        model.addAttribute(PARAM_VENTAS_RECIENTES, ventasFiltradas);
        model.addAttribute(PARAM_FECHA_INICIO, fechaInicio);
        model.addAttribute(PARAM_FECHA_FIN, fechaFin);

        return VISTA_LISTA;
    }

    private void cargarDatosFormulario(Model model, VentaDTO ventaDTO) {
        model.addAttribute(PARAM_VENTA_DTO, ventaDTO);
        model.addAttribute(PARAM_CLIENTES, clienteServicio.obtenerTodos());

        List<Producto> productos = productoServicio.listar();
        logger.info("Cargando {} productos para el formulario de venta", productos.size());
        if (productos.isEmpty()) {
            logger.warn("No se encontraron productos para cargar en el formulario");
        } else {
            logger.debug("Primer producto cargado: {}", productos.get(0));
        }
        model.addAttribute(PARAM_PRODUCTOS, productos);

        model.addAttribute(PARAM_VENDEDORES, usuarioServicio.listarVendedores());
    }

    private void asignarVendedorSiNoEspecificado(VentaDTO ventaDTO) {
        if (ventaDTO.getVendedorId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Usuario vendedor = usuarioServicio.buscarPorUsername(username);
            ventaDTO.setVendedorId(vendedor.getId());
        }
    }

    private void manejarExcepcionStock(BindingResult result, Model model, VentaDTO ventaDTO, StockInsuficienteException e) {
        result.rejectValue("detalles", "error.ventaDTO", e.getMessage());
        cargarDatosFormulario(model, ventaDTO);
    }

    private void manejarExcepcionRecursoNoEncontrado(BindingResult result, Model model, VentaDTO ventaDTO,
                                                     RecursoNoEncontradoException e, String campo) {
        result.rejectValue(campo, "error.ventaDTO", e.getMessage());
        cargarDatosFormulario(model, ventaDTO);
    }
}