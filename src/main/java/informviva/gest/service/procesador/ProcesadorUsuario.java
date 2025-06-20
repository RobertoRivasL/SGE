package informviva.gest.service.procesador;

import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.model.Usuario;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.service.importacion.ImportacionValidador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Procesador especializado para usuarios
 */
@Component
public class ProcesadorUsuario implements ProcesadorEntidad<Usuario> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorUsuario.class);

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ImportacionValidador validador;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public String getTipoEntidad() {
        return "usuario";
    }

    @Override
    @Transactional
    public Usuario mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Usuario usuario = new Usuario();

            usuario.setUsername(obtenerValorString(fila, "username"));
            usuario.setPassword(passwordEncoder.encode(obtenerValorString(fila, "password")));
            usuario.setNombre(obtenerValorString(fila, "nombre"));
            usuario.setApellido(obtenerValorString(fila, "apellido"));
            usuario.setEmail(obtenerValorString(fila, "email"));

            // Procesar roles
            String rolesStr = obtenerValorString(fila, "roles");
            Set<String> roles = new HashSet<>();
            if (!rolesStr.isEmpty()) {
                String[] rolesArray = rolesStr.split(";");
                for (String rol : rolesArray) {
                    roles.add(rol.trim().toUpperCase());
                }
            } else {
                roles.add("USER");
            }
            usuario.setRoles(roles);

            // Procesar estado activo
            String activoStr = obtenerValorString(fila, "activo");
            boolean activo = activoStr.isEmpty() ||
                    "true".equalsIgnoreCase(activoStr) ||
                    "1".equals(activoStr) ||
                    "sí".equalsIgnoreCase(activoStr) ||
                    "si".equalsIgnoreCase(activoStr);
            usuario.setActivo(activo);

            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setUltimoAcceso(LocalDateTime.now());

            return usuario;
        } catch (Exception e) {
            logger.error("Error mapeando usuario en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Usuario usuario) {
        return usuarioServicio.buscarPorUsername(usuario.getUsername()) != null;
    }

    @Override
    @Transactional
    public void guardarEntidad(Usuario usuario) {
        usuarioServicio.guardar(usuario);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Usuario usuario, int numeroFila) {
        Map<String, Object> fila = new HashMap<>();
        fila.put("username", usuario.getUsername());
        fila.put("password", "********"); // No exponer la contraseña
        fila.put("nombre", usuario.getNombre());
        fila.put("apellido", usuario.getApellido());
        fila.put("email", usuario.getEmail());
        fila.put("roles", String.join(";", usuario.getRoles()));
        fila.put("activo", String.valueOf(usuario.isActivo()));

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}