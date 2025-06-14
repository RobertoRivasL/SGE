package informviva.gest.service.impl;

import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Categoria;
import informviva.gest.repository.CategoriaRepositorio;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.CategoriaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio para la gestión de categorías.
 * Proporciona operaciones CRUD y validaciones de negocio para las categorías.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
@Transactional
public class CategoriaServicioImpl implements CategoriaServicio {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServicioImpl.class);

    // Constantes para mensajes de error
    private static final String CATEGORIA_NO_ENCONTRADA = "Categoría no encontrada con ID: ";
    private static final String NOMBRE_YA_EXISTE = "Ya existe una categoría con el nombre: ";
    private static final String CATEGORIA_CON_PRODUCTOS = "No se puede eliminar la categoría porque tiene productos asociados";
    private static final String CATEGORIA_NULA = "La categoría no puede ser nula";
    private static final String NOMBRE_REQUERIDO = "El nombre de la categoría es requerido";
    private static final String ID_INVALIDO = "El ID de la categoría debe ser mayor a 0";

    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;

    @Autowired
    public CategoriaServicioImpl(CategoriaRepositorio categoriaRepositorio,
                                 ProductoRepositorio productoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    // ===================== MÉTODOS DE CONSULTA =====================

    @Override
    @Cacheable(value = "categorias", key = "'todas'")
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        logger.debug("Listando todas las categorías");
        return categoriaRepositorio.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return listarTodas();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarPaginadas(Pageable pageable) {
        logger.debug("Listando categorías paginadas: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return categoriaRepositorio.findAll(pageable);
    }

    @Override
    @Cacheable(value = "categorias", key = "#id")
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        validarId(id);
        logger.debug("Buscando categoría por ID: {}", id);
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Categoría no encontrada con ID: {}", id);
                    return new RecursoNoEncontradoException(CATEGORIA_NO_ENCONTRADA + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorNombre(String nombre) {
        logger.debug("Buscando categoría por nombre: {}", nombre);
        if (!nombreValido(nombre)) {
            return null;
        }
        return categoriaRepositorio.findByNombre(nombre.trim())
                .orElse(null);
    }

    @Override
    @Cacheable(value = "categorias", key = "'activas'")
    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        logger.debug("Listando categorías activas");
        return categoriaRepositorio.findByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarActivasPaginadas(Pageable pageable) {
        logger.debug("Listando categorías activas paginadas");
        return categoriaRepositorio.findByActivaTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarPorTexto(String texto) {
        logger.debug("Buscando categorías por texto: {}", texto);
        if (!nombreValido(texto)) {
            return List.of();
        }
        return categoriaRepositorio.findByNombreContainingIgnoreCase(texto.trim());
    }

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria guardar(Categoria categoria) {
        logger.debug("Guardando categoría: {}", categoria != null ? categoria.getNombre() : "null");

        validarCategoria(categoria);

        // Validar duplicado para nueva categoría
        if (categoria.getId() == null && existePorNombre(categoria.getNombre())) {
            logger.warn("Intento de crear categoría con nombre duplicado: {}", categoria.getNombre());
            throw new IllegalArgumentException(NOMBRE_YA_EXISTE + categoria.getNombre());
        }

        // Validar duplicado para categoría existente
        if (categoria.getId() != null && existePorNombre(categoria.getNombre(), categoria.getId())) {
            logger.warn("Intento de actualizar categoría con nombre duplicado: {}", categoria.getNombre());
            throw new IllegalArgumentException(NOMBRE_YA_EXISTE + categoria.getNombre());
        }

        // Normalizar datos
        categoria.setNombre(categoria.getNombre().trim());
        if (categoria.getActiva() == null) {
            categoria.setActiva(true);
        }

        Categoria categoriaGuardada = categoriaRepositorio.save(categoria);
        logger.info("Categoría guardada exitosamente con ID: {}", categoriaGuardada.getId());

        return categoriaGuardada;
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria actualizar(Long id, Categoria categoria) {
        logger.debug("Actualizando categoría ID: {} con datos: {}", id, categoria.getNombre());

        validarId(id);
        validarCategoria(categoria);

        Categoria existente = buscarPorId(id);

        // Actualizar campos
        existente.setNombre(categoria.getNombre().trim());
        existente.setDescripcion(categoria.getDescripcion());
        existente.setActiva(categoria.getActiva() != null ? categoria.getActiva() : true);

        return guardar(existente);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void eliminar(Long id) {
        logger.debug("Eliminando categoría ID: {}", id);

        validarId(id);

        // Verificar que existe
        Categoria categoria = buscarPorId(id);

        // Verificar que no tenga productos asociados
        if (!puedeSerEliminada(id)) {
            logger.warn("Intento de eliminar categoría con productos asociados. ID: {}", id);
            throw new IllegalStateException(CATEGORIA_CON_PRODUCTOS);
        }

        categoriaRepositorio.deleteById(id);
        logger.info("Categoría eliminada exitosamente. ID: {}", id);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public boolean cambiarEstado(Long id, boolean activa) {
        logger.debug("Cambiando estado de categoría ID: {} a activa: {}", id, activa);

        try {
            Categoria categoria = buscarPorId(id);
            categoria.setActiva(activa);
            categoriaRepositorio.save(categoria);

            logger.info("Estado de categoría cambiado exitosamente. ID: {}, activa: {}", id, activa);
            return true;
        } catch (Exception e) {
            logger.error("Error al cambiar estado de categoría ID: {}", id, e);
            return false;
        }
    }

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        if (!nombreValido(nombre)) {
            return false;
        }
        return categoriaRepositorio.existsByNombre(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre, Long excludeId) {
        if (!nombreValido(nombre) || excludeId == null) {
            return existePorNombre(nombre);
        }
        return categoriaRepositorio.existsByNombreAndIdNot(nombre.trim(), excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeSerEliminada(Long id) {
        validarId(id);
        return productoRepositorio.countByCategoriaId(id) == 0;
    }

    // ===================== MÉTODOS DE CONTEO =====================

    @Override
    @Transactional(readOnly = true)
    public Long contarTodas() {
        return categoriaRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivas() {
        return categoriaRepositorio.countByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarInactivas() {
        return categoriaRepositorio.countByActivaFalse();
    }

    // ===================== MÉTODOS PRIVADOS DE VALIDACIÓN =====================

    /**
     * Valida que el ID sea válido
     */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(ID_INVALIDO);
        }
    }

    /**
     * Valida que la categoría tenga los datos requeridos
     */
    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException(CATEGORIA_NULA);
        }

        if (!nombreValido(categoria.getNombre())) {
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