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
import informviva.gest.util.RutasConstantes;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el manejo de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/ventas")
public class VentaControlador {

    private static final Logger logger = LoggerFactory.getLogger(VentaControlador.class);

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Muestra la lista de ventas con paginación y filtros
     */
    @GetMapping({"/lista", ""})
    public String listarVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long clienteId,
            Model model) {

        try {
            List<Venta> ventas;

            if (fechaInicio != null && fechaFin != null) {
                ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
            } else if (clienteId != null && clienteId > 0) {
                ventas = ventaServicio.buscarPorCliente(clienteId);
            } else {
                ventas = ventaServicio.listarTodas();
            }

            model.addAttribute("ventas", ventas);
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("clienteId", clienteId);

            return RutasConstantes.VISTA_LISTA_VENTAS;

        } catch (Exception e) {
            logger.error("Error al listar ventas: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar las ventas");
            return RutasConstantes.VISTA_LISTA_VENTAS;
        }
    }

    /**
     * Muestra el formulario para crear una nueva venta
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaVenta(Model model) {
        try {
            VentaDTO ventaDTO = new VentaDTO();
            ventaDTO.setFechaVenta(LocalDateTime.now());

            model.addAttribute("venta", ventaDTO);
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());

            return RutasConstantes.VISTA_NUEVA_VENTA;

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nueva venta: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el formulario");
            return "redirect:/ventas/lista";
        }
    }

    /**
     * Procesa la creación de una nueva venta
     */
    @PostMapping("/guardar")
    public String guardarVenta(
            @Valid @ModelAttribute("venta") VentaDTO ventaDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_NUEVA_VENTA;
        }

        try {
            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            // Convertir DTO a entidad
            Venta venta = convertirDTOAEntidad(ventaDTO);
            venta.setUsuario(usuarioActual);

            // Guardar venta
            Venta ventaGuardada = ventaServicio.guardar(venta);

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Venta guardada exitosamente con ID: " + ventaGuardada.getId());

            return "redirect:/ventas/detalle/" + ventaGuardada.getId();

        } catch (StockInsuficienteException e) {
            logger.warn("Stock insuficiente al guardar venta: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_NUEVA_VENTA;

        } catch (Exception e) {
            logger.error("Error al guardar venta: {}", e.getMessage());
            model.addAttribute("error", "Error al guardar la venta: " + e.getMessage());
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_NUEVA_VENTA;
        }
    }

    /**
     * Muestra los detalles de una venta específica
     */
    @GetMapping("/detalle/{id}")
    public String mostrarDetalleVenta(@PathVariable Long id, Model model) {
        try {
            Optional<Venta> ventaOpt = ventaServicio.buscarPorId(id);

            if (ventaOpt.isEmpty()) {
                throw new RecursoNoEncontradoException("Venta no encontrada con ID: " + id);
            }

            model.addAttribute("venta", ventaOpt.get());
            return RutasConstantes.VISTA_DETALLE_VENTA;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/ventas/lista";

        } catch (Exception e) {
            logger.error("Error al mostrar detalle de venta: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar los detalles de la venta");
            return "redirect:/ventas/lista";
        }
    }

    /**
     * Muestra el formulario para editar una venta
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarVenta(@PathVariable Long id, Model model) {
        try {
            Optional<Venta> ventaOpt = ventaServicio.buscarPorId(id);

            if (ventaOpt.isEmpty()) {
                throw new RecursoNoEncontradoException("Venta no encontrada con ID: " + id);
            }

            VentaDTO ventaDTO = convertirEntidadADTO(ventaOpt.get());

            model.addAttribute("venta", ventaDTO);
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());

            return RutasConstantes.VISTA_EDITAR_VENTA;

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para editar: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/ventas/lista";

        } catch (Exception e) {
            logger.error("Error al mostrar formulario de edición: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el formulario de edición");
            return "redirect:/ventas/lista";
        }
    }

    /**
     * Procesa la actualización de una venta
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarVenta(
            @PathVariable Long id,
            @Valid @ModelAttribute("venta") VentaDTO ventaDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_EDITAR_VENTA;
        }

        try {
            Venta ventaExistente = ventaServicio.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + id));

            // Actualizar campos
            ventaExistente.setCliente(clienteServicio.buscarPorId(ventaDTO.getClienteId()).get());
            ventaExistente.setProducto(productoServicio.buscarPorId(ventaDTO.getProductoId()).get());
            ventaExistente.setCantidad(ventaDTO.getCantidad());
            ventaExistente.setPrecioUnitario(ventaDTO.getPrecioUnitario());
            ventaExistente.setTotal(ventaDTO.getTotal());

            Venta ventaActualizada = ventaServicio.actualizar(ventaExistente);

            redirectAttributes.addFlashAttribute("mensajeExito", "Venta actualizada exitosamente");
            return "redirect:/ventas/detalle/" + ventaActualizada.getId();

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para actualizar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/lista";

        } catch (StockInsuficienteException e) {
            logger.warn("Stock insuficiente al actualizar venta: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_EDITAR_VENTA;

        } catch (Exception e) {
            logger.error("Error al actualizar venta: {}", e.getMessage());
            model.addAttribute("error", "Error al actualizar la venta: " + e.getMessage());
            model.addAttribute("clientes", clienteServicio.obtenerTodos());
            model.addAttribute("productos", productoServicio.listarActivos());
            return RutasConstantes.VISTA_EDITAR_VENTA;
        }
    }

    /**
     * Elimina una venta
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarVenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ventaServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Venta eliminada exitosamente");

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Venta no encontrada para eliminar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            logger.error("Error al eliminar venta: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la venta");
        }

        return "redirect:/ventas/lista";
    }

    // Métodos de utilidad

    private Venta convertirDTOAEntidad(VentaDTO dto) {
        Venta venta = new Venta();

        if (dto.getId() != null) {
            venta.setId(dto.getId());
        }

        Cliente cliente = clienteServicio.buscarPorId(dto.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        venta.setCliente(cliente);

        Producto producto = productoServicio.buscarPorId(dto.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        venta.setProducto(producto);

        venta.setCantidad(dto.getCantidad());
        venta.setPrecioUnitario(dto.getPrecioUnitario());
        venta.setTotal(dto.getTotal());
        venta.setFechaVenta(dto.getFechaVenta());

        return venta;
    }

    private VentaDTO convertirEntidadADTO(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setId(venta.getId());
        dto.setClienteId(venta.getCliente().getId());
        dto.setProductoId(venta.getProducto().getId());
        dto.setCantidad(venta.getCantidad());
        dto.setPrecioUnitario(venta.getPrecioUnitario());
        dto.setTotal(venta.getTotal());
        dto.setFechaVenta(venta.getFechaVenta());
        return dto;
    }
}