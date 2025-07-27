package informviva.gest.service.impl;

import informviva.gest.model.Usuario;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.service.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de usuarios
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service("usuarioServicioImpl")  // AGREGAR EL NOMBRE DEL BEAN
@Transactional
public class UsuarioServicioImpl implements UsuarioServicio {

    private final RepositorioUsuario usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServicioImpl(RepositorioUsuario usuarioRepositorio, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepositorio.findByUsername(username).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email).orElse(null);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        // Si es un usuario nuevo (sin ID), encriptar la contraseña
        if (usuario.getId() == null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setUltimoAcceso(LocalDateTime.now());

            // Si no tiene roles asignados, asignar rol USER por defecto
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                usuario.setRoles(Collections.singleton("USER"));
            }
        }

        return usuarioRepositorio.save(usuario);
    }

    @Override
    public void eliminar(Long id) {
        usuarioRepositorio.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarVendedores() {
        return listarTodos().stream()
                .filter(u -> u.isActivo() && (u.getRoles().contains("VENDEDOR") || u.getRoles().contains("VENTAS")))
                .collect(Collectors.toList());
    }

    @Override
    public boolean cambiarPassword(Long id, String nuevaPassword) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepositorio.save(usuario);

        return true;
    }

    @Override
    public boolean cambiarEstado(Long id, boolean activo) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        usuario.setActivo(activo);
        usuarioRepositorio.save(usuario);

        return true;
    }

    @Override
    public boolean agregarRol(Long id, String rol) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        Set<String> rolesActuales = usuario.getRoles() != null ? usuario.getRoles() : new HashSet<>();

        // Verificar si el rol ya existe
        if (rolesActuales.contains(rol)) {
            return true; // Ya tiene el rol
        }

        // Agregar el nuevo rol
        rolesActuales.add(rol);
        usuario.setRoles(rolesActuales);
        usuarioRepositorio.save(usuario);

        return true;
    }

    @Override
    public boolean quitarRol(Long id, String rol) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        Set<String> rolesActuales = usuario.getRoles() != null ? usuario.getRoles() : new HashSet<>();

        if (rolesActuales.isEmpty() || !rolesActuales.contains(rol)) {
            return false; // No tiene el rol
        }

        // Quitar el rol
        rolesActuales.remove(rol);

        // Evitar quitar el último rol
        if (rolesActuales.isEmpty()) {
            return false;
        }

        usuario.setRoles(rolesActuales);
        usuarioRepositorio.save(usuario);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneRol(Long id, String rol) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        Set<String> roles = usuario.getRoles();
        return roles != null && roles.contains(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombreOEmail(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String queryLower = query.toLowerCase().trim();
        return usuarioRepositorio.findAll().stream()
                .filter(u -> {
                    String nombreCompleto = (u.getNombre() + " " + u.getApellido()).toLowerCase();
                    String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    return nombreCompleto.contains(queryLower) || email.contains(queryLower);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosActivosDesde(LocalDateTime fechaAcceso) {
        return usuarioRepositorio.findAll().stream()
                .filter(u -> u.isActivo() &&
                        u.getUltimoAcceso() != null &&
                        u.getUltimoAcceso().isAfter(fechaAcceso))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosCreadosEnPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return usuarioRepositorio.findAll().stream()
                .filter(u -> u.getFechaCreacion() != null &&
                        u.getFechaCreacion().isAfter(inicio) &&
                        u.getFechaCreacion().isBefore(fin))
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarUltimoAcceso(Long usuarioId, LocalDateTime fechaAcceso) {
        Optional<Usuario> optUsuario = usuarioRepositorio.findById(usuarioId);
        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            usuario.setUltimoAcceso(fechaAcceso);
            usuarioRepositorio.save(usuario);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasActividad(LocalDateTime inicio, LocalDateTime fin) {
        Map<String, Object> estadisticas = new HashMap<>();

        List<Usuario> todosUsuarios = usuarioRepositorio.findAll();

        // Usuarios totales
        long totalUsuarios = todosUsuarios.size();
        estadisticas.put("totalUsuarios", totalUsuarios);

        // Usuarios activos
        long usuariosActivos = todosUsuarios.stream()
                .filter(Usuario::isActivo)
                .count();
        estadisticas.put("usuariosActivos", usuariosActivos);

        // Usuarios creados en el período
        long usuariosNuevos = todosUsuarios.stream()
                .filter(u -> u.getFechaCreacion() != null &&
                        u.getFechaCreacion().isAfter(inicio) &&
                        u.getFechaCreacion().isBefore(fin))
                .count();
        estadisticas.put("usuariosNuevos", usuariosNuevos);

        // Usuarios con acceso en el período
        long usuariosConAcceso = todosUsuarios.stream()
                .filter(u -> u.getUltimoAcceso() != null &&
                        u.getUltimoAcceso().isAfter(inicio) &&
                        u.getUltimoAcceso().isBefore(fin))
                .count();
        estadisticas.put("usuariosConAcceso", usuariosConAcceso);

        // Distribución por roles
        Map<String, Long> distribucionRoles = todosUsuarios.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRoles() != null && !u.getRoles().isEmpty() ?
                                String.join(",", u.getRoles()) : "SIN_ROL",
                        Collectors.counting()
                ));
        estadisticas.put("distribucionRoles", distribucionRoles);

        // Fechas de análisis
        estadisticas.put("fechaInicio", inicio);
        estadisticas.put("fechaFin", fin);
        estadisticas.put("fechaAnalisis", LocalDateTime.now());

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosInactivos(int diasInactividad) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasInactividad);

        return usuarioRepositorio.findAll().stream()
                .filter(u -> u.getUltimoAcceso() == null ||
                        u.getUltimoAcceso().isBefore(fechaLimite))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsername(String username) {
        return usuarioRepositorio.existsByUsername(username);
    }
}