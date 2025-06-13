package informviva.gest.service.impl;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.Usuario;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.service.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de usuarios
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class UsuarioServicioImpl implements UsuarioServicio {

    private final RepositorioUsuario repositorioUsuario;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServicioImpl(RepositorioUsuario repositorioUsuario, BCryptPasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return repositorioUsuario.findById(id).orElse(null);
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        return repositorioUsuario.findByUsername(username).orElse(null);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        // Si findByEmail devuelve Optional<Usuario>
        return repositorioUsuario.findByEmail(email).orElse(null);

        // Si findByEmail devuelve Usuario directamente (sin Optional)
        // return repositorioUsuario.findByEmail(email);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        // Si es un usuario nuevo (sin ID), encriptar la contraseña
        if (usuario.getId() == null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setUltimoAcceso(LocalDate.now());

            // Asegurar que existe la colección de roles
            if (usuario.getRoles() == null) {
                usuario.setRoles(new HashSet<>());
            }

            // Si no tiene roles, asignar el rol USER por defecto
            if (usuario.getRoles().isEmpty()) {
                usuario.getRoles().add("USER");
            }
        }

        return repositorioUsuario.save(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorioUsuario.deleteById(id);
    }

    @Override
    public List<Usuario> listarTodos() {
        // Si RepositorioUsuario extiende JpaRepository, findAll() devuelve List<Usuario>
        return repositorioUsuario.findAll();

        // Si RepositorioUsuario extiende CrudRepository, findAll() devuelve Iterable<Usuario>
        // y necesitamos convertirlo manualmente:
        // List<Usuario> usuarios = new ArrayList<>();
        // repositorioUsuario.findAll().forEach(usuarios::add);
        // return usuarios;
    }

    @Override
    public List<Usuario> listarVendedores() {
        return listarTodos().stream()
                .filter(u -> u.isActivo() && u.getRoles().contains("VENTAS"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean cambiarPassword(Long id, String nuevaPassword) {
        Optional<Usuario> optUsuario = repositorioUsuario.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        repositorioUsuario.save(usuario);

        return true;
    }

    @Override
    @Transactional
    public boolean cambiarEstado(Long id, boolean activo) {
        Optional<Usuario> optUsuario = repositorioUsuario.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        usuario.setActivo(activo);
        repositorioUsuario.save(usuario);

        return true;
    }

    @Override
    @Transactional
    public boolean agregarRol(Long id, String rol) {
        Optional<Usuario> optUsuario = repositorioUsuario.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        usuario.getRoles().add(rol);
        repositorioUsuario.save(usuario);

        return true;
    }

    @Override
    @Transactional
    public boolean quitarRol(Long id, String rol) {
        Optional<Usuario> optUsuario = repositorioUsuario.findById(id);
        if (!optUsuario.isPresent()) {
            return false;
        }

        Usuario usuario = optUsuario.get();
        // Evitar quitar el último rol
        if (usuario.getRoles().size() <= 1) {
            return false;
        }

        boolean resultado = usuario.getRoles().remove(rol);
        if (resultado) {
            repositorioUsuario.save(usuario);
        }

        return resultado;
    }
}