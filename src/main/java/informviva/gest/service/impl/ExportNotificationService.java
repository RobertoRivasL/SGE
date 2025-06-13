package informviva.gest.service.impl;

// ===================================================================
// SERVICIO DE NOTIFICACIONES POR EMAIL - ExportNotificationService.java
// ===================================================================

import informviva.gest.model.ExportacionHistorial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar notificaciones por email sobre exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ExportNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ExportNotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${exportacion.email.from:noreply@informviva.com}")
    private String fromEmail;

    @Value("${exportacion.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${exportacion.email.notify-completion:true}")
    private boolean notifyCompletion;

    @Value("${exportacion.email.notify-errors:true}")
    private boolean notifyErrors;

    /**
     * Envía notificación de exportación completada
     */
    public void notificarExportacionCompletada(ExportacionHistorial historial, String emailDestino) {
        if (!emailEnabled || !notifyCompletion || mailSender == null) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailDestino);
            message.setSubject("Exportación Completada - " + historial.getTipoExportacion());

            String contenido = String.format(
                    "Su exportación ha sido completada exitosamente.\n\n" +
                            "Detalles:\n" +
                            "- Tipo: %s\n" +
                            "- Formato: %s\n" +
                            "- Registros: %d\n" +
                            "- Tamaño: %s\n" +
                            "- Tiempo: %s\n" +
                            "- Fecha: %s\n\n" +
                            "Puede descargar el archivo desde el sistema.\n\n" +
                            "Saludos,\n" +
                            "Sistema InformViva",
                    historial.getTipoExportacion(),
                    historial.getFormato().toUpperCase(),
                    historial.getNumeroRegistros(),
                    historial.getTamanoLegible(),
                    historial.getTiempoLegible(),
                    historial.getFechaSolicitud()
            );

            message.setText(contenido);
            mailSender.send(message);

            logger.info("Notificación de exportación completada enviada a: {}", emailDestino);

        } catch (Exception e) {
            logger.error("Error enviando notificación de exportación completada: {}", e.getMessage());
        }
    }

    /**
     * Envía notificación de error en exportación
     */
    public void notificarErrorExportacion(ExportacionHistorial historial, String emailDestino) {
        if (!emailEnabled || !notifyErrors || mailSender == null) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailDestino);
            message.setSubject("Error en Exportación - " + historial.getTipoExportacion());

            String contenido = String.format(
                    "Ha ocurrido un error durante su exportación.\n\n" +
                            "Detalles:\n" +
                            "- Tipo: %s\n" +
                            "- Formato: %s\n" +
                            "- Error: %s\n" +
                            "- Fecha: %s\n\n" +
                            "Por favor, intente nuevamente o contacte al administrador del sistema.\n\n" +
                            "Saludos,\n" +
                            "Sistema InformViva",
                    historial.getTipoExportacion(),
                    historial.getFormato().toUpperCase(),
                    historial.getMensajeError(),
                    historial.getFechaSolicitud()
            );

            message.setText(contenido);
            mailSender.send(message);

            logger.info("Notificación de error en exportación enviada a: {}", emailDestino);

        } catch (Exception e) {
            logger.error("Error enviando notificación de error en exportación: {}", e.getMessage());
        }
    }
}