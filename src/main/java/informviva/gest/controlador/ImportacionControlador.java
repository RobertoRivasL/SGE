package informviva.gest.controlador;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controlador corregido para la gestión de importaciones
 */
@Controller
@RequestMapping("/importacion")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ImportacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionControlador.class);
    private static final List<String> TIPOS_ENTIDAD_VALIDOS = List.of("cliente", "producto", "usuario");

    @Autowired
    private ImportacionServicio importacionServicio;

    /**
     * Muestra la página principal de importación
     */
    @GetMapping
    public String mostrarPaginaImportacion(Model model) {
        try {
            model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());
            model.addAttribute("tiposEntidad", TIPOS_ENTIDAD_VALIDOS);
            return "importacion/index";
        } catch (Exception e) {
            logger.error("Error cargando página de importación: {}", e.getMessage(), e);
            model.addAttribute("mensajeError", "Error al cargar la página: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Muestra el formulario de importación para un tipo específico de entidad
     */
    @GetMapping("/{tipoEntidad}")
    public String mostrarFormularioImportacion(@PathVariable String tipoEntidad, Model model, RedirectAttributes redirectAttributes) {
        if (!esTipoEntidadValido(tipoEntidad)) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tipo de entidad no válido: " + tipoEntidad);
            return "redirect:/importacion?error=tipo-invalido";
        }

        try {
            model.addAttribute("tipoEntidad", tipoEntidad.toLowerCase());
            model.addAttribute("tipoEntidadCapitalizado", capitalizarPrimeraLetra(tipoEntidad));
            model.addAttribute("columnasRequeridas", importacionServicio.obtenerColumnasRequeridas(tipoEntidad));
            model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());

            // Agregar columnas específicas para cada tipo para la ayuda
            agregarColumnasEspecificas(model, tipoEntidad);

            return "importacion/formulario";
        } catch (Exception e) {
            logger.error("Error mostrando formulario para {}: {}", tipoEntidad, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/importacion";
        }
    }

    /**
     * Procesa la importación de archivos
     */
    @PostMapping("/{tipoEntidad}/procesar")
    public String procesarImportacion(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validaciones previas
        if (!esTipoEntidadValido(tipoEntidad)) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tipo de entidad no válido");
            return "redirect:/importacion";
        }

        if (archivo == null || archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar un archivo para importar");
            return "redirect:/importacion/" + tipoEntidad;
        }

        try {
            // Validar archivo antes de procesar
            Map<String, Object> validacion = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);

            if (!(Boolean) validacion.get("valido")) {
                @SuppressWarnings("unchecked")
                List<String> errores = (List<String>) validacion.get("errores");
                String mensajeError = "Archivo inválido: " + String.join(", ", errores);
                redirectAttributes.addFlashAttribute("mensajeError", mensajeError);
                return "redirect:/importacion/" + tipoEntidad;
            }

            // Procesar importación según el tipo de entidad
            ImportacionResultadoDTO resultado = procesarSegunTipo(tipoEntidad, archivo);

            if (resultado == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "Error procesando la importación");
                return "redirect:/importacion/" + tipoEntidad;
            }

            // Agregar resultado al modelo para mostrar en la vista
            model.addAttribute("resultado", resultado);
            model.addAttribute("tipoEntidad", tipoEntidad.toLowerCase());
            model.addAttribute("tipoEntidadCapitalizado", capitalizarPrimeraLetra(tipoEntidad));

            logger.info("Importación de {} completada: {} exitosos, {} errores",
                    tipoEntidad, resultado.getRegistrosExitosos(), resultado.getRegistrosConError());

            return "importacion/resultado";

        } catch (Exception e) {
            logger.error("Error procesando importación de {}: {}", tipoEntidad, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error procesando el archivo: " + e.getMessage());
            return "redirect:/importacion/" + tipoEntidad;
        }
    }

    /**
     * Muestra vista previa del archivo antes de importar
     */
    @PostMapping("/{tipoEntidad}/vista-previa")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerVistaPrevia(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {

        if (!esTipoEntidadValido(tipoEntidad)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "errores", List.of("Tipo de entidad no válido")));
        }

        if (archivo == null || archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "errores", List.of("No se proporcionó archivo")));
        }

        try {
            Map<String, Object> validacion = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);

            if ((Boolean) validacion.get("valido")) {
                List<Map<String, Object>> vistaPrevia = importacionServicio.obtenerVistaPreviaArchivo(archivo, 10);
                validacion.put("vistaPrevia", vistaPrevia);

                // Agregar información adicional
                validacion.put("nombreArchivo", archivo.getOriginalFilename());
                validacion.put("tamanoArchivo", formatearTamanoArchivo(archivo.getSize()));
            }

            return ResponseEntity.ok(validacion);

        } catch (Exception e) {
            logger.error("Error obteniendo vista previa para {}: {}", tipoEntidad, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valido", false,
                            "errores", List.of("Error procesando archivo: " + e.getMessage())));
        }
    }

    /**
     * Descarga plantilla de importación
     */
    @GetMapping("/{tipoEntidad}/plantilla")
    public ResponseEntity<byte[]> descargarPlantilla(
            @PathVariable String tipoEntidad,
            @RequestParam(defaultValue = "excel") String formato) {

        if (!esTipoEntidadValido(tipoEntidad)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] plantilla = importacionServicio.generarPlantillaImportacion(tipoEntidad, formato);

            if (plantilla.length == 0) {
                logger.warn("Plantilla generada está vacía para tipo: {} formato: {}", tipoEntidad, formato);
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            String nombreArchivo = "plantilla_" + tipoEntidad + "_importacion";

            if ("csv".equalsIgnoreCase(formato)) {
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.add("Content-Type", "text/csv; charset=UTF-8");
                nombreArchivo += ".csv";
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                nombreArchivo += ".xlsx";
            }

            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("Descargando plantilla: {} formato: {}", tipoEntidad, formato);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(plantilla);

        } catch (Exception e) {
            logger.error("Error generando plantilla para {} formato {}: {}", tipoEntidad, formato, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST para validar archivo
     */
    @PostMapping("/api/{tipoEntidad}/validar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarArchivo(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {

        if (!esTipoEntidadValido(tipoEntidad)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "errores", List.of("Tipo de entidad no válido")));
        }

        try {
            Map<String, Object> resultado = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando archivo para {}: {}", tipoEntidad, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valido", false,
                            "errores", List.of("Error interno: " + e.getMessage())));
        }
    }

    /**
     * Muestra el historial de importaciones
     */
    @GetMapping("/historial")
    public String mostrarHistorialImportaciones(Model model) {
        try {
            // TODO: Implementar historial de importaciones si se requiere persistencia
            model.addAttribute("mensaje", "Funcionalidad de historial en desarrollo");
            model.addAttribute("importacionesRecientes", List.of());
            return "importacion/historial";
        } catch (Exception e) {
            logger.error("Error cargando historial: {}", e.getMessage(), e);
            model.addAttribute("mensajeError", "Error al cargar el historial");
            return "importacion/historial";
        }
    }

    /**
     * Muestra la ayuda para importaciones
     */
    @GetMapping("/ayuda")
    public String mostrarAyudaImportacion(Model model) {
        try {
            model.addAttribute("tiposEntidad", TIPOS_ENTIDAD_VALIDOS);
            model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());

            // Agregar información de columnas para cada tipo
            for (String tipo : TIPOS_ENTIDAD_VALIDOS) {
                String atributo = "columnas" + capitalizarPrimeraLetra(tipo);
                model.addAttribute(atributo, importacionServicio.obtenerColumnasRequeridas(tipo));
            }

            return "importacion/ayuda";
        } catch (Exception e) {
            logger.error("Error cargando ayuda: {}", e.getMessage(), e);
            model.addAttribute("mensajeError", "Error al cargar la ayuda");
            return "importacion/ayuda";
        }
    }

    // Métodos auxiliares privados

    private ImportacionResultadoDTO procesarSegunTipo(String tipoEntidad, MultipartFile archivo) {
        long inicioTiempo = System.currentTimeMillis();
        ImportacionResultadoDTO resultado;

        try {
            switch (tipoEntidad.toLowerCase()) {
                case "cliente":
                    resultado = importacionServicio.importarClientes(archivo);
                    break;
                case "producto":
                    resultado = importacionServicio.importarProductos(archivo);
                    break;
                case "usuario":
                    // Validar permisos adicionales para usuarios
                    if (!tienePermisoUsuarios()) {
                        throw new SecurityException("No tiene permisos para importar usuarios");
                    }
                    resultado = importacionServicio.importarUsuarios(archivo);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de entidad no soportado: " + tipoEntidad);
            }

            long tiempoTranscurrido = System.currentTimeMillis() - inicioTiempo;
            resultado.setTiempoProcesamientoMs(tiempoTranscurrido);

            return resultado;
        } catch (Exception e) {
            logger.error("Error en procesarSegunTipo para {}: {}", tipoEntidad, e.getMessage(), e);

            // Crear resultado de error
            ImportacionResultadoDTO resultadoError = new ImportacionResultadoDTO();
            resultadoError.setTipoEntidad(capitalizarPrimeraLetra(tipoEntidad));
            resultadoError.setNombreArchivo(archivo.getOriginalFilename());
            resultadoError.agregarError("Error durante la importación: " + e.getMessage());
            resultadoError.calcularTotales();

            return resultadoError;
        }
    }

    private boolean esTipoEntidadValido(String tipoEntidad) {
        return tipoEntidad != null && TIPOS_ENTIDAD_VALIDOS.contains(tipoEntidad.toLowerCase());
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    private void agregarColumnasEspecificas(Model model, String tipoEntidad) {
        List<String> columnas = importacionServicio.obtenerColumnasRequeridas(tipoEntidad);
        String atributoColumnas = "columnas" + capitalizarPrimeraLetra(tipoEntidad);
        model.addAttribute(atributoColumnas, columnas);
    }

    private String formatearTamanoArchivo(long bytes) {
        if (bytes <= 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    private boolean tienePermisoUsuarios() {
        // TODO: Implementar lógica de permisos más específica si es necesario
        // Por ahora, el @PreAuthorize en la clase ya maneja los permisos básicos
        return true;
    }

    /**
     * Endpoint para obtener estadísticas de importación
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = Map.of(
                    "formatosSoportados", importacionServicio.obtenerFormatosSoportados(),
                    "tiposEntidad", TIPOS_ENTIDAD_VALIDOS,
                    "limiteArchivoMB", 10,
                    "procesadorActivo", true
            );

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Manejo de errores para archivos demasiado grandes
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException e,
                                              RedirectAttributes redirectAttributes) {
        logger.warn("Archivo demasiado grande: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("mensajeError",
                "El archivo es demasiado grande. El tamaño máximo permitido es 10MB.");
        return "redirect:/importacion";
    }

    /**
     * Manejo de errores generales
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        logger.error("Error general en importación: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("mensajeError",
                "Ha ocurrido un error inesperado: " + e.getMessage());
        return "redirect:/importacion";
    }
}