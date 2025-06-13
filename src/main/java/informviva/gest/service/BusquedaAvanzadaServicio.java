package informviva.gest.service;

// =================== SERVICIO DE BÚSQUEDA AVANZADA ===================

import informviva.gest.dto.BusquedaAvanzadaDTO;
import informviva.gest.dto.ResultadoBusquedaDTO;
import informviva.gest.model.*;
import informviva.gest.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusquedaAvanzadaServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;

    public BusquedaAvanzadaServicio(ProductoRepositorio productoRepositorio,
                                    ClienteRepositorio clienteRepositorio,
                                    VentaRepositorio ventaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    public Page<Producto> buscarProductosAvanzado(BusquedaAvanzadaDTO filtros, Pageable pageable) {
        Specification<Producto> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getTexto() != null && !filtros.getTexto().trim().isEmpty()) {
                String texto = "%" + filtros.getTexto().toLowerCase() + "%";
                Predicate nombrePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")), texto);
                Predicate codigoPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("codigo")), texto);
                predicates.add(criteriaBuilder.or(nombrePredicate, codigoPredicate));
            }

            if (filtros.getCategoriaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoria").get("id"), filtros.getCategoriaId()));
            }

            if (filtros.getPrecioMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precio"), filtros.getPrecioMin()));
            }

            if (filtros.getPrecioMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precio"), filtros.getPrecioMax()));
            }

            if (filtros.getStockMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stock"), filtros.getStockMin()));
            }

            if (filtros.getSoloActivos() != null && filtros.getSoloActivos()) {
                predicates.add(criteriaBuilder.isTrue(root.get("activo")));
            }

            if (filtros.getSoloConStock() != null && filtros.getSoloConStock()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productoRepositorio.findAll(spec, pageable);
    }

    public Page<Cliente> buscarClientesAvanzado(BusquedaAvanzadaDTO filtros, Pageable pageable) {
        Specification<Cliente> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getTexto() != null && !filtros.getTexto().trim().isEmpty()) {
                String texto = "%" + filtros.getTexto().toLowerCase() + "%";
                Predicate nombrePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")), texto);
                Predicate apellidoPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("apellido")), texto);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), texto);
                predicates.add(criteriaBuilder.or(nombrePredicate, apellidoPredicate, emailPredicate));
            }

            if (filtros.getFechaInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaRegistro"), filtros.getFechaInicio()));
            }

            if (filtros.getFechaFin() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaRegistro"), filtros.getFechaFin()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return clienteRepositorio.findAll(spec, pageable);
    }

    public ResultadoBusquedaDTO busquedaGlobal(String texto) {
        ResultadoBusquedaDTO resultado = new ResultadoBusquedaDTO();

        if (texto != null && !texto.trim().isEmpty()) {
            // Buscar en productos
            List<Producto> productos = productoRepositorio.buscarPorTexto(texto);
            resultado.setProductos(productos.subList(0, Math.min(5, productos.size())));

            // Buscar en clientes
            List<Cliente> clientes = clienteRepositorio.buscarPorTexto(texto);
            resultado.setClientes(clientes.subList(0, Math.min(5, clientes.size())));

            // Establecer totales
            resultado.setTotalProductos(productos.size());
            resultado.setTotalClientes(clientes.size());
        }

        return resultado;
    }
}
