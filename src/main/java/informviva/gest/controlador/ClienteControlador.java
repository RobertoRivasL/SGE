package informviva.gest.controlador;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.MensajesConstantes;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador principal para operaciones básicas con clientes
 * Maneja CRUD básico y operaciones comunes
 *
 * @author Roberto Rivas
 * @version 3.1
 */
@Controller
@RequestMapping("/clientes")
public class ClienteControlador {

    private static final Logger logger = LoggerFactory.getLogger(ClienteControlador.class);

    private final ClienteServicio clienteServicio;
    private final VentaServicio ventaServicio;

    public ClienteControlador(ClienteServicio clienteServicio, VentaServicio ventaServicio) {
        this.clienteServicio = clienteServicio;
        this.ventaServicio = ventaServicio;
    }

    // ================================
    // VISTAS PRINCIPALES
    // ================================

    /**
     * Lista principal de clientes (acceso general)
     * URL: /clientes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String listarClientes(Model modelo,
                                 @ModelAttribute("mensaje") String mensaje,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String search) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Cliente> clientesPage;

            if (search != null && !search.trim().isEmpty()) {
                clientesPage = clienteServicio.buscarPorNombreOEmail(search.trim(), pageable);
                modelo.addAttribute("search", search);
            } else {
                clientesPage = clienteServicio.obtenerTodosPaginados(pageable);
            }

            modelo.addAttribute("clientes", clientesPage.getContent());
            modelo.addAttribute("currentPage", clientesPage);
            modelo.addAttribute("mensaje", mensaje);

            logger.debug("Listando clientes: página {}, {} resultados", page, clientesPage.getContent().size());

            return "clientes/lista";

        } catch (Exception e) {
            logger.error("Error listando clientes: {}", e.getMessage(), e);
            modelo.addAttribute("error", "Error al cargar la lista de clientes");
            return "error/500";
        }
    }

    /**
     * Formulario para nuevo cliente
     * URL: /clientes/nuevo
     */
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String mostrarFormularioNuevo(Model modelo) {
        modelo.addAttribute("cliente", new Cliente());
        modelo.addAttribute("esNuevo", true);
        modelo.addAttribute("categorias", obtenerCategoriasDisponibles());

        logger.debug("Mostrando formulario para nuevo cliente");
        return "clientes/formulario";
    }

    /**
     * Formulario para editar cliente
     * URL: /clientes/editar/{id}
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String editarCliente(@PathVariable Long id, Model modelo, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
                return "redirect:/clientes";
            }

            modelo.addAttribute("cliente", cliente);
            modelo.addAttribute("esNuevo", false);
            modelo.addAttribute("categorias", obtenerCategoriasDisponibles());

            logger.debug("Mostrando formulario de edición para cliente ID: {}", id);
            return "clientes/formulario";

        } catch (Exception e) {
            logger.error("Error cargando cliente para edición ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el cliente");
            return "redirect:/clientes";
        }
    }

    /**
     * Detalle del cliente
     * URL: /clientes/detalle/{id}
     */
    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String mostrarDetalleCliente(@PathVariable Long id, Model modelo, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
                return "redirect:/clientes";
            }

            // Obtener información adicional del cliente
            var ventasCliente = ventaServicio.buscarPorCliente(cliente);
            var totalCompras = ventasCliente.stream()
                    .mapToDouble(venta -> venta.getTotal() != null ? venta.getTotal() : 0.0)
                    .sum();

            modelo.addAttribute("cliente", cliente);
            modelo.addAttribute("ventasCliente", ventasCliente);
            modelo.addAttribute("totalCompras", totalCompras);
            modelo.addAttribute("numeroCompras", ventasCliente.size());

            // Calcular estadísticas adicionales
            if (!ventasCliente.isEmpty()) {
                var promedioCompra = totalCompras / ventasCliente.size();
                var ultimaCompra = ventasCliente.stream()
                        .map(venta -> venta.getFechaAsLocalDate())
                        .max(LocalDate::compareTo)
                        .orElse(null);

                modelo.addAttribute("promedioCompra", promedioCompra);
                modelo.addAttribute("ultimaCompra", ultimaCompra);
            }

