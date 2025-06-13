package informviva.gest.seguridad;

import informviva.gest.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UsuarioDetalles implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetalles(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Los roles del usuario se almacenan como un Set<String>, por lo que usamos un stream para convertirlo en un conjunto de GrantedAuthority
        return usuario.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes agregar lógica personalizada si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.isActivo(); // Se utiliza el estado de la cuenta del usuario
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes agregar lógica personalizada si es necesario
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo(); // Usamos la propiedad 'activo' para determinar si el usuario está habilitado
    }
}
