package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.Rol;
import informviva.gest.repository.RolRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
public class RolServicio {

    private final RolRepositorio rolRepositorio;

    @Autowired
    public RolServicio(RolRepositorio rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    public List<Rol> listarTodos() {
        return rolRepositorio.findAll();
    }

    public Rol buscarPorId(Long id) {
        return rolRepositorio.findById(id).orElse(null);
    }

    public Rol buscarPorNombre(String nombre) {
        return rolRepositorio.findByNombre(nombre);
    }

    @Transactional
    public Rol guardar(Rol rol) {
        return rolRepositorio.save(rol);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (rolRepositorio.existsById(id)) {
            // Aquí puedes agregar validación para verificar si el rol está en uso
            rolRepositorio.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void actualizarPermisos(Long rolId, List<String> permisos) {
        Rol rol = buscarPorId(rolId);
        if (rol != null) {
            if (permisos != null) {
                rol.setPermisos(new HashSet<>(permisos));
            } else {
                rol.setPermisos(new HashSet<>());
            }
            rolRepositorio.save(rol);
        }
    }

    public List<String> listarTodosLosPermisos() {
        // Lista de permisos disponibles en la aplicación
        return Arrays.asList(
                "CREAR_USUARIO", "EDITAR_USUARIO", "VER_USUARIO", "ELIMINAR_USUARIO",
                "CREAR_PRODUCTO", "EDITAR_PRODUCTO", "VER_PRODUCTO", "ELIMINAR_PRODUCTO",
                "CREAR_VENTA", "EDITAR_VENTA", "VER_VENTA", "ANULAR_VENTA",
                "CREAR_CLIENTE", "EDITAR_CLIENTE", "VER_CLIENTE", "ELIMINAR_CLIENTE",
                "VER_REPORTES", "EXPORTAR_REPORTES",
                "CONFIGURACION_SISTEMA"
        );
    }
}