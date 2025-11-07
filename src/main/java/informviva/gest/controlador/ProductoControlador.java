package informviva.gest.controlador;

import informviva.gest.dto.ProductoDTO;
import informviva.gest.service.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoControlador {

    @Autowired
    private ProductoServicio productoService;

    // TODO: CategoriaServicio necesita ser refactorizado a DTOs
    // @Autowired
    // private CategoriaServicio categoriaService;

    // Lista básica de productos (usuario normal)
    @GetMapping("/lista")
    public String listaProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long categoriaId,
            Model model) {

        Page<ProductoDTO> productos;

        if (buscar != null && !buscar.trim().isEmpty()) {
            productos = productoService.buscarProductos(buscar.trim(), PageRequest.of(page, size));
        } else if (categoriaId != null) {
            productos = productoService.findByCategoriaId(categoriaId, PageRequest.of(page, size));
        } else {
            productos = productoService.findAllActivos(PageRequest.of(page, size));
        }

        // TODO: Categorias - necesita CategoriaServicio refactorizado
        // List<CategoriaDTO> categorias = categoriaService.findAllActivas();

        model.addAttribute("productos", productos);
        // model.addAttribute("categorias", categorias);
        model.addAttribute("categorias", List.of()); // Temporal: lista vacía
        model.addAttribute("buscar", buscar);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());

        return "productos/lista";
    }

    // Lista administrativa de productos (admin)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String listaProductosAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            Model model) {

        Page<ProductoDTO> productos;

        if (buscar != null && !buscar.trim().isEmpty()) {
            productos = productoService.buscarTodosProductos(buscar.trim(), PageRequest.of(page, size));
        } else if ("inactivos".equals(estado)) {
            productos = productoService.findAllInactivos(PageRequest.of(page, size));
        } else {
            productos = productoService.findAll(PageRequest.of(page, size));
        }

        // TODO: Categorias - necesita CategoriaServicio refactorizado
        // List<CategoriaDTO> categorias = categoriaService.findAll();

        model.addAttribute("productos", productos);
        // model.addAttribute("categorias", categorias);
        model.addAttribute("categorias", List.of()); // Temporal: lista vacía
        model.addAttribute("buscar", buscar);
        model.addAttribute("estado", estado);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());

        return "productos/admin";
    }

    // Formulario para nuevo producto
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        // TODO: Categorias - necesita CategoriaServicio refactorizado
        model.addAttribute("categorias", List.of());
        return "productos/formulario";
    }

    // Guardar nuevo producto
    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    public String guardarProducto(@Valid @ModelAttribute ProductoDTO producto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        if (result.hasErrors()) {
            // TODO: Categorias - necesita CategoriaServicio refactorizado
            model.addAttribute("categorias", List.of());
            return "productos/formulario";
        }

        try {
            productoService.guardar(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/productos/admin";
    }

    // Editar producto
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    public String editarProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.findById(id);
            model.addAttribute("producto", producto);
            // TODO: Categorias - necesita CategoriaServicio refactorizado
            model.addAttribute("categorias", List.of());
            return "productos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/productos/admin";
        }
    }

    // Productos con bajo stock
    @GetMapping("/bajo-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    public String productosBajoStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "10") int stockMinimo,
            Model model) {

        Page<ProductoDTO> productos = productoService.findProductosBajoStock(stockMinimo, PageRequest.of(page, size));

        model.addAttribute("productos", productos);
        model.addAttribute("stockMinimo", stockMinimo);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());
        model.addAttribute("totalProductosBajoStock", productos.getTotalElements());

        return "productos/bajo-stock";
    }

    // TODO: Gestión de categorías requiere CategoriaServicio refactorizado
    /*
    @GetMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public String gestionCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("categoria", new CategoriaDTO());
        return "productos/categorias";
    }

    @PostMapping("/categorias/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarCategoria(@Valid @ModelAttribute CategoriaDTO categoria,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensaje", "Error en los datos de la categoría");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        } else {
            try {
                categoriaService.guardar(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría guardada exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("mensaje", "Error al guardar la categoría: " + e.getMessage());
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        }

        return "redirect:/productos/categorias";
    }
    */

    // Activar/Desactivar producto
    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstadoProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Determinar estado actual y cambiarlo
            productoService.cambiarEstado(id, true); // Necesita lógica para determinar el estado
            redirectAttributes.addFlashAttribute("mensaje", "Estado del producto actualizado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar estado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/productos/admin";
    }
}