package informviva.gest.controlador;

import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.MensajesConstantes;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;
    private final VentaServicio ventaServicio;

    public ClienteControlador(ClienteServicio clienteServicio, VentaServicio ventaServicio) {
        this.clienteServicio = clienteServicio;
        this.ventaServicio = ventaServicio;
    }

    /**
     * VISTA PRINCIPAL - Lista de clientes (acceso general)
     * Ruta: /clientes
     * Vista: templates/clientes/lista.html
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public String listarClientes(Model modelo,
                                 @ModelAttribute("mensaje") String mensaje,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        // Paginación
        Pageable pageable = PageRequest.of(page, size);
        Page<Cliente> clientesPage = clienteServicio.obtenerTodosPaginados(pageable);

        modelo.addAttribute("clientes", clientesPage.getContent());
        modelo.addAttribute("currentPage", clientesPage);
        modelo.addAttribute("mensaje", mensaje);

        return "clientes/lista"; // → templates/clientes/lista.html
    }

    /**
     * VISTA ADMINISTRATIVA - Lista de clientes con más funciones
     * Ruta: /clientes/admin
     * Vista: templates/clientes/lista-admin.html
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarClientesAdmin(Model modelo,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "15") int size,
                                      @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Cliente> clientesPage;

        if (search != null && !search.trim().isEmpty()) {
            clientesPage = clienteServicio.buscarPorNombreOEmail(search, pageable);
            modelo.addAttribute("search", search);
        } else {
            clientesPage = clienteServicio.obtenerTodosPaginados(pageable);
        }

        modelo.addAttribute("clientes", clientesPage.getContent());
        modelo.addAttribute("currentPage", clientesPage);

        return "clientes/lista-admin"; // → templates/clientes/lista-admin.html
    }

    /**
     * FORMULARIO NUEVO CLIENTE
     * Ruta: /clientes/nuevo
     * Vista: templates/clientes/formulario.html
     */
    @GetMapping("/nuevo")  // Cambiar de "/formulario" a "/nuevo"
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public String mostrarFormularioNuevo(Model modelo) {
        modelo.addAttribute("cliente", new Cliente());
        modelo.addAttribute("esNuevo", true);
        return "clientes/formulario";
    }

    /**
     * FORMULARIO EDITAR CLIENTE
     * Ruta: /clientes/editar/{id}
     * Vista: templates/clientes/formulario.html
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public String editarCliente(@PathVariable Long id, Model modelo, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Cliente no encontrado");
            return "redirect:/clientes";
        }

        modelo.addAttribute("cliente", cliente);
        modelo.addAttribute("esNuevo", false);

        return "clientes/formulario"; // → templates/clientes/formulario.html
    }

    /**
     * DETALLE DEL CLIENTE
     * Ruta: /clientes/detalle/{id}
     * Vista: templates/clientes/detalle.html
     */
    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public String mostrarDetalleCliente(@PathVariable Long id, Model modelo, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Cliente no encontrado");
            return "redirect:/clientes";
        }

        // Obtener información adicional del cliente
        var ventasCliente = ventaServicio.buscarPorCliente(cliente);
        var totalCompras = ventasCliente.stream()
                .mapToDouble(venta -> venta.getTotal())
                .sum();

        modelo.addAttribute("cliente", cliente);
        modelo.addAttribute("ventasCliente", ventasCliente);
        modelo.addAttribute("totalCompras", totalCompras);
        modelo.addAttribute("numeroCompras", ventasCliente.size());

        return "clientes/detalle"; // → templates/clientes/detalle.html
    }

    @GetMapping("/admin/detalle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarDetalleClienteAdmin(@PathVariable Long id, Model modelo, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteServicio.buscarPorId(id);

        if (cliente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Cliente no encontrado");
            return "redirect:/clientes";
        }

        // Obtener información adicional del cliente
        var ventasCliente = ventaServicio.buscarPorCliente(cliente);
        var totalCompras = ventasCliente.stream()
                .mapToDouble(venta -> venta.getTotal())
                .sum();

        modelo.addAttribute("cliente", cliente);
        modelo.addAttribute("ventasCliente", ventasCliente);
        modelo.addAttribute("totalCompras", totalCompras);
        modelo.addAttribute("numeroCompras", ventasCliente.size());

        return "clientes/detalle-admin"; // Ruta corregida
    }
    /**
     * GUARDAR CLIENTE (POST)
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public String guardarCliente(@Valid @ModelAttribute("cliente") Cliente cliente,
                                 BindingResult resultado,
                                 RedirectAttributes redirectAttributes,
                                 Model modelo) {

        // Validar RUT
        if (!clienteServicio.rutEsValido(cliente.getRut())) {
            resultado.rejectValue("rut", "error.cliente", MensajesConstantes.ERROR_RUT_INVALIDO);
        }

        if (resultado.hasErrors()) {
            modelo.addAttribute("esNuevo", cliente.getId() == null);
            return "clientes/formulario";
        }

        try {
            clienteServicio.guardar(cliente);
            String mensaje = cliente.getId() == null ?
                    "Cliente creado exitosamente" :
                    "Cliente actualizado exitosamente";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);

            return "redirect:/clientes/detalle/" + cliente.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar cliente: " + e.getMessage());
            return "redirect:/clientes";
        }
    }

    /**
     * ELIMINAR CLIENTE
     */
    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        // Verificar si tiene ventas asociadas
        if (ventaServicio.existenVentasPorCliente(id)) {
            redirectAttributes.addFlashAttribute("error", MensajesConstantes.ERROR_CLIENTE_CON_VENTAS);
            return "redirect:/clientes";
        }

        try {
            clienteServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", MensajesConstantes.EXITO_CLIENTE_ELIMINADO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }
}