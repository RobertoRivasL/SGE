package informviva.gest.controlador;

import informviva.gest.dto.ResultadoValidacionSistema;
import informviva.gest.service.ValidacionDatosServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador temporal para probar que el sistema de validación funciona
 * ELIMINAR DESPUÉS DE VERIFICAR QUE TODO FUNCIONA
 */
@Controller
@RequestMapping("/test-validacion")
@PreAuthorize("hasRole('ADMIN')")
public class ControladorPruebaValidacion {

    @Autowired
    private ValidacionDatosServicio validacionDatosServicio;

    /**
     * Página de prueba con resultados de validación
     * URL: /test-validacion
     */
    @GetMapping
    public String paginaPrueba(Model model) {
        try {
            // Ejecutar validación completa
            ResultadoValidacionSistema resultado = validacionDatosServicio.validarSistemaCompleto();

            model.addAttribute("resultado", resultado);
            model.addAttribute("resumenHTML", resultado.generarResumenHTML());
            model.addAttribute("resumenTexto", resultado.generarResumenEjecutivo());

            return "test-validacion"; // Crear esta vista temporal

        } catch (Exception e) {
            model.addAttribute("error", "Error ejecutando validación: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * API REST para verificar funcionamiento
     * URL: /test-validacion/api
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> apiPrueba() {
        try {
            ResultadoValidacionSistema resultado = validacionDatosServicio.validarSistemaCompleto();

            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                            "estado", "FUNCIONAL",
                            "sistemaConsistente", resultado.isSistemaConsistente(),
                            "erroresCriticos", resultado.getTotalErroresCriticos(),
                            "advertencias", resultado.getTotalAdvertencias(),
                            "tiempoMs", resultado.getTiempoValidacionMs(),
                            "fecha", resultado.getFechaValidacion().toString()
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Map.of(
                            "estado", "ERROR",
                            "mensaje", e.getMessage()
                    ));
        }
    }

    /**
     * Validación rápida
     * URL: /test-validacion/rapida
     */
    @GetMapping("/rapida")
    @ResponseBody
    public ResponseEntity<?> validacionRapida() {
        try {
            boolean esConsistente = validacionDatosServicio.esSistemaConsistente();

            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                            "sistemaConsistente", esConsistente,
                            "mensaje", esConsistente ? "✅ Sistema consistente" : "⚠️ Sistema inconsistente"
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Map.of(
                            "error", e.getMessage()
                    ));
        }
    }
}