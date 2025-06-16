package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.*;
import informviva.gest.repository.*;
import informviva.gest.service.VentaServicio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServicioImpl implements VentaServicio {

    private final VentaRepositorio ventaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final VentaDetalleRepositorio ventaDetalleRepositorio; // Corregido

    @Override
    @Transactional
    public Venta crearNuevaVenta(VentaDTO ventaDTO) {
        Cliente cliente = clienteRepositorio.findById(ventaDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + ventaDTO.getClienteId()));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFecha(LocalDateTime.now());

        Map<Long, Integer> productosCantidadMap = ventaDTO.getProductos().stream()
                .collect(Collectors.toMap(VentaDTO.ProductoVentaDTO::getProductoId, VentaDTO.ProductoVentaDTO::getCantidad));

        List<Producto> productosEnVenta = productoRepositorio.findAllById(productosCantidadMap.keySet());

        if (productosEnVenta.size() != productosCantidadMap.size()) {
            throw new RuntimeException("Alguno de los productos no fue encontrado.");
        }

        double totalVenta = 0;
        List<VentaDetalle> detalles = new ArrayList<>(); // Corregido

        for (Producto producto : productosEnVenta) {
            int cantidadVendida = productosCantidadMap.get(producto.getId());

            if (producto.getStock() < cantidadVendida) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - cantidadVendida);

            VentaDetalle detalle = new VentaDetalle(); // Corregido
            detalle.setVenta(nuevaVenta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadVendida);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);

            totalVenta += producto.getPrecio() * cantidadVendida;
        }

        nuevaVenta.setTotal(totalVenta);
        nuevaVenta.setDetalles(detalles);

        return ventaRepositorio.save(nuevaVenta);
    }

    @Override
    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepositorio.findAll();
    }

    @Override
    public Venta obtenerVentaPorId(Long id) {
        return ventaRepositorio.findById(id).orElse(null);
    }

    @Override
    public List<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    public Long contarTransacciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepositorio.countByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    public Long contarVentasPorCliente(Long clienteId) {
        return ventaRepositorio.countByClienteId(clienteId);
    }

    @Override
    public Double calcularTotalVentasPorCliente(Long clienteId) {
        Double total = ventaRepositorio.sumTotalByClienteId(clienteId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin) {
        Double total = ventaRepositorio.sumTotalByFechaBetween(inicio, fin);
        return total != null ? total : 0.0;
    }

    @Override
    public Double calcularPorcentajeCambio(Double actual, Double anterior) {
        if (anterior == null || anterior == 0) {
            return null;
        }
        return ((actual - anterior) / anterior) * 100;
    }

    @Override
    public boolean existenVentasPorCliente(Long clienteId) {
        return ventaRepositorio.existsByClienteId(clienteId);
    }

    @Override
    public Long contarArticulosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Long total = ventaDetalleRepositorio.sumCantidadByFechaBetween(fechaInicio, fechaFin);
        return total != null ? total : 0L;
    }

    @Override
    public boolean existenVentasPorProducto(Long productoId) {
        return ventaDetalleRepositorio.existsByProductoId(productoId);
    }

    @Override
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        return 0L;
    }

    @Override
    public Double calcularIngresosPorProducto(Long productoId) {
        return 0.0;
    }

    @Override
    public List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        return Collections.emptyList();
    }

    @Override
    public List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite) {
        return Collections.emptyList();
    }

    @Override
    public List<Venta> buscarPorCliente(Cliente cliente) {
        return Collections.emptyList();
    }

    @Override
    public Double calcularTicketPromedio(LocalDateTime inicio, LocalDateTime fin) {
        return 0.0;
    }
}