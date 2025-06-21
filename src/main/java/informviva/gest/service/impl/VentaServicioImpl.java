package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.*;
import informviva.gest.repository.*;
import informviva.gest.service.VentaServicio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServicioImpl implements VentaServicio {

    private final VentaRepositorio ventaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final VentaDetalleRepositorio ventaDetalleRepositorio;

    // ==================== MÉTODOS PRINCIPALES ====================

    @Override
    @Transactional
    public Venta crearNuevaVenta(VentaDTO ventaDTO) {
        Cliente cliente = clienteRepositorio.findById(ventaDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + ventaDTO.getClienteId()));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFecha(LocalDateTime.now());

        if (ventaDTO.getProductos() != null && !ventaDTO.getProductos().isEmpty()) {
            Map<Long, Integer> productosCantidadMap = ventaDTO.getProductos().stream()
                    .collect(Collectors.toMap(VentaDTO.ProductoVentaDTO::getProductoId, VentaDTO.ProductoVentaDTO::getCantidad));

            List<Producto> productosEnVenta = productoRepositorio.findAllById(productosCantidadMap.keySet());

            if (productosEnVenta.size() != productosCantidadMap.size()) {
                throw new RuntimeException("Alguno de los productos no fue encontrado.");
            }

            double totalVenta = 0;
            List<VentaDetalle> detalles = new ArrayList<>();

            for (Producto producto : productosEnVenta) {
                int cantidadVendida = productosCantidadMap.get(producto.getId());

                if (producto.getStock() < cantidadVendida) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                producto.setStock(producto.getStock() - cantidadVendida);

                VentaDetalle detalle = new VentaDetalle();
                detalle.setVenta(nuevaVenta);
                detalle.setProducto(producto);
                detalle.setCantidad(cantidadVendida);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalles.add(detalle);

                totalVenta += producto.getPrecio() * cantidadVendida;
            }

            nuevaVenta.setTotal(totalVenta);
            nuevaVenta.setDetalles(detalles);
        }

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

    // ==================== MÉTODOS DE BÚSQUEDA ====================

    @Override
    public List<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Page<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        try {
            return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }

    @Override
    public List<Venta> buscarPorCliente(Long clienteId) {
        try {
            return ventaRepositorio.findByClienteId(clienteId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Venta> buscarPorCliente(Cliente cliente) {
        if (cliente != null && cliente.getId() != null) {
            return buscarPorCliente(cliente.getId());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Venta> buscarPorVendedorYFechas(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return ventaRepositorio.findByVendedorIdAndFechaBetween(vendedorId, fechaInicio, fechaFin);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Venta> buscarVentasParaExportar(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                String estado, String metodoPago, String vendedor) {
        List<Venta> ventas = buscarPorRangoFechas(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> estado == null || estado.isEmpty() || estado.equals(venta.getEstado()))
                .filter(venta -> metodoPago == null || metodoPago.isEmpty() || metodoPago.equals(venta.getMetodoPago()))
                .filter(venta -> vendedor == null || vendedor.isEmpty() ||
                        (venta.getVendedor() != null && vendedor.equals(venta.getVendedor().getNombreCompleto())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        // Implementación básica - mejorar según necesidades
        return new ArrayList<>();
    }

    @Override
    public List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite) {
        // Implementación básica - mejorar según necesidades
        return new ArrayList<>();
    }

    // ==================== MÉTODOS CRUD ====================

    @Override
    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepositorio.findById(id);
    }

    @Override
    public List<Venta> listarTodas() {
        return obtenerTodasLasVentas();
    }

    @Override
    public Venta guardar(Venta venta) {
        if (venta.getFecha() == null) {
            venta.setFecha(LocalDateTime.now());
        }
        return ventaRepositorio.save(venta);
    }

    @Override
    @Transactional
    public Venta guardar(VentaDTO ventaDTO) {
        return crearNuevaVenta(ventaDTO);
    }

    @Override
    public Venta actualizar(Venta venta) {
        return ventaRepositorio.save(venta);
    }

    @Override
    @Transactional
    public Venta actualizar(Long id, VentaDTO ventaDTO) {
        Venta ventaExistente = ventaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        // Actualizar campos básicos de manera segura
        if (ventaDTO.getSubtotal() != null) {
            ventaExistente.setSubtotal(ventaDTO.getSubtotal());
        }
        if (ventaDTO.getImpuesto() != null) {
            ventaExistente.setImpuesto(ventaDTO.getImpuesto());
        }
        if (ventaDTO.getTotal() != null) {
            ventaExistente.setTotal(ventaDTO.getTotal());
        }
        if (ventaDTO.getMetodoPago() != null) {
            ventaExistente.setMetodoPago(ventaDTO.getMetodoPago());
        }
        if (ventaDTO.getEstado() != null) {
            ventaExistente.setEstado(ventaDTO.getEstado());
        }
        if (ventaDTO.getObservaciones() != null) {
            ventaExistente.setObservaciones(ventaDTO.getObservaciones());
        }

        return ventaRepositorio.save(ventaExistente);
    }

    @Override
    public void eliminar(Long id) {
        ventaRepositorio.deleteById(id);
    }

    @Override
    @Transactional
    public Venta anular(Long id) {
        Venta venta = ventaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        venta.setEstado("ANULADA");
        return ventaRepositorio.save(venta);
    }

    @Override
    @Transactional
    public Venta duplicarVenta(Venta ventaOriginal) {
        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(ventaOriginal.getCliente());
        nuevaVenta.setVendedor(ventaOriginal.getVendedor());
        nuevaVenta.setSubtotal(ventaOriginal.getSubtotal());
        nuevaVenta.setImpuesto(ventaOriginal.getImpuesto());
        nuevaVenta.setTotal(ventaOriginal.getTotal());
        nuevaVenta.setMetodoPago(ventaOriginal.getMetodoPago());
        nuevaVenta.setEstado("PENDIENTE");
        nuevaVenta.setFecha(LocalDateTime.now());

        return ventaRepositorio.save(nuevaVenta);
    }

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    @Override
    public VentaDTO convertirADTO(Venta venta) {
        VentaDTO dto = new VentaDTO();

        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setClienteId(venta.getCliente() != null ? venta.getCliente().getId() : null);
        dto.setVendedorId(venta.getVendedor() != null ? venta.getVendedor().getId() : null);

        // Manejo seguro de valores null
        dto.setSubtotal(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0);
        dto.setImpuesto(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0);
        dto.setTotal(venta.getTotal() != null ? venta.getTotal() : 0.0);

        dto.setMetodoPago(venta.getMetodoPago());
        dto.setEstado(venta.getEstado());
        dto.setObservaciones(venta.getObservaciones());

        // Convertir detalles si existen
        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            List<VentaDTO.ProductoVentaDTO> productosDTO = venta.getDetalles().stream()
                    .map(detalle -> {
                        VentaDTO.ProductoVentaDTO productoDTO = new VentaDTO.ProductoVentaDTO();
                        productoDTO.setProductoId(detalle.getProducto().getId());
                        productoDTO.setCantidad(detalle.getCantidad());
                        productoDTO.setPrecioUnitario(detalle.getPrecioUnitario());
                        return productoDTO;
                    })
                    .collect(Collectors.toList());
            dto.setProductos(productosDTO);
        }

        return dto;
    }

    // ==================== MÉTODOS DE CÁLCULO Y ESTADÍSTICAS ====================

    @Override
    public Long contarTransacciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return ventaRepositorio.countByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public Long contarVentasPorCliente(Long clienteId) {
        try {
            return ventaRepositorio.countByClienteId(clienteId);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public Long contarVentasHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1).minusNanos(1);
        return contarTransacciones(inicioHoy, finHoy);
    }

    @Override
    public Double calcularTotalVentasPorCliente(Long clienteId) {
        try {
            Double total = ventaRepositorio.sumTotalByClienteId(clienteId);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin) {
        try {
            Double total = ventaRepositorio.sumTotalByFechaBetween(inicio, fin);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public Double calcularPorcentajeCambio(Double actual, Double anterior) {
        if (anterior == null || anterior == 0) {
            return null;
        }
        return ((actual - anterior) / anterior) * 100;
    }

    @Override
    public Double calcularTicketPromedio(LocalDateTime inicio, LocalDateTime fin) {
        Double totalVentas = calcularTotalVentas(inicio, fin);
        Long totalTransacciones = contarTransacciones(inicio, fin);

        if (totalTransacciones > 0) {
            return totalVentas / totalTransacciones;
        }
        return 0.0;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    @Override
    public boolean existenVentasPorCliente(Long clienteId) {
        try {
            return ventaRepositorio.existsByClienteId(clienteId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean existenVentasPorProducto(Long productoId) {
        try {
            return ventaDetalleRepositorio.existsByProductoId(productoId);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== MÉTODOS DE ARTÍCULOS Y PRODUCTOS ====================

    @Override
    public Long contarArticulosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            Long total = ventaDetalleRepositorio.sumCantidadByFechaBetween(fechaInicio, fechaFin);
            return total != null ? total : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        // Implementación básica - mejorar según necesidades
        return 0L;
    }

    @Override
    public Double calcularIngresosPorProducto(Long productoId) {
        // Implementación básica - mejorar según necesidades
        return 0.0;
    }
}