            logger.debug("Mostrando detalle de cliente ID: {}", id);
            return "clientes/detalle";

        } catch (Exception e) {
            logger.error("Error cargando detalle cliente ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el detalle del cliente");
            return "redirect:/clientes";
        }
    }

    // ================================
    // OPERACIONES CRUD
    // ================================

    /**
     * Guardar cliente (nuevo o actualización)
     * URL: POST /clientes/guardar
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String guardarCliente(@Valid @ModelAttribute("cliente") Cliente cliente,
                                 BindingResult resultado,
                                 RedirectAttributes redirectAttributes,
                                 Model modelo) {

        try {
            // Validar RUT
            if (!clienteServicio.rutEsValido(cliente.getRut())) {
                resultado.rejectValue("rut", "error.cliente", MensajesConstantes.ERROR_RUT_INVALIDO);
            }

            // Validar unicidad de email
            if (cliente.getId() == null) {
                if (clienteServicio.existeClienteConEmail(cliente.getEmail())) {
                    resultado.rejectValue("email", "error.cliente", "Ya existe un cliente con este email");
                }
                if (clienteServicio.existeClienteConRut(cliente.getRut())) {
                    resultado.rejectValue("rut", "error.cliente", "Ya existe un cliente con este RUT");
                }
            } else {
                if (clienteServicio.existeClienteConEmailExcluyendo(cliente.getEmail(), cliente.getId())) {
                    resultado.rejectValue("email", "error.cliente", "Ya existe otro cliente con este email");
                }
                if (clienteServicio.existeClienteConRutExcluyendo(cliente.getRut(), cliente.getId())) {
                    resultado.rejectValue("rut", "error.cliente", "Ya existe otro cliente con este RUT");
                }
            }

            if (resultado.hasErrors()) {
                modelo.addAttribute("esNuevo", cliente.getId() == null);
                modelo.addAttribute("categorias", obtenerCategoriasDisponibles());
                return "clientes/formulario";
            }

            Cliente clienteGuardado = clienteServicio.guardar(cliente);
            String mensaje = cliente.getId() == null ?
                    "Cliente creado exitosamente" :
                    "Cliente actualizado exitosamente";

            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            logger.info("Cliente guardado exitosamente: ID {}, Email {}",
                    clienteGuardado.getId(), clienteGuardado.getEmail());

            return "redirect:/clientes/detalle/" + clienteGuardado.getId();

        } catch (Exception e) {
            logger.error("Error guardando cliente: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar cliente: " + e.getMessage());
            return "redirect:/clientes";
        }
    }

    /**
     * Eliminar cliente
     * URL: GET /clientes/eliminar/{id}
     */
    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado exitosamente");
            logger.info("Cliente eliminado exitosamente: ID {}", id);

        } catch (IllegalStateException e) {
            logger.warn("Intento de eliminar cliente con ventas asociadas: ID {}", id);
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_CON_VENTAS);
        } catch (Exception e) {
            logger.error("Error eliminando cliente ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }

    // ================================
    // API REST ENDPOINTS
    // ================================

    /**
     * API: Obtener cliente por ID
     * URL: GET /clientes/api/{id}
     */
    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    @ResponseBody
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            logger.error("Error en API obtenerClientePorId ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Búsqueda de clientes
     * URL: GET /clientes/api/buscar?q=termino
     */
    @GetMapping("/api/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    @ResponseBody
    public ResponseEntity<List<Cliente>> buscarClientes(@RequestParam("q") String termino) {
        try {
            List<Cliente> clientes = clienteServicio.buscarPorTermino(termino);
            return ResponseEntity.ok(clientes);

        } catch (Exception e) {
            logger.error("Error en API buscarClientes con término '{}': {}", termino, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Validar RUT
     * URL: GET /clientes/api/validar-rut?rut=12345678-9
     */
    @GetMapping("/api/validar-rut")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarRut(@RequestParam String rut) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            boolean esValido = clienteServicio.rutEsValido(rut);
            boolean existe = esValido && clienteServicio.existeClienteConRut(rut);

            respuesta.put("valido", esValido);
            respuesta.put("existe", existe);

            if (existe) {
                Cliente cliente = clienteServicio.buscarPorRut(rut);
                respuesta.put("cliente", Map.of(
                        "id", cliente.getId(),
                        "nombre", cliente.getNombreCompleto(),
                        "email", cliente.getEmail()
                ));
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error validando RUT '{}': {}", rut, e.getMessage(), e);
            respuesta.put("error", "Error al validar RUT");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    /**
     * API: Validar email único
     * URL: GET /clientes/api/validar-email?email=test@example.com&excludeId=123
     */
    @GetMapping("/api/validar-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarEmail(@RequestParam String email,
                                                            @RequestParam(required = false) Long excludeId) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            boolean existe;
            if (excludeId != null) {
                existe = clienteServicio.existeClienteConEmailExcluyendo(email, excludeId);
            } else {
                existe = clienteServicio.existeClienteConEmail(email);
            }

            respuesta.put("disponible", !existe);
            respuesta.put("existe", existe);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error validando email '{}': {}", email, e.getMessage(), e);
            respuesta.put("error", "Error al validar email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    /**
     * API: Estadísticas de cliente
     * URL: GET /clientes/api/{id}/estadisticas
     */
    @GetMapping("/api/{id}/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCliente(@PathVariable Long id) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }

            var ventas = ventaServicio.buscarPorCliente(cliente);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalCompras", ventas.size());
            estadisticas.put("montoTotal", ventas.stream()
                    .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                    .sum());
            estadisticas.put("promedioCompra", ventas.isEmpty() ? 0.0 :
                    ventas.stream().mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0).average().orElse(0.0));
            estadisticas.put("ultimaCompra", ventas.stream()
                    .map(v -> v.getFechaAsLocalDate())
                    .max(LocalDate::compareTo)
                    .orElse(null));

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas cliente ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ================================

    private List<String> obtenerCategoriasDisponibles() {
        // Por ahora categorías estáticas, se puede hacer dinámico después
        return List.of("VIP", "PREMIUM", "FRECUENTE", "REGULAR", "NUEVO", "INACTIVO");
    }
}