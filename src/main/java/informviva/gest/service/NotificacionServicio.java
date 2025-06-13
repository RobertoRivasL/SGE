package informviva.gest.service;

import informviva.gest.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import informviva.gest.model.Notificacion;
import informviva.gest.model.TipoNotificacion;
import informviva.gest.model.EstadisticasNotificaciones;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de notificaciones para el sistema
 */
@Service
public class NotificacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionServicio.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private ConfiguracionServicio configuracionServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    private final Map<String, List<Notificacion>> notificacionesPorUsuario = new ConcurrentHashMap<>();
    private final Queue<Notificacion> colaNotificaciones = new LinkedList<>();

    // ** Métodos de envío de notificaciones **

    public void notificarUsuario(String username, String titulo, String mensaje, TipoNotificacion tipo) {
        try {
            Notificacion notificacion = crearNotificacion(username, titulo, mensaje, tipo);
            agregarNotificacionUsuario(username, notificacion);
            procesarNotificacionAsync(notificacion);
            logger.debug("Notificación enviada a {}: {}", username, titulo);
        } catch (Exception e) {
            logger.error("Error enviando notificación a {}: {}", username, e.getMessage());
        }
    }

    public void notificarAdministradores(String titulo, String mensaje, TipoNotificacion tipo) {
        try {
            List<Usuario> administradores = obtenerUsuariosPorRol("ADMIN");
            administradores.forEach(admin -> notificarUsuario(admin.getUsername(), titulo, mensaje, tipo));
            logger.info("Notificación enviada a {} administradores: {}", administradores.size(), titulo);
        } catch (Exception e) {
            logger.error("Error notificando administradores: {}", e.getMessage());
        }
    }

    public void notificarTodos(String titulo, String mensaje, TipoNotificacion tipo) {
        try {
            List<Usuario> usuariosActivos = obtenerUsuariosActivos();
            usuariosActivos.forEach(usuario -> notificarUsuario(usuario.getUsername(), titulo, mensaje, tipo));
            logger.info("Notificación broadcast enviada a {} usuarios: {}", usuariosActivos.size(), titulo);
        } catch (Exception e) {
            logger.error("Error en notificación broadcast: {}", e.getMessage());
        }
    }

    // ** Métodos de obtención de notificaciones **

    public List<Notificacion> obtenerNotificacionesUsuario(String username) {
        return notificacionesPorUsuario.getOrDefault(username, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(Notificacion::getFechaCreacion).reversed())
                .collect(Collectors.toList());
    }

    public List<Notificacion> obtenerNotificacionesNoLeidas(String username) {
        return obtenerNotificacionesUsuario(username).stream()
                .filter(n -> !n.isLeida())
                .collect(Collectors.toList());
    }

    // ** Métodos de marcado de notificaciones **

    public boolean marcarComoLeida(String username, String notificacionId) {
        return modificarNotificacion(username, notificacionId, notificacion -> {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
        });
    }

    public int marcarTodasComoLeidas(String username) {
        List<Notificacion> notificaciones = notificacionesPorUsuario.get(username);
        if (notificaciones != null) {
            LocalDateTime ahora = LocalDateTime.now();
            return (int) notificaciones.stream()
                    .filter(n -> !n.isLeida())
                    .peek(n -> {
                        n.setLeida(true);
                        n.setFechaLectura(ahora);
                    })
                    .count();
        }
        return 0;
    }

    // ** Métodos de limpieza de notificaciones **

    public int limpiarNotificacionesAntiguas(int diasAntiguedad) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAntiguedad);
        int eliminadas = 0;

        for (List<Notificacion> notificaciones : notificacionesPorUsuario.values()) {
            eliminadas += notificaciones.removeIf(n -> n.getFechaCreacion().isBefore(fechaLimite)) ? 1 : 0;
        }

        logger.info("Limpieza completada: {} notificaciones eliminadas", eliminadas);
        return eliminadas;
    }

    // ** Métodos de estadísticas **

    public EstadisticasNotificaciones obtenerEstadisticas() {
        EstadisticasNotificaciones stats = new EstadisticasNotificaciones();
        int totalNotificaciones = 0;
        int totalNoLeidas = 0;
        Map<TipoNotificacion, Integer> porTipo = new HashMap<>();

        for (List<Notificacion> notificaciones : notificacionesPorUsuario.values()) {
            for (Notificacion notificacion : notificaciones) {
                totalNotificaciones++;
                if (!notificacion.isLeida()) {
                    totalNoLeidas++;
                }
                porTipo.merge(notificacion.getTipo(), 1, Integer::sum);
            }
        }

        stats.setTotalNotificaciones(totalNotificaciones);
        stats.setNoLeidas(totalNoLeidas);
        stats.setLeidas(totalNotificaciones - totalNoLeidas);
        stats.setPorTipo(porTipo);
        stats.setUsuariosConNotificaciones(notificacionesPorUsuario.size());

        return stats;
    }

    // ** Métodos privados auxiliares **

    private Notificacion crearNotificacion(String usuario, String titulo, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(UUID.randomUUID().toString());
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        notificacion.setPrioridad(tipo.getPrioridadDefecto());
        return notificacion;
    }

    private void agregarNotificacionUsuario(String username, Notificacion notificacion) {
        notificacionesPorUsuario.computeIfAbsent(username, k -> new ArrayList<>()).add(notificacion);
        colaNotificaciones.offer(notificacion);
    }

    private void procesarNotificacionAsync(Notificacion notificacion) {
        CompletableFuture.runAsync(() -> {
            try {
                if (configuracionServicio.obtenerConfiguracion().getHabilitarNotificaciones()) {
                    procesarNotificacionExtendida(notificacion);
                }
            } catch (Exception e) {
                logger.error("Error procesando notificación async: {}", e.getMessage());
            }
        });
    }

    private void procesarNotificacionExtendida(Notificacion notificacion) {
        switch (notificacion.getTipo()) {
            case ERROR ->
                    logger.error("NOTIFICACION ERROR: {} - {}", notificacion.getTitulo(), notificacion.getMensaje());
            case ADVERTENCIA ->
                    logger.warn("NOTIFICACION ADVERTENCIA: {} - {}", notificacion.getTitulo(), notificacion.getMensaje());
            case EXITO ->
                    logger.info("NOTIFICACION EXITO: {} - {}", notificacion.getTitulo(), notificacion.getMensaje());
            case INFORMACION ->
                    logger.debug("NOTIFICACION INFO: {} - {}", notificacion.getTitulo(), notificacion.getMensaje());
        }
    }

    private List<Usuario> obtenerUsuariosPorRol(String rol) {
        return usuarioServicio.listarTodos().stream()
                .filter(u -> u.getRoles().contains(rol))
                .collect(Collectors.toList());
    }

    private List<Usuario> obtenerUsuariosActivos() {
        return usuarioServicio.listarTodos().stream()
                .filter(Usuario::isActivo)
                .collect(Collectors.toList());
    }

    private boolean modificarNotificacion(String username, String notificacionId, java.util.function.Consumer<Notificacion> accion) {
        List<Notificacion> notificaciones = notificacionesPorUsuario.get(username);
        if (notificaciones != null) {
            return notificaciones.stream()
                    .filter(n -> n.getId().equals(notificacionId))
                    .findFirst()
                    .map(n -> {
                        accion.accept(n);
                        return true;
                    })
                    .orElse(false);
        }
        return false;
    }
}