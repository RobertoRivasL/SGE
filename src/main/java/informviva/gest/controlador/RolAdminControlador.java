package informviva.gest.controlador;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.Rol;
import informviva.gest.service.RolServicio;
import informviva.gest.service.RolVistaServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para la administración de roles y permisos
 */
@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RolAdminControlador {

    private final RolServicio rolServicio;
    private final RolVistaServicio rolVistaServicio;

    @Autowired
    public RolAdminControlador(RolServicio rolServicio, RolVistaServicio rolVistaServicio) {
        this.rolServicio = rolServicio;
        this.rolVistaServicio = rolVistaServicio;
    }

    /**
     * Muestra la lista de roles
     */
    @GetMapping
    public String listarRoles(Model model) {
        List<Rol> roles = rolServicio.listarTodos();
        model.addAttribute("roles", roles);
        return "admin/roles";
    }

    /**
     * Muestra el formulario para un nuevo rol
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoRol(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("esNuevo", true);
        return "admin/rol-form";
    }

    /**
     * Procesa la creación de un nuevo rol
     */
    @PostMapping("/guardar")
    public String guardarRol(
            @Valid @ModelAttribute("rol") Rol rol,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/rol-form";
        }

        try {
            // Verificar si el rol ya existe
            if (rol.getId() == null && rolServicio.buscarPorNombre(rol.getNombre()) != null) {
                result.rejectValue("nombre", "error.rol", "El nombre de rol ya está en uso");
                return "admin/rol-form";
            }

            rolServicio.guardar(rol);

            String mensaje = rol.getId() == null ?
                    "Rol creado correctamente" :
                    "Rol actualizado correctamente";
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);

            // Registrar la vista del rol
            rolVistaServicio.registrarVistaRol(rol.getNombre());

            return "redirect:/admin/roles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al guardar el rol: " + e.getMessage());
            return "redirect:/admin/roles";
        }
    }

    /**
     * Muestra el formulario para editar un rol existente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarRol(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Rol rol = rolServicio.buscarPorId(id);

        if (rol == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Rol no encontrado");
            return "redirect:/admin/roles";
        }

        model.addAttribute("rol", rol);
        model.addAttribute("esNuevo", false);

        // Registrar la vista del rol
        rolVistaServicio.registrarVistaRol(rol.getNombre());

        return "admin/rol-form";
    }

    /**
     * Elimina un rol
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarRol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (rolServicio.eliminar(id)) {
                redirectAttributes.addFlashAttribute("mensajeExito", "Rol eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "No se puede eliminar el rol porque está asignado a usuarios");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al eliminar el rol: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    /**
     * Muestra la página de permisos para un rol
     */
    @GetMapping("/permisos/{id}")
    public String mostrarPermisos(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Rol rol = rolServicio.buscarPorId(id);

        if (rol == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Rol no encontrado");
            return "redirect:/admin/roles";
        }

        model.addAttribute("rol", rol);
        model.addAttribute("permisosDisponibles", rolServicio.listarTodosLosPermisos());

        // Registrar la vista del rol
        rolVistaServicio.registrarVistaRol(rol.getNombre());

        return "admin/rol-permisos";
    }

    /**
     * Actualiza los permisos de un rol
     */
    @PostMapping("/permisos/{id}")
    public String actualizarPermisos(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> permisos,
            RedirectAttributes redirectAttributes) {

        try {
            rolServicio.actualizarPermisos(id, permisos);
            redirectAttributes.addFlashAttribute("mensajeExito", "Permisos actualizados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al actualizar permisos: " + e.getMessage());
        }

        return "redirect:/admin/roles";
    }
}