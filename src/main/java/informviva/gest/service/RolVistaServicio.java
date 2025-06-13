package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.RolVista;
import informviva.gest.model.Usuario;
import informviva.gest.repository.RolVistaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RolVistaServicio {

    @Autowired
    private RolVistaRepositorio rolVistaRepositorio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    public List<RolVista> obtenerUltimasVistas() {
        return rolVistaRepositorio.findTop10ByOrderByFechaVistaDesc();
    }

    public List<RolVista> obtenerVistasPorRol(String rolNombre) {
        return rolVistaRepositorio.findByRolNombreOrderByFechaVistaDesc(rolNombre);
    }

    public void registrarVistaRol(String rolNombre) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            Usuario usuario = usuarioServicio.buscarPorUsername(username);

            if (usuario != null) {
                RolVista vista = new RolVista();
                vista.setRolNombre(rolNombre);
                vista.setUsuarioId(usuario.getId());
                vista.setUsername(username);
                vista.setFechaVista(new Date());

                rolVistaRepositorio.save(vista);
            }
        }
    }
}
