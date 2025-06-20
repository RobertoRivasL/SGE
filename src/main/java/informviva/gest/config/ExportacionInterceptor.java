package informviva.gest.config;

import informviva.gest.model.ExportacionHistorial;
import informviva.gest.service.ExportacionHistorialServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para registrar automáticamente las exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Component
public class ExportacionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionInterceptor.class);

    private final ExportacionHistorialServicio exportacionHistorialServicio;

    @Autowired
    public ExportacionInterceptor(ExportacionHistorialServicio exportacionHistorialServicio) {
        this.exportacionHistorialServicio = exportacionHistorialServicio;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Detectar solicitudes de exportación
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equals(method) && isExportRequest(uri)) {
            try {
                String tipoExportacion = detectarTipoExportacion(uri);
                String entidad = detectarEntidad(uri);
                String usuario = obtenerUsuarioActual();

                if (tipoExportacion != null && entidad != null && usuario != null) {
                    // Registrar el inicio de la exportación
                    ExportacionHistorial exportacion = exportacionHistorialServicio.registrarExportacion(
                            entidad, tipoExportacion, usuario, null, null);

                    // Guardar información en el request para uso posterior
                    request.setAttribute("exportacion.id", exportacion.getId());
                    request.setAttribute("exportacion.tipo", tipoExportacion);
                    request.setAttribute("exportacion.entidad", entidad);
                    request.setAttribute("exportacion.usuario", usuario);
                    request.setAttribute("exportacion.inicio", System.currentTimeMillis());
                }

            } catch (Exception e) {
                logger.error("Error en interceptor de exportación: {}", e.getMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Verificar si fue una solicitud de exportación
        Long exportacionId = (Long) request.getAttribute("exportacion.id");
        String tipoExportacion = (String) request.getAttribute("exportacion.tipo");
        String entidad = (String) request.getAttribute("exportacion.entidad");
        String usuario = (String) request.getAttribute("exportacion.usuario");
        Long inicio = (Long) request.getAttribute("exportacion.inicio");

        if (exportacionId != null && tipoExportacion != null && entidad != null && usuario != null && inicio != null) {
            try {
                long tiempoProcesamiento = System.currentTimeMillis() - inicio;

                // Actualizar tiempo de procesamiento
                exportacionHistorialServicio.actualizarTiempoProcesamiento(exportacionId, tiempoProcesamiento);

                if (ex == null && response.getStatus() == HttpServletResponse.SC_OK) {
                    // Exportación exitosa
                    long tamanoArchivo = response.getHeader("Content-Length") != null ?
                            Long.parseLong(response.getHeader("Content-Length")) : 0L;

                    exportacionHistorialServicio.marcarComoCompletada(
                            exportacionId, 0, tamanoArchivo, null);

                } else {
                    // Exportación fallida
                    String mensajeError = ex != null ? ex.getMessage() :
                            "Error HTTP " + response.getStatus();

                    exportacionHistorialServicio.marcarComoError(exportacionId, mensajeError);
                }

            } catch (Exception e) {
                logger.error("Error al finalizar registro de exportación: {}", e.getMessage());
            }
        }
    }

    /**
     * Determina si la solicitud es una exportación
     */
    private boolean isExportRequest(String uri) {
        return uri.contains("/exportar/") ||
                uri.contains("/export/") ||
                uri.endsWith("/pdf") ||
                uri.endsWith("/excel") ||
                uri.endsWith("/csv") ||
                uri.contains("/download");
    }

    /**
     * Detecta el tipo de exportación basado en la URI
     */
    private String detectarTipoExportacion(String uri) {
        if (uri.contains("/pdf") || uri.endsWith(".pdf")) {
            return "PDF";
        } else if (uri.contains("/excel") || uri.contains("/xlsx") || uri.endsWith(".xlsx")) {
            return "EXCEL";
        } else if (uri.contains("/csv") || uri.endsWith(".csv")) {
            return "CSV";
        }
        return "UNKNOWN";
    }

    /**
     * Detecta la entidad siendo exportada basado en la URI
     */
    private String detectarEntidad(String uri) {
        if (uri.contains("/clientes")) {
            return "CLIENTES";
        } else if (uri.contains("/productos")) {
            return "PRODUCTOS";
        } else if (uri.contains("/ventas")) {
            return "VENTAS";
        } else if (uri.contains("/reportes")) {
            return "REPORTES";
        } else if (uri.contains("/usuarios")) {
            return "USUARIOS";
        }
        return "GENERAL";
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad
     */
    private String obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()) ?
                    auth.getName() : "ANONIMO";
        } catch (Exception e) {
            return "SISTEMA";
        }
    }
}