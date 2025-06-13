package informviva.gest.config;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.util.MensajesConstantes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Filtro para registrar accesos y acciones de seguridad
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Obtener información del usuario autenticado (si existe)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())
                ? auth.getName()
                : "Anonymous";

        // Obtener información de la solicitud
        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String remoteAddr = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String timestamp = LocalDateTime.now().format(formatter);

        // Registrar antes de procesar la solicitud
        if (requestURI.startsWith("/api") ||
                requestURI.equals("/login") ||
                requestURI.equals("/logout") ||
                requestURI.startsWith("/panel")) {

            logger.info("[{}] Usuario: {} | {}:{} | IP: {} | Agente: {}",
                    timestamp, username, requestMethod, requestURI, remoteAddr, userAgent);
        }

        // Procesar la solicitud
        filterChain.doFilter(request, response);

        // No registramos nada después de la respuesta para evitar sobrecarga de logs
    }

    /**
     * Obtiene la dirección IP real del cliente, incluso detrás de proxies
     *
     * @param request La solicitud HTTP
     * @return La dirección IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || MensajesConstantes.UNK.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || MensajesConstantes.UNK.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || MensajesConstantes.UNK.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || MensajesConstantes.UNK.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || MensajesConstantes.UNK.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Si hay múltiples IPs (X-Forwarded-For puede contener una lista), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}