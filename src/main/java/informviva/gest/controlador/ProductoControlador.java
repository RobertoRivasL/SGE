package informviva.gest.controlador;

import informviva.gest.model.Producto;
import informviva.gest.model.Categoria;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.CategoriaServicio;
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

    @Autowired
    private CategoriaServicio categoriaService;

    // Lista básica de productos (usuario normal)
    @GetMapping("/lista")
    public String listaProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long categoriaId,
            Model model) {

        Page<Producto> productos;

        if (buscar != null && !buscar.trim().isEmpty()) {
            productos = productoService.buscarProductos(buscar.trim(), PageRequest.of(page, size));
        } else if (categoriaId != null) {
            productos = productoService.findByCategoriaId(categoriaId, PageRequest.of(page, size));
        } else {
            productos = productoService.findAllActivos(PageRequest.of(page, size));
        }

        List<Categoria> categorias = categoriaService.findAllActivas();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
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

        Page<Producto> productos;

        if (buscar != null && !buscar.trim().isEmpty()) {
            productos = productoService.buscarTodosProductos(buscar.trim(), PageRequest.of(page, size));
        } else if ("inactivos".equals(estado)) {
            productos = productoService.findAllInactivos(PageRequest.of(page, size));
        } else {
            productos = productoService.findAll(PageRequest.of(page, size));
        }

        List<Categoria> categorias = categoriaService.findAll();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
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
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.findAllActivas());
        return "productos/formulario";
    }

    // Guardar nuevo producto
    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    public String guardarProducto(@Valid @ModelAttribute Producto producto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.findAllActivas());
            return "productos/formulario";
        }

        try {
            productoService.save(producto);
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
            Producto producto = productoService.findById(id);
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaService.findAllActivas());
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

        Page<Producto> productos = productoService.findProductosBajoStock(stockMinimo, PageRequest.of(page, size));

        model.addAttribute("productos", productos);
        model.addAttribute("stockMinimo", stockMinimo);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());
        model.addAttribute("totalProductosBajoStock", productos.getTotalElements());

        return "productos/bajo-stock";
    }

    // Gestión de categorías
    @GetMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public String gestionCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("categoria", new Categoria());
        return "productos/categorias";
    }

    // Guardar categoría
    @PostMapping("/categorias/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarCategoria(@Valid @ModelAttribute Categoria categoria,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensaje", "Error en los datos de la categoría");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        } else {
            try {
                categoriaService.save(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría guardada exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("mensaje", "Error al guardar la categoría: " + e.getMessage());
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }
        }

        return "redirect:/productos/categorias";
    }

    // Activar/Desactivar producto
    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstadoProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.cambiarEstado(id, true); // O el valor booleano necesario
            redirectAttributes.addFlashAttribute("mensaje", "Estado del producto actualizado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar estado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/productos/admin";
    }
}