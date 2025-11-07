package informviva.gest.controlador;


import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.MensajesConstantes;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Controlador para las vistas de gestión de clientes
 * Diferencia entre acceso para administradores/gerentes vs personal de ventas
 *
 * Siguiendo el patrón arquitectónico del ProductoVistaControlador
 *
 * @author Roberto
 * @version 2.1
 * @since FASE 2 - Estandarización Arquitectónica
 */
@Controller
@RequestMapping("/clientes")
public class ClienteVistaControlador {

    private final ClienteServicio clienteServicio;
    private final VentaServicio ventaServicio;

    public ClienteVistaControlador(ClienteServicio clienteServicio, VentaServicio ventaServicio) {
        this.clienteServicio = clienteServicio;
        this.ventaServicio = ventaServicio;
    }

    /**
     * Vista de clientes para PERSONAL DE VENTAS - Solo lectura y consulta
     * Acceso: VENTAS, ADMIN, GERENTE
     * Ruta: /clientes/vendedor
     * Vista: templates/clientes/lista-vendedor.html
     */
    @GetMapping("/vendedor")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'GERENTE')")
    public String listarClientesVendedor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos,
            Model model) {

        // Configurar paginación - Ordenar por último contacto
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaUltimaCompra").descending());

        // Aplicar filtros
        Page<Cliente> clientesPage = aplicarFiltros(search, ciudad, soloActivos, pageable);

        // Estadísticas básicas para vendedores
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("search", search);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("soloActivos", soloActivos);
        model.addAttribute("totalClientes", clienteServicio.contarTodos());
        model.addAttribute("ciudades", clienteServicio.listarCiudades());
        model.addAttribute("clientesActivos", clienteServicio.contarActivos());

        return "clientes/lista-vendedor";
    }

    /**
     * Vista de administración de clientes para ADMIN/GERENTE - CRUD completo
     * Acceso: ADMIN, GERENTE
     * Ruta: /clientes/admin
     * Vista: templates/clientes/lista-admin.html
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String listarClientesAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos,
            Model model) {

        // Misma lógica de filtros y paginación - Ordenar por fecha de registro
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        Page<Cliente> clientesPage = aplicarFiltros(search, ciudad, soloActivos, pageable);

        // Estadísticas completas para administradores
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("search", search);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("soloActivos", soloActivos);
        model.addAttribute("totalClientes", clienteServicio.contarTodos());
        model.addAttribute("ciudades", clienteServicio.listarCiudades());
        model.addAttribute("clientesActivos", clienteServicio.contarActivos());
        model.addAttribute("clientesInactivos", clienteServicio.contarInactivos());
        model.addAttribute("clientesNuevosHoy", clienteServicio.contarNuevosHoy());

        return "clientes/lista-admin";
    }

    /**
     * Formulario para crear nuevo cliente
     * Acceso: ADMIN, GERENTE, VENTAS
     * Ruta: /clientes/nuevo
     * Vista: templates/clientes/formulario.html
     */
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String nuevoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("esNuevo", true);
        model.addAttribute("titulo", "Nuevo Cliente");
        return "clientes/formulario";
    }

    /**
     * Formulario para editar cliente existente
     * Acceso: ADMIN, GERENTE, VENTAS
     * Ruta: /clientes/editar/{id}
     * Vista: templates/clientes/formulario.html
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String editarCliente(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_NO_ENCONTRADO);
            return "redirect:/clientes/admin";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("esNuevo", false);
        model.addAttribute("titulo", "Editar Cliente - " + cliente.getNombreCompleto());
        return "clientes/formulario";
    }

    /**
     * Vista detallada del cliente - Solo lectura
     * Acceso: ADMIN, GERENTE, VENTAS
     * Ruta: /clientes/detalle/{id}
     * Vista: templates/clientes/detalle.html
     */
    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String detalleCliente(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_NO_ENCONTRADO);
            return "redirect:/clientes";
        }

        // Obtener información adicional del cliente
        var ventasCliente = ventaServicio.buscarPorCliente(cliente);
        var totalCompras = ventasCliente.stream()
                .mapToDouble(venta -> venta.getTotal())
                .sum();

        // Calcular estadísticas adicionales
        var promedioCompra = ventasCliente.isEmpty() ? 0.0 : totalCompras / ventasCliente.size();
        var ultimaCompraFecha = ventasCliente.stream()
                .map(venta -> venta.getFecha())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        model.addAttribute("cliente", cliente);
        model.addAttribute("ventasCliente", ventasCliente);
        model.addAttribute("totalCompras", totalCompras);
        model.addAttribute("numeroCompras", ventasCliente.size());
        model.addAttribute("promedioCompra", promedioCompra);
        model.addAttribute("ultimaCompraFecha", ultimaCompraFecha);

        return "clientes/detalle";
    }

    /**
     * Procesar formulario de cliente (crear/actualizar)
     * Acceso: ADMIN, GERENTE, VENTAS
     * Ruta: POST /clientes/guardar
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String guardarCliente(@Valid @ModelAttribute("cliente") Cliente cliente,
                                 BindingResult resultado,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 Authentication authentication) {

        // Validar RUT
        if (!clienteServicio.esRutValido(cliente.getRut())) {
            resultado.rejectValue("rut", "error.cliente", MensajesConstantes.ERROR_RUT_INVALIDO);
        }

        // Validar duplicados de email si es nuevo o cambió
        if (clienteServicio.existeEmailOtroCliente(cliente.getEmail(), cliente.getId())) {
            resultado.rejectValue("email", "error.cliente", MensajesConstantes.ERROR_EMAIL_DUPLICADO);
        }

        if (resultado.hasErrors()) {
            model.addAttribute("esNuevo", cliente.getId() == null);
            model.addAttribute("titulo", cliente.getId() == null ? "Nuevo Cliente" : "Editar Cliente");
            return "clientes/formulario";
        }

        try {
            // Establecer datos de auditoría
            if (cliente.getId() == null) {
                cliente.setFechaRegistro(LocalDateTime.now());
                cliente.setUsuarioCreacion(authentication.getName());
            } else {
                cliente.setFechaModificacion(LocalDateTime.now());
                cliente.setUsuarioModificacion(authentication.getName());
            }

            Cliente clienteGuardado = clienteServicio.guardar(cliente);

            String mensaje = cliente.getId() == null ?
                    MensajesConstantes.EXITO_CLIENTE_CREADO :
                    MensajesConstantes.EXITO_CLIENTE_ACTUALIZADO;

            redirectAttributes.addFlashAttribute("mensaje", mensaje);

            return "redirect:/clientes/detalle/" + clienteGuardado.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar cliente: " + e.getMessage());
            return "redirect:/clientes/admin";
        }
    }

    /**
     * Confirmar eliminación de cliente
     * Acceso: ADMIN solamente
     * Ruta: /clientes/eliminar/{id}
     */
    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String confirmarEliminacion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_NO_ENCONTRADO);
            return "redirect:/clientes/admin";
        }

        // Verificar si tiene ventas asociadas
        if (ventaServicio.existenVentasPorCliente(id)) {
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_CON_VENTAS);
            return "redirect:/clientes/admin";
        }

        model.addAttribute("cliente", cliente);
        return "clientes/confirmar-eliminacion";
    }

    /**
     * Ejecutar eliminación de cliente
     * Acceso: ADMIN solamente
     * Ruta: POST /clientes/eliminar/{id}
     */
    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", MensajesConstantes.EXITO_CLIENTE_ELIMINADO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }

        return "redirect:/clientes/admin";
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Aplica filtros de búsqueda a la consulta de clientes
     */
    private Page<Cliente> aplicarFiltros(String search, String ciudad, Boolean soloActivos, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            if (ciudad != null && !ciudad.trim().isEmpty()) {
                if (soloActivos != null && soloActivos) {
                    return clienteServicio.buscarPorTerminoYCiudadYActivos(search.trim(), ciudad.trim(), pageable);
                } else {
                    return clienteServicio.buscarPorTerminoYCiudad(search.trim(), ciudad.trim(), pageable);
                }
            } else {
                if (soloActivos != null && soloActivos) {
                    return clienteServicio.buscarPorTerminoYActivos(search.trim(), pageable);
                } else {
                    return clienteServicio.buscarPorTermino(search.trim(), pageable);
                }
            }
        } else if (ciudad != null && !ciudad.trim().isEmpty()) {
            if (soloActivos != null && soloActivos) {
                return clienteServicio.buscarPorCiudadYActivos(ciudad.trim(), pageable);
            } else {
                return clienteServicio.buscarPorCiudad(ciudad.trim(), pageable);
            }
        } else if (soloActivos != null && soloActivos) {
            return clienteServicio.buscarActivos(pageable);
        } else {
            return clienteServicio.buscarTodos(pageable);
        }
    }
}