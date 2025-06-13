package informviva.gest.service.impl;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.ConfiguracionSistema;
import informviva.gest.repository.ConfiguracionSistemaRepositorio;
import informviva.gest.service.ConfiguracionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionServicioImpl implements ConfiguracionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionServicioImpl.class);
    private final ConfiguracionSistemaRepositorio configuracionRepositorio;
    private Boolean habilitarNotificaciones;

    @Autowired
    public ConfiguracionServicioImpl(ConfiguracionSistemaRepositorio configuracionRepositorio) {
        this.configuracionRepositorio = configuracionRepositorio;
    }

    public Boolean getHabilitarNotificaciones() {
        return habilitarNotificaciones;
    }

    public void setHabilitarNotificaciones(Boolean habilitarNotificaciones) {
        this.habilitarNotificaciones = habilitarNotificaciones;
    }

    @Override
    public ConfiguracionSistema obtenerConfiguracion() {
        ConfiguracionSistema configuracion = configuracionRepositorio.findFirstByOrderByIdAsc();

        // Si no existe configuración, crear una por defecto
        if (configuracion == null) {
            configuracion = new ConfiguracionSistema();
            configuracion.setNombreEmpresa("Mi Empresa");
            configuracion.setEmailContacto("contacto@miempresa.com");
            configuracion.setHabilitarNotificaciones(true);
            configuracion.setColorPrimario("#0d6efd");
            configuracion = configuracionRepositorio.save(configuracion);
            logger.info("Se ha creado una configuración por defecto");
        }

        return configuracion;
    }

    @Override
    public ConfiguracionSistema guardarConfiguracion(ConfiguracionSistema configuracion, String usuarioActual) {
        try {
            // Verificar si ya existe una configuración
            ConfiguracionSistema configuracionExistente = configuracionRepositorio.findFirstByOrderByIdAsc();

            if (configuracionExistente != null) {
                // Actualizar el ID para mantener el registro existente
                configuracion.setId(configuracionExistente.getId());
            }

            // Establecer el usuario que realizó la actualización
            configuracion.setUsuarioActualizacion(usuarioActual);

            // Guardar la configuración
            configuracion = configuracionRepositorio.save(configuracion);
            logger.info("Configuración actualizada por usuario: {}", usuarioActual);

            return configuracion;
        } catch (Exception e) {
            logger.error("Error al guardar la configuración: {}", e.getMessage());
            throw new RuntimeException("Error al guardar la configuración", e);
        }
    }

    @Override
    public boolean probarConfiguracionCorreo(String emailDestino) {
        try {
            // Aquí iría la implementación para enviar un correo de prueba
            // Usando la configuración SMTP almacenada

            // Por ahora, simularemos que se envió correctamente
            logger.info("Correo de prueba enviado a: {}", emailDestino);
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar correo de prueba: {}", e.getMessage());
            return false;
        }
    }
}
