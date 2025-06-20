package informviva.gest.controlador;

import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Categoria;
import informviva.gest.service.CategoriaServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Controlador para la gestión de categorías
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/categorias")
public class CategoriaControlador {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaControlador.class);

    // Vistas
    private static final String VISTA_LISTA = "categorias/lista";
    private static final String VISTA_FORMULARIO = "categorias/formulario";
    private static final String VISTA_DETALLE = "categorias/detalle";
    private static final String REDIRECT_LISTA = "redirect:/categorias";

    // Atributos del modelo
    private static final String ATTR_CATEGORIA = "categoria";
    private static final String ATTR_CATEGORIAS_PAGE = "categoriasPage";
    private static final String ATTR_ES_NUEVO = "esNuevo";
    private static final String ATTR_SEARCH = "search";
    private static final String ATTR_MENSAJE_EXITO = "mensajeExito";
    private static final String ATTR_MENSAJE_ERROR = "mensajeError";

    private final CategoriaServicio categoriaServicio;

    @Autowired
    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    /**
     * Lista todas las categorías con paginación y búsqueda
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String listarCategorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean soloActivas,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
            Page<Categoria> categoriasPage;

            if (search != null && !search.trim().isEmpty()) {
                categoriasPage = categoriaServicio.buscarPorTextoPaginado(search, pageable);
                model.addAttribute(ATTR_SEARCH, search);
            } else if (soloActivas != null && soloActivas) {
                categoriasPage = categoriaServicio.listarActivasPaginadas(pageable);
                model.addAttribute("soloActivas", true);
            } else {
                categoriasPage = categoriaServicio.listarPaginadas(pageable);
            }

            model.addAttribute(ATTR_CATEGORIAS_PAGE, categoriasPage);
            model.addAttribute("totalCategorias", categoriaServicio.contarTodas());
            model.addAttribute("categoriasActivas", categoriaServicio.contarActivas());
            model.addAttribute("categoriasInactivas", categoriaServicio.contarInactivas());

            return VISTA_LISTA;
        } catch (Exception e) {
            logger.error("Error al listar categorías: {}", e.getMessage());
            model.addAttribute(ATTR_MENSAJE_ERROR, "Error al cargar las categorías");
            return VISTA_LISTA;
        }
    }

    /**
     * Muestra el formulario para crear una nueva categoría
     */
    @GetMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute(ATTR_CATEGORIA, new Categoria());
        model.addAttribute(ATTR_ES_NUEVO, true);
        return VISTA_FORMULARIO;
    }

    /**
     * Procesa la creación o actualización de una categoría
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String guardarCategoria(
            @Valid @ModelAttribute(ATTR_CATEGORIA) Categoria categoria,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (categoriaServicio.existePorNombre(categoria.getNombre(), categoria.getId())) {
            result.rejectValue("nombre", "error.categoria", "Ya existe una categoría con este nombre");
        }

        if (result.hasErrors()) {
            model.addAttribute(ATTR_ES_NUEVO, categoria.getId() == null);
            return VISTA_FORMULARIO;
        }

        try {
            categoriaServicio.guardar(categoria);
            String mensaje = categoria.getId() == null ?
                    "Categoría creada exitosamente" :
                    "Categoría actualizada exitosamente";
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_EXITO, mensaje);

            return "redirect:/categorias/detalle/" + categoria.getId();
        } catch (Exception e) {
            logger.error("Error al guardar categoría: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, "Error al guardar categoría: " + e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    /**
     * Muestra el formulario para editar una categoría existente
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    public String editarCategoria(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaServicio.buscarPorId(id);
            model.addAttribute(ATTR_CATEGORIA, categoria);
            model.addAttribute(ATTR_ES_NUEVO, false);
            return VISTA_FORMULARIO;
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    /**
     * Muestra el detalle de una categoría
     */
    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE', 'VENTAS')")
    public String mostrarDetalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaServicio.buscarPorId(id);
            model.addAttribute(ATTR_CATEGORIA, categoria);
            return VISTA_DETALLE;
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    /**
     * Cambia el estado activo/inactivo de una categoría
     */
    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS')")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaServicio.buscarPorId(id);
            boolean nuevoEstado = !categoria.getActiva();

            if (categoriaServicio.cambiarEstado(id, nuevoEstado)) {
                String mensaje = "Categoría " + (nuevoEstado ? "activada" : "desactivada") + " exitosamente";
                redirectAttributes.addFlashAttribute(ATTR_MENSAJE_EXITO, mensaje);
            } else {
                redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, "Error al cambiar el estado de la categoría");
            }
        } catch (Exception e) {
            logger.error("Error al cambiar estado de categoría: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, "Error al cambiar estado: " + e.getMessage());
        }

        return REDIRECT_LISTA;
    }

    /**
     * Elimina una categoría
     */
    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!categoriaServicio.puedeSerEliminada(id)) {
                redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR,
                        "No se puede eliminar la categoría porque tiene productos asociados. Puede desactivarla en su lugar.");
                return REDIRECT_LISTA;
            }

            categoriaServicio.eliminar(id);
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_EXITO, "Categoría eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar categoría: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE_ERROR, "Error al eliminar categoría: " + e.getMessage());
        }

        return REDIRECT_LISTA;
    }

    /**
     * API REST para obtener categorías activas (para formularios de productos)
     */
    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'VENTAS')")
    @ResponseBody
    public List<Categoria> obtenerCategoriasActivas() {
        return categoriaServicio.listarActivas();
    }

    /**
     * API REST para obtener categorías más utilizadas
     */
    @GetMapping("/mas-utilizadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'GERENTE')")
    @ResponseBody
    public List<Categoria> obtenerMasUtilizadas(@RequestParam(defaultValue = "10") int limite) {
        return categoriaServicio.obtenerMasUtilizadas(limite);
    }

    /**
     * Búsqueda rápida de categorías
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOS', 'VENTAS')")
    @ResponseBody
    public List<Categoria> buscarCategorias(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return Collections.emptyList();
        }
        return categoriaServicio.buscarPorTexto(q);
    }
}