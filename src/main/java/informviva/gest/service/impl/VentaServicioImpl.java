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
}