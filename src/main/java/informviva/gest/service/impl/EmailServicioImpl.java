package informviva.gest.service.impl;

import informviva.gest.service.EmailServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServicioImpl implements EmailServicio {

    private static final Logger logger = LoggerFactory.getLogger(EmailServicioImpl.class);

    @Override
    public void enviarEmail(String destinatario, String asunto, String contenido) {
        // Implementación temporal - configurar con JavaMailSender
        logger.info("Enviando email a: {} con asunto: {}", destinatario, asunto);
    }

    @Override
    public void enviarEmailConfirmacion(String destinatario, String nombreCliente) {
        String asunto = "Confirmación de registro";
        String contenido = "Estimado/a " + nombreCliente + ", su registro ha sido confirmado.";
        enviarEmail(destinatario, asunto, contenido);
    }
}
