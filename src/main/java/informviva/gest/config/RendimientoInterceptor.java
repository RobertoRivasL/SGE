package informviva.gest.config;

// =================== INTERCEPTOR DE RENDIMIENTO ===================

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RendimientoInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RendimientoInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            String uri = request.getRequestURI();
            String method = request.getMethod();
            int status = response.getStatus();

            // Log solo para requests que tomen más de 1 segundo
            if (duration > 1000) {
                logger.warn("SLOW REQUEST: {} {} - {}ms - Status: {}",
                        method, uri, duration, status);
            } else if (duration > 500) {
                logger.info("REQUEST: {} {} - {}ms - Status: {}",
                        method, uri, duration, status);
            }

            // Agregar header de tiempo de respuesta
            response.setHeader("X-Response-Time", duration + "ms");
        }
    }
}