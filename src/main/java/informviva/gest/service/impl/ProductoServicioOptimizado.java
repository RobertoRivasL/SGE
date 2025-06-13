package informviva.gest.service.impl;


import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.CacheServicio;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoServicioOptimizado implements ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final CacheServicio cacheServicio;

    public ProductoServicioOptimizado(ProductoRepositorio productoRepositorio,
                                      CacheServicio cacheServicio) {
        this.productoRepositorio = productoRepositorio;
        this.cacheServicio = cacheServicio;
    }

    @Override
    @Cacheable(value = "productos", key = "'activos'", unless = "#result.size() == 0")
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepositorio.findByActivoTrue();
    }

    @Override
    @Cacheable(value = "productos", key = "'bajo-stock-' + #umbral")
    @Transactional(readOnly = true)
    public List<Producto> listarConBajoStock(int umbral) {
        return productoRepositorio.findByStockLessThanOrderByStockAsc(umbral);
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto guardar(Producto producto) {
        Producto savedProducto = productoRepositorio.save(producto);
        // Limpiar cache después de guardar
        cacheServicio.limpiarCacheProductos();
        return savedProducto;
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public void eliminar(Long id) {
        productoRepositorio.deleteById(id);
        cacheServicio.limpiarCacheProductos();
    }

    // Implementar otros métodos del servicio...
    // (Los métodos ya implementados en ProductoServicioImpl se mantienen igual)
}