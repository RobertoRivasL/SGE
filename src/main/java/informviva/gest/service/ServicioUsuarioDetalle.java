package informviva.gest.service;

import informviva.gest.model.Usuario;
import informviva.gest.repository.RepositorioUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Servicio para cargar detalles de usuario para Spring Security
 *
 * @author Roberto Rivas
 * @version 1.1 - CORREGIDO
 */
@Service
public class ServicioUsuarioDetalle implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    /**
     * Constructor para inyección de dependencias
     * @param repositorioUsuario Repositorio de usuarios
     */
    public ServicioUsuarioDetalle(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Actualizar la fecha de último acceso
        actualizarUltimoAcceso(usuario.getId());

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isActivo(),
                true,              // accountNonExpired
                true,              // credentialsNonExpired
                true,              // accountNonLocked
                getAuthorities(usuario)
        );
    }

    /**
     * Convierte los roles del usuario en GrantedAuthority
     * @param usuario Usuario
     * @return Colección de GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        // Convertir roles a GrantedAuthority con el prefijo ROLE_
        return usuario.getRoles().stream()
                .map(rol -> "ROLE_" + rol)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la fecha de último acceso del usuario
     * @param userId ID del usuario
     */
    @Transactional
    public void actualizarUltimoAcceso(Long userId) {
        repositorioUsuario.findById(userId).ifPresent(usuario -> {
            usuario.setUltimoAcceso(LocalDateTime.now());
            repositorioUsuario.save(usuario);
        });
    }
}