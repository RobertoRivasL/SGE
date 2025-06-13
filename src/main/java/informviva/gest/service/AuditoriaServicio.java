package informviva.gest.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaServicio {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Async
    public void registrarAccion(String usuario, String accion, String entidad, Long entidadId, String detalles) {
        try {
            String mensaje = String.format("AUDIT [%s] Usuario: %s | Acción: %s | Entidad: %s | ID: %s | Detalles: %s",
                    LocalDateTime.now(), usuario, accion, entidad, entidadId, detalles);

            auditLogger.info(mensaje);

            // Aquí podrías agregar lógica para guardar en base de datos
            // si necesitas auditoría persistente

        } catch (Exception e) {
            // No lanzar excepción para no afectar el flujo principal
            LoggerFactory.getLogger(AuditoriaServicio.class)
                    .error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    @Async
    public void registrarInicioSesion(String usuario, String ip, boolean exitoso) {
        String resultado = exitoso ? "EXITOSO" : "FALLIDO";
        String mensaje = String.format("LOGIN %s [%s] Usuario: %s | IP: %s",
                resultado, LocalDateTime.now(), usuario, ip);

        auditLogger.info(mensaje);
    }

    @Async
    public void registrarCierreSession(String usuario, String ip) {
        String mensaje = String.format("LOGOUT [%s] Usuario: %s | IP: %s",
                LocalDateTime.now(), usuario, ip);

        auditLogger.info(mensaje);
    }
}