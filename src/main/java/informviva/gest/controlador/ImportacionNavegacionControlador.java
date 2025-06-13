package informviva.gest.controlador;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para navegación adicional del módulo de importación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/importacion")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ImportacionNavegacionControlador {

    /**
     * Redirección desde ruta base a página principal
     */
    @GetMapping("")
    public String redirigirAIndex() {
        return "redirect:/importacion/";
    }

    /**
     * Página de historial (implementación futura)
     */
    @GetMapping("/historial")
    public String mostrarHistorial() {
        // Por ahora redirige a la página principal
        // TODO: Implementar historial de importaciones
        return "redirect:/importacion?info=historial-en-desarrollo";
    }

    /**
     * Página de configuración de importación
     */
    @GetMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarConfiguracion() {
        // TODO: Implementar configuración de importación
        return "redirect:/importacion?info=configuracion-en-desarrollo";
    }
}