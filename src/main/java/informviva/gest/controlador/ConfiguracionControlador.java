package informviva.gest.controlador;

import informviva.gest.model.ConfiguracionSistema;
import informviva.gest.service.ConfiguracionServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la administración de la configuración del sistema
 */
@Controller
@RequestMapping("/admin/configuracion")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConfiguracionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionControlador.class);

    private static final String MENSAJE_EXITO = "La configuración se ha guardado correctamente";
    private static final String MENSAJE_ERROR = "Error al guardar la configuración: ";
    private static final String VISTA_CONFIGURACION = "admin/configuracion";

    private final ConfiguracionServicio configuracionServicio;

    @Autowired
    public ConfiguracionControlador(ConfiguracionServicio configuracionServicio) {
        this.configuracionServicio = configuracionServicio;
    }

    /**
     * Muestra la página de configuración del sistema
     */
    @GetMapping
    public String mostrarConfiguracion(Model model) {
        ConfiguracionSistema configuracion = configuracionServicio.obtenerConfiguracion();
        model.addAttribute("configuracion", configuracion);
        return VISTA_CONFIGURACION;
    }

    /**
     * Procesa la actualización de la configuración del sistema
     */
    @PostMapping("/guardar")
    public String guardarConfiguracion(
            @Valid @ModelAttribute("configuracion") ConfiguracionSistema configuracion,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        if (result.hasErrors()) {
            return VISTA_CONFIGURACION;
        }

        try {
            String usuarioActual = authentication.getName();
            configuracionServicio.guardarConfiguracion(configuracion, usuarioActual);
            agregarMensajeExito(redirectAttributes);
        } catch (Exception e) {
            manejarError(e, redirectAttributes);
        }

        return "redirect:/admin/configuracion";
    }

    /**
     * Prueba la configuración de correo electrónico
     */
    @PostMapping("/probar-correo")
    @ResponseBody
    public String probarCorreo(@RequestParam String email) {
        if (!esCorreoValido(email)) {
            return "El correo proporcionado no es válido.";
        }

        try {
            boolean resultado = configuracionServicio.probarConfiguracionCorreo(email);
            return resultado
                    ? "Correo de prueba enviado correctamente a: " + email
                    : "No se pudo enviar el correo de prueba. Por favor, revisa la configuración SMTP.";
        } catch (Exception e) {
            logger.error("Error al probar configuración de correo: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    // Métodos privados para reutilización de lógica

    private void agregarMensajeExito(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensajeExito", MENSAJE_EXITO);
    }

    private void manejarError(Exception e, RedirectAttributes redirectAttributes) {
        logger.error(MENSAJE_ERROR + "{}", e.getMessage());
        redirectAttributes.addFlashAttribute("mensajeError", MENSAJE_ERROR + e.getMessage());
    }

    private boolean esCorreoValido(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}