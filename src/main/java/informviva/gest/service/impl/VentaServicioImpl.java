package informviva.gest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.model.VentaDetalle;
import informviva.gest.repository.VentaDetalleRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.VentaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VentaServicioImpl implements VentaServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaServicioImpl.class);
    private static final String ESTADO_ANULADA = "ANULADA";
    private static final double CIEN_PORCIENTO = 100.0;
    private static final double CERO_PORCIENTO = 0.0;

    private final VentaRepositorio ventaRepositorio;
    private final VentaDetalleRepositorio ventaDetalleRepositorio;
    private final ObjectMapper objectMapper;

    @Autowired
    public VentaServicioImpl(VentaRepositorio ventaRepositorio,
                             VentaDetalleRepositorio ventaDetalleRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.ventaDetalleRepositorio = ventaDetalleRepositorio;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public List<Venta> listarTodas() {
        return ventaRepositorio.findAll();
    }

    @Override
    public Page<Venta> listarPaginadas(Pageable pageable) {
        return ventaRepositorio.findAll(pageable);
    }

    @Override
    public Venta buscarPorId(Long id) {
        return ventaRepositorio.findById(id).orElse(null);
    }

    @Override
    public Venta guardar(VentaDTO ventaDTO) {
        Venta venta = convertirAEntidad(ventaDTO);
        return ventaRepositorio.save(venta);
    }

    @Override
    public Venta actualizar(Long id, VentaDTO ventaDTO) {
        Venta venta = buscarPorId(id);
        if (venta != null) {
            // Actualizar campos de la venta
            return ventaRepositorio.save(venta);
        }
        return null;
    }

    @Override
    public void eliminar(Long id) {
        ventaRepositorio.deleteById(id);
    }

    @Override
    public Venta anular(Long id) {
        Venta venta = buscarPorId(id);
        if (venta != null) {
            venta.setEstado(ESTADO_ANULADA);
            return ventaRepositorio.save(venta);
        }
        return null;
    }

    @Override
    public List<Venta> buscarPorRangoFechas(LocalDate inicio, LocalDate fin) {
        try {
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime finDateTime = fin.atTime(23, 59, 59);
            return ventaRepositorio.findByFechaBetween(inicioDateTime, finDateTime);
        } catch (Exception e) {
            logger.error("Error buscando ventas por rango de fechas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    @Override
    public List<Venta> buscarPorRangoFechasCompleto(LocalDateTime inicio, LocalDateTime fin) {
        // Filtrar ventas por LocalDateTime
        return ventaRepositorio.findAll().stream()
                .filter(v -> !v.getFecha().isBefore(inicio) && !v.getFecha().isAfter(fin))
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> buscarPorCliente(Cliente cliente) {
        return ventaRepositorio.findByCliente(cliente);
    }

    @Override
    public List<Venta> buscarPorVendedor(Usuario vendedor) {
        return ventaRepositorio.findByVendedor(vendedor);
    }

    @Override
    public boolean existenVentasPorCliente(Long clienteId) {
        return ventaRepositorio.existsByClienteId(clienteId);
    }

    @Override
    public boolean existenVentasPorProducto(Long productoId) {
        return ventaRepositorio.existsByDetallesProductoId(productoId);
    }

    @Override
    public VentaDTO convertirADTO(Venta venta) {
        if (venta == null) {
            return null;
        }
        VentaDTO dto = new VentaDTO();
        // Copiar propiedades de venta a dto
        return dto;
    }

    private Venta convertirAEntidad(VentaDTO dto) {
        if (dto == null) {
            return null;
        }
        Venta venta = new Venta();
        // Copiar propiedades de dto a venta
        return venta;
    }

    @Override
    public Double calcularTotalVentas(LocalDate inicio, LocalDate fin) {
        List<Venta> ventas = buscarPorRangoFechas(inicio, fin);
        return ventas.stream()
                .filter(v -> !ESTADO_ANULADA.equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    @Override
    public Double calcularTotalVentas() {
        List<Venta> ventas = listarTodas();
        return ventas.stream()
                .filter(v -> !ESTADO_ANULADA.equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    @Override
    public Long contarTransacciones(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(23, 59, 59);
        return ventaRepositorio.countByFechaBetweenAndEstadoNot(inicioDateTime, finDateTime, ESTADO_ANULADA);
    }

    @Override
    public Long contarArticulosVendidos(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(23, 59, 59);
        return ventaRepositorio.countArticulosVendidosBetweenFechas(inicioDateTime, finDateTime);
    }

    @Override
    public Double calcularTicketPromedio(LocalDate inicio, LocalDate fin) {
        Double totalVentas = calcularTotalVentas(inicio, fin);
        Long totalTransacciones = contarTransacciones(inicio, fin);
        return (totalTransacciones == 0) ? 0.0 : totalVentas / totalTransacciones;
    }

    @Override
    public Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior) {
        if (valorAnterior == null || Math.abs(valorAnterior) < 0.0001) {
            return (valorActual != null && valorActual > 0.0) ? CIEN_PORCIENTO : CERO_PORCIENTO;
        }
        return ((valorActual - valorAnterior) / valorAnterior) * CIEN_PORCIENTO;
    }

    @Override
    public Long contarVentasHoy() {
        LocalDate hoy = LocalDate.now();
        return contarTransacciones(hoy, hoy);
    }

    @Override
    public Long contarVentasPorCliente(Long clienteId) {
        return ventaRepositorio.countByClienteId(clienteId);
    }

    @Override
    public Double calcularTotalVentasPorCliente(Long clienteId) {
        return ventaRepositorio.calcularTotalPorCliente(clienteId);
    }

    @Override
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        return ventaRepositorio.contarUnidadesVendidasPorProducto(productoId);
    }

    @Override
    public Double calcularIngresosPorProducto(Long productoId) {
        return ventaRepositorio.calcularIngresosPorProducto(productoId);
    }

    @Override
    public List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepositorio.buscarVentasRecientesPorProducto(productoId, pageable);
    }

    @Override
    public List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepositorio.findTopByClienteIdOrderByFechaDesc(clienteId, pageable);
    }

    // ===== IMPLEMENTACIÓN DE NUEVOS MÉTODOS PARA RESPALDO =====

    @Override
    @Transactional(readOnly = true)
    public List<VentaDetalle> obtenerDetallesPorVentaId(Long ventaId) {
        return ventaDetalleRepositorio.findByVentaId(ventaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDetalle> obtenerDetallesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaDetalleRepositorio.findByVentaFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTotalDetalles() {
        return ventaDetalleRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Estadísticas básicas
            Double totalVentas = calcularTotalVentas(fechaInicio, fechaFin);
            Long numeroVentas = contarTransacciones(fechaInicio, fechaFin);
            Double ticketPromedio = calcularTicketPromedio(fechaInicio, fechaFin);
            Long articulosVendidos = contarArticulosVendidos(fechaInicio, fechaFin);

            estadisticas.put("totalVentas", totalVentas);
            estadisticas.put("numeroVentas", numeroVentas);
            estadisticas.put("ticketPromedio", ticketPromedio);
            estadisticas.put("articulosVendidos", articulosVendidos);

            // Ventas por estado
            List<Venta> ventasPeriodo = buscarPorRangoFechas(fechaInicio, fechaFin);
            Map<String, Long> ventasPorEstado = ventasPeriodo.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getEstado() != null ? v.getEstado() : "SIN_ESTADO",
                            Collectors.counting()
                    ));
            estadisticas.put("ventasPorEstado", ventasPorEstado);

            // Ventas por método de pago
            Map<String, Long> ventasPorMetodoPago = ventasPeriodo.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getMetodoPago() != null ? v.getMetodoPago() : "SIN_METODO",
                            Collectors.counting()
                    ));
            estadisticas.put("ventasPorMetodoPago", ventasPorMetodoPago);

            // Top vendedores
            Map<String, Long> topVendedores = ventasPeriodo.stream()
                    .filter(v -> v.getVendedor() != null)
                    .collect(Collectors.groupingBy(
                            v -> v.getVendedor().getNombreCompleto(),
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            estadisticas.put("topVendedores", topVendedores);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas del período", e);
        }

        return estadisticas;
    }

    @Override
    public String exportarDatos(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            List<Venta> ventas = buscarPorRangoFechasCompleto(fechaInicio, fechaFin);

            switch (formato.toUpperCase()) {
                case "JSON":
                    return exportarComoJSON(ventas);
                case "CSV":
                    return exportarComoCSV(ventas);
                case "XML":
                    return exportarComoXML(ventas);
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error al exportar datos", e);
            throw new RuntimeException("Error al exportar datos: " + e.getMessage(), e);
        }
    }

    @Override
    public int importarDatos(String datos, String formato) {
        try {
            switch (formato.toUpperCase()) {
                case "JSON":
                    return importarDesdeJSON(datos);
                case "CSV":
                    return importarDesdeCSV(datos);
                case "XML":
                    return importarDesdeXML(datos);
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error al importar datos", e);
            throw new RuntimeException("Error al importar datos: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validarIntegridadDatos() {
        Map<String, Object> resultados = new HashMap<>();
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        try {
            // Validar ventas sin cliente
            long ventasSinCliente = ventaRepositorio.findAll().stream()
                    .filter(v -> v.getCliente() == null)
                    .count();
            if (ventasSinCliente > 0) {
                errores.add("Hay " + ventasSinCliente + " ventas sin cliente asignado");
            }

            // Validar ventas sin vendedor
            long ventasSinVendedor = ventaRepositorio.findAll().stream()
                    .filter(v -> v.getVendedor() == null)
                    .count();
            if (ventasSinVendedor > 0) {
                advertencias.add("Hay " + ventasSinVendedor + " ventas sin vendedor asignado");
            }

            // Validar detalles sin producto
            long detallesSinProducto = ventaDetalleRepositorio.findAll().stream()
                    .filter(d -> d.getProducto() == null)
                    .count();
            if (detallesSinProducto > 0) {
                errores.add("Hay " + detallesSinProducto + " detalles de venta sin producto");
            }

            // Validar totales
            List<Venta> ventasConErrorTotal = new ArrayList<>();
            for (Venta venta : ventaRepositorio.findAll()) {
                List<VentaDetalle> detalles = obtenerDetallesPorVentaId(venta.getId());
                double totalCalculado = detalles.stream()
                        .mapToDouble(VentaDetalle::getTotal)
                        .sum();

                if (Math.abs(totalCalculado - venta.getSubtotal()) > 0.01) {
                    ventasConErrorTotal.add(venta);
                }
            }
            if (!ventasConErrorTotal.isEmpty()) {
                advertencias.add("Hay " + ventasConErrorTotal.size() + " ventas con discrepancia en totales");
            }

            resultados.put("valido", errores.isEmpty());
            resultados.put("errores", errores);
            resultados.put("advertencias", advertencias);
            resultados.put("totalVentasValidadas", ventaRepositorio.count());
            resultados.put("totalDetallesValidados", ventaDetalleRepositorio.count());

        } catch (Exception e) {
            logger.error("Error al validar integridad de datos", e);
            errores.add("Error general: " + e.getMessage());
            resultados.put("valido", false);
            resultados.put("errores", errores);
        }

        return resultados;
    }

    // Métodos auxiliares de exportación

    private String exportarComoJSON(List<Venta> ventas) throws Exception {
        List<Map<String, Object>> ventasDTO = ventas.stream()
                .map(this::convertirVentaAMapCompleto)
                .collect(Collectors.toList());

        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(ventasDTO);
    }

    private String exportarComoCSV(List<Venta> ventas) {
        StringWriter writer = new StringWriter();

        // Encabezados
        writer.write("ID,Fecha,Cliente,Vendedor,Subtotal,Impuesto,Total,MetodoPago,Estado,Observaciones\n");

        // Datos
        for (Venta venta : ventas) {
            writer.write(String.format("%d,%s,%s,%s,%.2f,%.2f,%.2f,%s,%s,%s\n",
                    venta.getId(),
                    venta.getFecha(),
                    venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "",
                    venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "",
                    venta.getSubtotal(),
                    venta.getImpuesto(),
                    venta.getTotal(),
                    venta.getMetodoPago() != null ? venta.getMetodoPago() : "",
                    venta.getEstado() != null ? venta.getEstado() : "",
                    venta.getObservaciones() != null ? venta.getObservaciones().replace(",", ";") : ""
            ));
        }

        return writer.toString();
    }

    private String exportarComoXML(List<Venta> ventas) {
        // Implementación básica de XML
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ventas>\n");

        for (Venta venta : ventas) {
            xml.append("  <venta>\n");
            xml.append("    <id>").append(venta.getId()).append("</id>\n");
            xml.append("    <fecha>").append(venta.getFecha()).append("</fecha>\n");
            xml.append("    <total>").append(venta.getTotal()).append("</total>\n");
            xml.append("    <estado>").append(venta.getEstado()).append("</estado>\n");
            xml.append("  </venta>\n");
        }

        xml.append("</ventas>");
        return xml.toString();
    }

    // Métodos auxiliares de importación

    private int importarDesdeJSON(String json) throws Exception {
        // TODO: Implementar importación desde JSON
        logger.warn("Importación desde JSON no implementada completamente");
        return 0;
    }

    private int importarDesdeCSV(String csv) {
        // TODO: Implementar importación desde CSV
        logger.warn("Importación desde CSV no implementada completamente");
        return 0;
    }

    private int importarDesdeXML(String xml) {
        // TODO: Implementar importación desde XML
        logger.warn("Importación desde XML no implementada completamente");
        return 0;
    }

    private Map<String, Object> convertirVentaAMapCompleto(Venta venta) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", venta.getId());
        map.put("fecha", venta.getFecha());
        map.put("cliente", venta.getCliente() != null ?
                Map.of("id", venta.getCliente().getId(),
                        "nombre", venta.getCliente().getNombreCompleto()) : null);
        map.put("vendedor", venta.getVendedor() != null ?
                Map.of("id", venta.getVendedor().getId(),
                        "nombre", venta.getVendedor().getNombreCompleto()) : null);
        map.put("subtotal", venta.getSubtotal());
        map.put("impuesto", venta.getImpuesto());
        map.put("total", venta.getTotal());
        map.put("metodoPago", venta.getMetodoPago());
        map.put("observaciones", venta.getObservaciones());
        map.put("estado", venta.getEstado());

        // Incluir detalles
        List<VentaDetalle> detalles = obtenerDetallesPorVentaId(venta.getId());
        List<Map<String, Object>> detallesDTO = detalles.stream()
                .map(d -> Map.of(
                        "productoId", d.getProducto() != null ? d.getProducto().getId() : null,
                        "cantidad", d.getCantidad(),
                        "precioUnitario", d.getPrecioUnitario(),
                        "total", (Object) d.getTotal()
                ))
                .collect(Collectors.toList());
        map.put("detalles", detallesDTO);

        return map;
    }


}