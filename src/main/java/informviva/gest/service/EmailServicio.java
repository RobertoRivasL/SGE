package informviva.gest.service;

import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails
 */
@Service
public interface EmailServicio {
    void enviarEmail(String destinatario, String asunto, String contenido);
    void enviarEmailConfirmacion(String destinatario, String nombreCliente);
}
