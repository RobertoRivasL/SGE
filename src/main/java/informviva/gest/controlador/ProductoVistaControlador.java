package informviva.gest.controlador;


import informviva.gest.dto.ProductoDTO;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
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
 * Controlador para las vistas de gestión de productos
 * Diferencia entre acceso para administradores/gerentes vs vendedores
 */
@Controller
@RequestMapping("/productos")
public class ProductoVistaControlador {

    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;

    public ProductoVistaControlador(ProductoServicio productoServicio, VentaServicio ventaServicio) {
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
    }

    /**
     * Vista de productos para VENDEDORES - Solo lectura (consulta de inventario)
     * Acceso: VENTAS
     */
    @GetMapping("/productos/vendedor")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'PRODUCTOS', 'GERENTE')")
    public String listarProductosVendedor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean soloConStock,
            Model model) {

        // Configurar paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());

        // Aplicar filtros
        Page<ProductoDTO> productosPage = aplicarFiltros(search, categoria, soloConStock, pageable);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("search", search);
        model.addAttribute("categoria", categoria);
        model.addAttribute("soloConStock", soloConStock);
        model.addAttribute("totalProductos", productoServicio.contar());
        model.addAttribute("categorias", productoServicio.listarCategorias());
        model.addAttribute("productosConBajoStock", productoServicio.contarConBajoStock(5));

        return "productos-vendedor";
    }

    /**
     * Vista de administración de productos para ADMIN/PRODUCTOS/GERENTE - CRUD completo
     * Acceso: ADMIN, PRODUCTOS, GERENTE
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String listarProductosAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean soloConStock,
            Model model) {

        // Misma lógica de filtros y paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<ProductoDTO> productosPage = aplicarFiltros(search, categoria, soloConStock, pageable);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("search", search);
        model.addAttribute("categoria", categoria);
        model.addAttribute("soloConStock", soloConStock);
        model.addAttribute("totalProductos", productoServicio.contar());
        model.addAttribute("categorias", productoServicio.listarCategorias());
        model.addAttribute("productosConBajoStock", productoServicio.contarConBajoStock(5));

        return "productos/lista-admin";
    }

    /**
     * Formulario para nuevo producto - Solo ADMIN/PRODUCTOS/GERENTE
     */
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("esNuevo", true);
        model.addAttribute("categorias", productoServicio.listarCategorias());
        return "productos/formulario";
    }

    /**
     * Guardar producto - Solo ADMIN/PRODUCTOS/GERENTE
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String guardarProducto(
            @Valid @ModelAttribute("producto") ProductoDTO producto,
            BindingResult resultado,
            RedirectAttributes redirectAttributes,
            Model model) {

        // TODO: Validar código duplicado - falta implementar existePorCodigo en ProductoServicio
        // if (productoServicio.existePorCodigo(producto.getCodigo(), producto.getId())) {
        //     resultado.rejectValue("codigo", "error.producto", "Ya existe un producto con este código");
        // }

        if (resultado.hasErrors()) {
            model.addAttribute("esNuevo", producto.getId() == null);
            model.addAttribute("categorias", productoServicio.listarCategorias());
            return "productos/formulario";
        }

        try {
            // Las fechas de auditoría deben ser manejadas por el servicio o JPA auditing
            ProductoDTO productoGuardado;
            if (producto.getId() == null) {
                productoGuardado = productoServicio.guardar(producto);
            } else {
                productoGuardado = productoServicio.actualizar(producto.getId(), producto);
            }

            redirectAttributes.addFlashAttribute("mensajeExito",
                    producto.getId() == null ? "Producto creado exitosamente" : "Producto actualizado exitosamente");
            return "redirect:/productos/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar producto: " + e.getMessage());
            return "redirect:/productos/admin";
        }
    }

    /**
     * Editar producto - Solo ADMIN/PRODUCTOS/GERENTE
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String editarProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ProductoDTO producto = productoServicio.buscarPorId(id);
        if (producto == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado");
            return "redirect:/productos/admin";
        }

        model.addAttribute("producto", producto);
        model.addAttribute("esNuevo", false);
        model.addAttribute("categorias", productoServicio.listarCategorias());
        return "productos/formulario";
    }

    /**
     * Cambiar estado activo/inactivo - Solo ADMIN/PRODUCTOS
     */
    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS')")
    public String cambiarEstadoProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado");
                return "redirect:/productos/admin";
            }

            // Usar métodos del servicio para activar/desactivar
            if (producto.getActivo()) {
                productoServicio.desactivar(id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto desactivado exitosamente");
            } else {
                productoServicio.activar(id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto activado exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cambiar estado: " + e.getMessage());
        }

        return "redirect:/productos/admin";
    }

    /**
     * Eliminar producto - Solo ADMIN
     */
    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si tiene ventas asociadas
            if (ventaServicio.existenVentasPorProducto(id)) {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "No se puede eliminar el producto porque tiene ventas registradas. Puede desactivarlo en su lugar.");
                return "redirect:/productos/admin";
            }

            productoServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar producto: " + e.getMessage());
        }

        return "redirect:/productos/admin";
    }

    /**
     * Ver detalle de producto
     */
    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'PRODUCTOS', 'GERENTE')")
    public String verDetalleProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ProductoDTO producto = productoServicio.buscarPorId(id);
        if (producto == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado");
            return "redirect:/productos";
        }

        // Obtener estadísticas del producto
        Long totalVendido = ventaServicio.contarUnidadesVendidasPorProducto(id);
        var ingresosTotales = ventaServicio.calcularIngresosPorProducto(id);

        model.addAttribute("producto", producto);
        model.addAttribute("totalVendido", totalVendido);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("ventasRecientes", ventaServicio.buscarVentasRecientesPorProducto(id, 5));

        return "productos/detalle";
    }

    /**
     * Vista de productos con bajo stock
     */
    @GetMapping("/bajo-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String productosConBajoStock(
            @RequestParam(defaultValue = "5") int umbral,
            Model model) {

        model.addAttribute("productos", productoServicio.listarConBajoStock(umbral));
        model.addAttribute("umbral", umbral);
        return "productos/bajo-stock";
    }

    /**
     * Actualización rápida de stock
     */
    @PostMapping("/actualizar-stock/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer nuevoStock,
            RedirectAttributes redirectAttributes) {

        try {
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado");
                return "redirect:/productos/admin";
            }

            // Calcular diferencia y actualizar stock usando el método del servicio
            int diferencia = nuevoStock - producto.getStock();
            productoServicio.actualizarStock(id, diferencia);

            redirectAttributes.addFlashAttribute("mensajeExito", "Stock actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar stock: " + e.getMessage());
        }

        return "redirect:/productos/admin";
    }

    /**
     * Método auxiliar para aplicar filtros
     */
    private Page<ProductoDTO> aplicarFiltros(String search, String categoria, Boolean soloConStock, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return productoServicio.buscarPorNombreOCodigoPaginado(search, pageable);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            return productoServicio.buscarPorCategoriaPaginado(categoria, pageable);
        } else if (soloConStock != null && soloConStock) {
            return productoServicio.listarConStockPaginado(pageable);
        } else {
            return productoServicio.listarPaginados(pageable);
        }
    }

    // ProductoVistaControlador.java - AGREGAR
    @GetMapping
    public String redirigirAVistaProductos(Authentication auth) {
        // Redirigir según el rol del usuario
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_PRODUCTOS") ||
                        a.getAuthority().equals("ROLE_GERENTE"))) {
            return "redirect:/productos/admin";
        } else {
            return "redirect:/productos/productos/vendedor";
        }
    }
//    @GetMapping
//    public String listarProductos(Model model) {
//        // Redirigir a la vista apropiada según el rol
//        return "redirect:/productos/admin";
//    }
}
