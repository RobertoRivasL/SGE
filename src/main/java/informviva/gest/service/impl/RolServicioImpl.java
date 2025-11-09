package informviva.gest.service.impl;


import informviva.gest.model.Rol;
import informviva.gest.repository.RolRepositorio;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.service.RolServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Implementación del servicio para la gestión de roles del sistema.
 * Proporciona operaciones CRUD y funcionalidades de negocio para roles.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
@Transactional
public class RolServicioImpl implements RolServicio {

    private static final Logger logger = LoggerFactory.getLogger(RolServicioImpl.class);

    // Constantes para mensajes
    private static final String ROL_NO_ENCONTRADO = "Rol no encontrado con ID: ";
    private static final String ROL_CON_USUARIOS = "No se puede eliminar el rol porque tiene usuarios asociados";
    private static final String ROL_NULO = "El rol no puede ser nulo";
    private static final String NOMBRE_REQUERIDO = "El nombre del rol es requerido";

    private final RolRepositorio rolRepositorio;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public RolServicioImpl(RolRepositorio rolRepositorio,
                           RepositorioUsuario repositorioUsuario) {
        this.rolRepositorio = rolRepositorio;
        this.repositorioUsuario = repositorioUsuario;
    }

    // ===================== MÉTODOS DE CONSULTA =====================

    @Override
    @Cacheable(value = "roles", key = "'todos'")
    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        logger.debug("Listando todos los roles");
        return rolRepositorio.findAll();
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    @Transactional(readOnly = true)
    public Rol buscarPorId(Long id) {
        logger.debug("Buscando rol por ID: {}", id);
        validarId(id);

        return rolRepositorio.findById(id)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol buscarPorNombre(String nombre) {
        logger.debug("Buscando rol por nombre: {}", nombre);
        if (!nombreValido(nombre)) {
            return null;
        }

        return rolRepositorio.findByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerPermisosPorRol(Long rolId) {
        logger.debug("Obteniendo permisos para rol ID: {}", rolId);
        Rol rol = buscarPorId(rolId);

        if (rol != null && rol.getPermisos() != null) {
            return List.copyOf(rol.getPermisos());
        }

        return List.of();
    }

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public Rol guardar(Rol rol) {
        logger.debug("Guardando rol: {}", rol != null ? rol.getNombre() : "null");

        validarRol(rol);

        // Validar nombre único
        if (rol.getId() == null && existePorNombre(rol.getNombre())) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rol.getNombre());
        }

        // Normalizar datos
        rol.setNombre(rol.getNombre().trim().toUpperCase());

        try {
            Rol rolGuardado = rolRepositorio.save(rol);
            logger.info("Rol guardado exitosamente con ID: {}", rolGuardado.getId());
            return rolGuardado;
        } catch (Exception e) {
            logger.error("Error al guardar rol: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el rol", e);
        }
    }

    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public boolean eliminar(Long id) {
        logger.debug("Eliminando rol ID: {}", id);
        validarId(id);

        try {
            if (!rolRepositorio.existsById(id)) {
                logger.warn("Intento de eliminar rol inexistente: {}", id);
                return false;
            }

            // Verificar si el rol tiene usuarios asociados
            if (!puedeSerEliminado(id)) {
                logger.warn("Intento de eliminar rol con usuarios asociados: {}", id);
                throw new IllegalStateException(ROL_CON_USUARIOS);
            }

            rolRepositorio.deleteById(id);
            logger.info("Rol eliminado exitosamente: {}", id);
            return true;

        } catch (Exception e) {
            logger.error("Error al eliminar rol {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public void actualizarPermisos(Long rolId, List<String> permisos) {
        logger.debug("Actualizando permisos para rol ID: {}", rolId);

        Rol rol = buscarPorId(rolId);
        if (rol != null) {
            if (permisos != null) {
                rol.setPermisos(new HashSet<>(permisos));
            } else {
                rol.setPermisos(new HashSet<>());
            }

            rolRepositorio.save(rol);
            logger.info("Permisos actualizados para rol ID: {}", rolId);
        } else {
            logger.warn("Intento de actualizar permisos de rol inexistente: {}", rolId);
        }
    }

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        if (!nombreValido(nombre)) {
            return false;
        }
        return rolRepositorio.findByNombre(nombre.trim().toUpperCase()) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeSerEliminado(Long id) {
        validarId(id);

        // Verificar si hay usuarios con este rol
        // Primero obtener el rol para obtener su nombre
        Rol rol = buscarPorId(id);
        if (rol == null) {
            return true; // Si el rol no existe, puede ser "eliminado"
        }

        // Contar usuarios que tienen este rol (por nombre)
        return repositorioUsuario.countByRolNombre(rol.getNombre()) == 0;
    }

    // ===================== MÉTODOS DE CONTEO =====================

    @Override
    @Transactional(readOnly = true)
    public Long contarTodos() {
        return rolRepositorio.count();
    }

    // ===================== MÉTODOS DE CONFIGURACIÓN =====================

    @Override
    @Transactional(readOnly = true)
    public List<String> listarTodosLosPermisos() {
        logger.debug("Obteniendo lista completa de permisos disponibles");

        // Lista de permisos disponibles en la aplicación
        return Arrays.asList(
                // Permisos de usuarios
                "CREAR_USUARIO", "EDITAR_USUARIO", "VER_USUARIO", "ELIMINAR_USUARIO",

                // Permisos de productos
                "CREAR_PRODUCTO", "EDITAR_PRODUCTO", "VER_PRODUCTO", "ELIMINAR_PRODUCTO",
                "GESTIONAR_STOCK", "VER_PRODUCTOS_BAJO_STOCK",

                // Permisos de ventas
                "CREAR_VENTA", "EDITAR_VENTA", "VER_VENTA", "ANULAR_VENTA",
                "APLICAR_DESCUENTOS", "VER_HISTORIAL_VENTAS",

                // Permisos de clientes
                "CREAR_CLIENTE", "EDITAR_CLIENTE", "VER_CLIENTE", "ELIMINAR_CLIENTE",
                "IMPORTAR_CLIENTES", "EXPORTAR_CLIENTES",

                // Permisos de categorías
                "CREAR_CATEGORIA", "EDITAR_CATEGORIA", "VER_CATEGORIA", "ELIMINAR_CATEGORIA",

                // Permisos de reportes
                "VER_REPORTES", "EXPORTAR_REPORTES", "VER_DASHBOARD",
                "VER_ESTADISTICAS_VENTAS", "VER_ESTADISTICAS_PRODUCTOS",

                // Permisos de administración
                "CONFIGURACION_SISTEMA", "GESTIONAR_ROLES", "GESTIONAR_PERMISOS",
                "VER_LOGS_SISTEMA", "REALIZAR_RESPALDOS",

                // Permisos especiales
                "ACCESO_TOTAL", "SOLO_LECTURA"
        );
    }

    // ===================== MÉTODOS PRIVADOS DE VALIDACIÓN =====================

    /**
     * Valida que el ID sea válido
     */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del rol debe ser mayor a 0");
        }
    }

    /**
     * Valida que el rol tenga los datos requeridos
     */
    private void validarRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException(ROL_NULO);
        }

        if (!nombreValido(rol.getNombre())) {
            throw new IllegalArgumentException(NOMBRE_REQUERIDO);
        }
    }

    /**
     * Valida que el nombre sea válido (no nulo ni vacío)
     */
    private boolean nombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }
}