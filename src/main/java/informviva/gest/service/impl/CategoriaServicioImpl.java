package informviva.gest.service.impl;

import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Categoria;
import informviva.gest.repository.CategoriaRepositorio;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.CategoriaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaServicioImpl implements CategoriaServicio {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServicioImpl.class);
    private static final String CATEGORIA_NO_ENCONTRADA = "Categoría no encontrada con ID: ";
    private static final String NOMBRE_YA_EXISTE = "Ya existe una categoría con el nombre: ";
    private static final String CATEGORIA_CON_PRODUCTOS = "No se puede eliminar la categoría porque tiene productos asociados";

    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;

    @Autowired
    public CategoriaServicioImpl(CategoriaRepositorio categoriaRepositorio, ProductoRepositorio productoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepositorio.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarPaginadas(Pageable pageable) {
        return categoriaRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        validarId(id);
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CATEGORIA_NO_ENCONTRADA + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorNombre(String nombre) {
        return nombreValido(nombre)
                ? categoriaRepositorio.findFirstByNombreContainingIgnoreCase(nombre.trim())
                .orElse(null)
                : null;
    }

    // CategoriaServicioImpl.java
    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return categoriaRepositorio.findAll();
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        validarCategoria(categoria);
        if (categoria.getId() == null && existePorNombre(categoria.getNombre())) {
            throw new IllegalArgumentException(NOMBRE_YA_EXISTE + categoria.getNombre());
        }
        if (categoria.getId() != null && existePorNombre(categoria.getNombre(), categoria.getId())) {
            throw new IllegalArgumentException(NOMBRE_YA_EXISTE + categoria.getNombre());
        }
        categoria.setNombre(categoria.getNombre().trim());
        if (categoria.getActiva() == null) categoria.setActiva(true);
        return categoriaRepositorio.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, Categoria categoria) {
        validarId(id);
        Categoria existente = buscarPorId(id);
        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        existente.setActiva(categoria.getActiva());
        return guardar(existente);
    }

    @Override
    public void eliminar(Long id) {
        validarId(id);
        if (!puedeSerEliminada(id)) {
            throw new IllegalStateException(CATEGORIA_CON_PRODUCTOS);
        }
        categoriaRepositorio.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return nombreValido(nombre) && categoriaRepositorio.existsByNombre(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre, Long excludeId) {
        return nombreValido(nombre) && excludeId != null
                ? categoriaRepositorio.existsByNombreAndIdNot(nombre.trim(), excludeId)
                : existePorNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepositorio.findByActivaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarActivasPaginadas(Pageable pageable) {
        return categoriaRepositorio.findByActivaTrue(pageable);
    }

    @Override
    public boolean cambiarEstado(Long id, boolean activa) {
        Categoria categoria = buscarPorId(id);
        categoria.setActiva(activa);
        categoriaRepositorio.save(categoria);
        return true;
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarPorTexto(String texto) {
        return nombreValido(texto) ? categoriaRepositorio.buscarPorTexto(texto.trim()) : List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> buscarPorTextoPaginado(String texto, Pageable pageable) {
        if (!nombreValido(texto)) {
            throw new IllegalArgumentException("El texto de búsqueda no puede estar vacío");
        }
        return categoriaRepositorio.buscarPorTexto(texto.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerMasUtilizadas(int limite) {
        return categoriaRepositorio.findCategoriasMasUtilizadas(limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAllActivas() {
        return listarActivas();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeSerEliminada(Long id) {
        return productoRepositorio.countByCategoriaId(id) == 0;
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null || !nombreValido(categoria.getNombre())) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede estar vacío");
        }
        if (categoria.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre de la categoría no puede superar los 100 caracteres");
        }
        if (categoria.getDescripcion() != null && categoria.getDescripcion().length() > 500) {
            throw new IllegalArgumentException("La descripción no puede superar los 500 caracteres");
        }
    }

    private void validarId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
    }

    private boolean nombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }

    @Override
    @Transactional
    public Categoria save(Categoria categoria) {
        return categoriaRepositorio.save(categoria);
    }
}