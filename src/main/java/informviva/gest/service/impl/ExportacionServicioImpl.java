package informviva.gest.service.impl;

// ===== IMPORTS ORGANIZADOS - SOLO OPENPDF =====

// Java Core
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// Spring Framework
import com.lowagie.text.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// Apache POI (Excel) - AGRUPADOS PARA EVITAR CONFLICTOS
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ✅ SOLO OpenPDF - CORREGIDO
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Aplicación
import informviva.gest.dto.*;
import informviva.gest.model.*;
import informviva.gest.service.*;

/**
 * ✅ CORRECCIÓN: Solo OpenPDF (com.lowagie.text)
 * - Eliminados todos los imports de iText
 * - Código actualizado para usar PdfPTable en lugar de Table
 * - Consistencia total con OpenPDF
 */
@Service
public class ExportacionServicioImpl implements ExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionServicioImpl.class);

    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;
    private final UsuarioServicio usuarioServicio;
    private final ReporteServicio reporteServicio;
    private final ExportacionHistorialServicio historialServicio;

    // Formatos soportados
    public static final String FORMATO_PDF = "PDF";
    public static final String FORMATO_EXCEL = "EXCEL";
    public static final String FORMATO_CSV = "CSV";

    // Tipos MIME
    private static final Map<String, String> MIME_TYPES = Map.of(
            FORMATO_PDF, "application/pdf",
            FORMATO_EXCEL, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            FORMATO_CSV, "text/csv"
    );

    // Extensiones de archivo
    private static final Map<String, String> EXTENSIONES = Map.of(
            FORMATO_PDF, ".pdf",
            FORMATO_EXCEL, ".xlsx",
            FORMATO_CSV, ".csv"
    );

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ARCHIVO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public ExportacionServicioImpl(
            ClienteServicio clienteServicio,
            @Qualifier("productoServicioImpl") ProductoServicio productoServicio,
            VentaServicio ventaServicio,
            UsuarioServicio usuarioServicio,
            ReporteServicio reporteServicio,
            ExportacionHistorialServicio historialServicio
    ) {
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
        this.usuarioServicio = usuarioServicio;
        this.reporteServicio = reporteServicio;
        this.historialServicio = historialServicio;
    }

    // ==================== MÉTODOS PRINCIPALES DE LA INTERFAZ ====================

    @Override
    public byte[] exportarUsuarios(ExportConfigDTO config) {
        logger.info("Exportando usuarios con configuración: {}", config.getFormato());

        try {
            List<UsuarioExportDTO> usuarios = obtenerUsuariosParaExportacion(config);

            return switch (config.getFormato().toUpperCase()) {
                case FORMATO_PDF -> generarPDFUsuarios(usuarios);
                case FORMATO_EXCEL -> generarExcelUsuarios(usuarios);
                case FORMATO_CSV -> generarCSVUsuarios(usuarios);
                default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
            };

        } catch (Exception e) {
            logger.error("Error al exportar usuarios: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de usuarios", e);
        }
    }

    @Override
    public byte[] exportarClientes(ExportConfigDTO config) {
        logger.info("Exportando clientes con configuración: {}", config.getFormato());

        try {
            List<ClienteExportDTO> clientes = obtenerClientesParaExportacion(config);

            return switch (config.getFormato().toUpperCase()) {
                case FORMATO_PDF -> generarPDFClientes(clientes);
                case FORMATO_EXCEL -> generarExcelClientes(clientes);
                case FORMATO_CSV -> generarCSVClientes(clientes);
                default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
            };

        } catch (Exception e) {
            logger.error("Error al exportar clientes: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de clientes", e);
        }
    }

    @Override
    public byte[] exportarProductos(ExportConfigDTO config) {
        logger.info("Exportando productos con configuración: {}", config.getFormato());

        try {
            List<ProductoExportDTO> productos = obtenerProductosParaExportacion(config);

            return switch (config.getFormato().toUpperCase()) {
                case FORMATO_PDF -> generarPDFProductos(productos);
                case FORMATO_EXCEL -> generarExcelProductos(productos);
                case FORMATO_CSV -> generarCSVProductos(productos);
                default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
            };

        } catch (Exception e) {
            logger.error("Error al exportar productos: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de productos", e);
        }
    }

    @Override
    public byte[] exportarVentas(ExportConfigDTO config) {
        logger.info("Exportando ventas con configuración: {} para período: {} - {}",
                config.getFormato(), config.getFechaInicio(), config.getFechaFin());

        try {
            List<VentaExportDTO> ventas = obtenerVentasParaExportacion(config);

            return switch (config.getFormato().toUpperCase()) {
                case FORMATO_PDF -> generarPDFVentas(ventas);
                case FORMATO_EXCEL -> generarExcelVentas(ventas);
                case FORMATO_CSV -> generarCSVVentas(ventas);
                default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
            };

        } catch (Exception e) {
            logger.error("Error al exportar ventas: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación de ventas", e);
        }
    }

    @Override
    public byte[] exportarReportes(ExportConfigDTO config) {
        logger.info("Exportando reporte: {} en formato: {}", config.getTipo(), config.getFormato());

        try {
            return switch (config.getTipo().toLowerCase()) {
                case "ventas" -> exportarReporteVentas(config);
                case "financiero" -> exportarReporteFinanciero(config);
                case "inventario" -> exportarReporteInventario(config);
                default -> throw new IllegalArgumentException("Tipo de reporte no soportado: " + config.getTipo());
            };

        } catch (Exception e) {
            logger.error("Error al exportar reporte: {}", e.getMessage());
            throw new RuntimeException("Error en la exportación del reporte", e);
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasExportacion() {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Obtener estadísticas del historial
            List<ExportacionHistorial> historialReciente = historialServicio.obtenerHistorialReciente(100);

            estadisticas.put("totalExportaciones", historialReciente.size());
            estadisticas.put("exportacionesExitosas",
                    historialReciente.stream().filter(h -> "COMPLETADO".equals(h.getEstado())).count());
            estadisticas.put("exportacionesErrores",
                    historialReciente.stream().filter(h -> "ERROR".equals(h.getEstado())).count());

            // Estadísticas por formato
            Map<String, Long> porFormato = historialReciente.stream()
                    .collect(Collectors.groupingBy(ExportacionHistorial::getFormato, Collectors.counting()));
            estadisticas.put("porFormato", porFormato);

            // Estadísticas por tipo
            Map<String, Long> porTipo = historialReciente.stream()
                    .collect(Collectors.groupingBy(ExportacionHistorial::getTipoExportacion, Collectors.counting()));
            estadisticas.put("porTipo", porTipo);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de exportación: {}", e.getMessage());
            estadisticas.put("error", "No se pudieron cargar las estadísticas");
        }

        return estadisticas;
    }

    @Override
    public List<ExportacionHistorial> obtenerHistorialReciente(int limite) {
        try {
            return historialServicio.obtenerHistorialReciente(limite);
        } catch (Exception e) {
            logger.error("Error al obtener historial reciente: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> obtenerEstimaciones(String tipo, String formato,
                                                   LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Map<String, Object> estimaciones = new HashMap<>();

        try {
            int registrosEstimados = calcularRegistrosEstimados(tipo, fechaInicio, fechaFin);
            long tamanoEstimado = calcularTamanoEstimado(registrosEstimados, formato);
            long tiempoEstimado = calcularTiempoEstimado(registrosEstimados, formato);

            estimaciones.put("registrosEstimados", registrosEstimados);
            estimaciones.put("tamanoEstimado", tamanoEstimado);
            estimaciones.put("tiempoEstimado", tiempoEstimado);
            estimaciones.put("tamanoLegible", formatearTamano(tamanoEstimado));
            estimaciones.put("tiempoLegible", formatearTiempo(tiempoEstimado));

        } catch (Exception e) {
            logger.error("Error al calcular estimaciones: {}", e.getMessage());
            estimaciones.put("error", "No se pudieron calcular las estimaciones");
        }

        return estimaciones;
    }

    @Override
    public Page<ExportacionHistorial> obtenerHistorialPaginado(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return historialServicio.obtenerHistorialPaginado(pageable);
        } catch (Exception e) {
            logger.error("Error al obtener historial paginado: {}", e.getMessage());
            throw new RuntimeException("Error al obtener historial paginado", e);
        }
    }

    // ==================== MÉTODOS LEGACY PARA COMPATIBILIDAD ====================

    @Override
    @Deprecated
    public byte[] exportarUsuarios(String formato, Map<String, Object> filtros) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("usuarios");
        config.setFormato(formato);
        config.setFiltros(filtros != null ? filtros : new HashMap<>());
        return exportarUsuarios(config);
    }

    @Override
    @Deprecated
    public byte[] exportarClientes(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("clientes");
        config.setFormato(formato);
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config.setFiltros(filtros != null ? filtros : new HashMap<>());
        return exportarClientes(config);
    }

    @Override
    @Deprecated
    public byte[] exportarProductos(String formato, Map<String, Object> filtros) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("productos");
        config.setFormato(formato);
        config.setFiltros(filtros != null ? filtros : new HashMap<>());
        return exportarProductos(config);
    }

    @Override
    @Deprecated
    public byte[] exportarVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("ventas");
        config.setFormato(formato);
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config.setFiltros(filtros != null ? filtros : new HashMap<>());
        return exportarVentas(config);
    }

    // ==================== MÉTODOS AUXILIARES DE OBTENCIÓN DE DATOS ====================

    private List<ClienteExportDTO> obtenerClientesParaExportacion(ExportConfigDTO config) {
        List<Cliente> clientes = clienteServicio.buscarTodos();

        return clientes.stream()
                .filter(cliente -> aplicarFiltrosCliente(cliente, config))
                .map(this::convertirAClienteExportDTO)
                .collect(Collectors.toList());
    }

    private List<ProductoExportDTO> obtenerProductosParaExportacion(ExportConfigDTO config) {
        List<Producto> productos = productoServicio.listar();

        return productos.stream()
                .filter(producto -> aplicarFiltrosProducto(producto, config))
                .map(this::convertirAProductoExportDTO)
                .collect(Collectors.toList());
    }

    private List<VentaExportDTO> obtenerVentasParaExportacion(ExportConfigDTO config) {
        LocalDateTime fechaInicio = config.getFechaInicio();
        LocalDateTime fechaFin = config.getFechaFin();

        // Si no hay fechas, usar últimos 30 días
        if (fechaInicio == null || fechaFin == null) {
            fechaFin = LocalDateTime.now();
            fechaInicio = fechaFin.minusDays(30);
        }

        List<Venta> ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> aplicarFiltrosVenta(venta, config))
                .map(this::convertirAVentaExportDTO)
                .collect(Collectors.toList());
    }

    private List<UsuarioExportDTO> obtenerUsuariosParaExportacion(ExportConfigDTO config) {
        List<Usuario> usuarios = usuarioServicio.listarTodos();

        return usuarios.stream()
                .filter(usuario -> aplicarFiltrosUsuario(usuario, config))
                .map(this::convertirAUsuarioExportDTO)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE FILTRADO ====================

    private boolean aplicarFiltrosCliente(Cliente cliente, ExportConfigDTO config) {
        Map<String, Object> filtros = config.getFiltros();

        // Filtrar por rango de fechas
        if (config.getFechaInicio() != null && cliente.getFechaRegistro() != null) {
            if (cliente.getFechaRegistro().isBefore(config.getFechaInicio())) {
                return false;
            }
        }

        if (config.getFechaFin() != null && cliente.getFechaRegistro() != null) {
            if (cliente.getFechaRegistro().isAfter(config.getFechaFin())) {
                return false;
            }
        }

        // Aplicar filtros adicionales
        if (filtros != null) {
            String busqueda = (String) filtros.get("busqueda");
            if (busqueda != null && !busqueda.isEmpty()) {
                String busquedaLower = busqueda.toLowerCase();
                return cliente.getNombre().toLowerCase().contains(busquedaLower) ||
                        cliente.getApellido().toLowerCase().contains(busquedaLower) ||
                        (cliente.getEmail() != null && cliente.getEmail().toLowerCase().contains(busquedaLower));
            }

            Boolean soloConCompras = (Boolean) filtros.get("soloConCompras");
            if (soloConCompras != null && soloConCompras) {
                return ventaServicio.existenVentasPorCliente(cliente.getId());
            }
        }

        return true;
    }

    private boolean aplicarFiltrosProducto(Producto producto, ExportConfigDTO config) {
        Map<String, Object> filtros = config.getFiltros();
        if (filtros == null) return true;

        Boolean soloActivos = (Boolean) filtros.get("soloActivos");
        if (soloActivos != null && soloActivos && !producto.getActivo()) {
            return false;
        }

        Boolean soloBajoStock = (Boolean) filtros.get("soloBajoStock");
        if (soloBajoStock != null && soloBajoStock) {
            Integer umbral = (Integer) filtros.get("umbralStock");
            if (umbral == null) umbral = 5;
            return producto.getStock() != null && producto.getStock() <= umbral;
        }

        String categoria = (String) filtros.get("categoria");
        if (categoria != null && !categoria.isEmpty() && producto.getCategoria() != null) {
            return producto.getCategoria().getNombre().equalsIgnoreCase(categoria);
        }

        return true;
    }

    private boolean aplicarFiltrosVenta(Venta venta, ExportConfigDTO config) {
        Map<String, Object> filtros = config.getFiltros();
        if (filtros == null) return true;

        String estado = (String) filtros.get("estado");
        if (estado != null && !estado.isEmpty()) {
            return venta.getEstado() != null && estado.equalsIgnoreCase(venta.getEstado().name());
        }

        String metodo = (String) filtros.get("metodo");
        if (metodo != null && !metodo.isEmpty()) {
            return venta.getMetodoPago() != null && metodo.equalsIgnoreCase(venta.getMetodoPago().name());
        }

        return true;
    }

    private boolean aplicarFiltrosUsuario(Usuario usuario, ExportConfigDTO config) {
        Map<String, Object> filtros = config.getFiltros();
        if (filtros == null) return true;

        Boolean soloActivos = (Boolean) filtros.get("soloActivos");
        if (soloActivos != null && soloActivos && !usuario.isActivo()) {
            return false;
        }

        String rol = (String) filtros.get("rol");
        if (rol != null && !rol.isEmpty()) {
            return usuario.getRoles().contains(rol);
        }

        return true;
    }

    // ==================== MÉTODOS DE CONVERSIÓN A DTOs ====================

    private ClienteExportDTO convertirAClienteExportDTO(Cliente cliente) {
        ClienteExportDTO dto = new ClienteExportDTO();
        dto.setId(cliente.getId());
        dto.setRut(cliente.getRut());
        dto.setNombreCompleto(cliente.getNombreCompleto());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setCategoria(cliente.getCategoria());

        // Obtener estadísticas de compras
        Long totalCompras = ventaServicio.contarVentasPorCliente(cliente.getId());
        Double montoTotalCompras = ventaServicio.calcularTotalVentasPorCliente(cliente.getId());

        dto.setTotalCompras(totalCompras != null ? totalCompras : 0L);
        dto.setMontoTotalCompras(montoTotalCompras != null ? montoTotalCompras : 0.0);

        return dto;
    }

    private ProductoExportDTO convertirAProductoExportDTO(Producto producto) {
        ProductoExportDTO dto = new ProductoExportDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría");
        dto.setMarca(producto.getMarca());
        dto.setModelo(producto.getModelo());
        dto.setActivo(producto.getActivo());
        dto.setFechaCreacion(producto.getFechaCreacion());
        dto.setFechaActualizacion(producto.getFechaActualizacion());

        return dto;
    }

    private VentaExportDTO convertirAVentaExportDTO(Venta venta) {
        VentaExportDTO dto = new VentaExportDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setClienteNombre(venta.getCliente().getNombreCompleto());
        dto.setClienteRut(venta.getCliente().getRut());
        dto.setVendedorNombre(venta.getVendedor().getNombreCompleto());
        dto.setSubtotal(venta.getSubtotal());
        dto.setImpuesto(venta.getImpuesto());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago() != null ? venta.getMetodoPago().name() : "");
        dto.setEstado(venta.getEstado() != null ? venta.getEstado().name() : "");
        dto.setObservaciones(venta.getObservaciones());
        return dto;
    }

    private UsuarioExportDTO convertirAUsuarioExportDTO(Usuario usuario) {
        UsuarioExportDTO dto = new UsuarioExportDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setRoles(String.join(", ", usuario.getRoles()));

        return dto;
    }

    // ==================== MÉTODOS DE REPORTES ESPECÍFICOS ====================

    private byte[] exportarReporteVentas(ExportConfigDTO config) {
        LocalDateTime fechaInicio = config.getFechaInicio();
        LocalDateTime fechaFin = config.getFechaFin();

        if (fechaInicio == null || fechaFin == null) {
            fechaFin = LocalDateTime.now();
            fechaInicio = fechaFin.minusDays(30);
        }

        VentaResumenDTO resumen = reporteServicio.generarResumenVentas(
                fechaInicio.toLocalDate(),
                fechaFin.toLocalDate()
        );

        return switch (config.getFormato().toUpperCase()) {
            case FORMATO_PDF -> generarPDFReporteVentas(resumen, "ventas", fechaInicio, fechaFin);
            case FORMATO_EXCEL -> generarExcelReporteVentas(resumen, "ventas", fechaInicio, fechaFin);
            case FORMATO_CSV -> generarCSVReporteVentas(resumen, "ventas", fechaInicio, fechaFin);
            default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
        };
    }

    private byte[] exportarReporteFinanciero(ExportConfigDTO config) {
        LocalDateTime fechaInicio = config.getFechaInicio();
        LocalDateTime fechaFin = config.getFechaFin();

        if (fechaInicio == null || fechaFin == null) {
            fechaFin = LocalDateTime.now();
            fechaInicio = fechaFin.minusDays(30);
        }

        ReporteFinancieroDTO reporte = generarReporteFinanciero(fechaInicio, fechaFin, true);

        return switch (config.getFormato().toUpperCase()) {
            case FORMATO_PDF -> generarPDFReporteFinanciero(reporte);
            case FORMATO_EXCEL -> generarExcelReporteFinanciero(reporte);
            case FORMATO_CSV -> generarCSVReporteFinanciero(reporte);
            default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
        };
    }

    private byte[] exportarReporteInventario(ExportConfigDTO config) {
        Map<String, Object> filtros = config.getFiltros();
        Boolean incluirBajoStock = (Boolean) filtros.get("incluirBajoStock");
        Integer umbralStock = (Integer) filtros.get("umbralStock");

        List<ProductoExportDTO> productos = incluirBajoStock != null && incluirBajoStock ?
                obtenerProductosBajoStock(umbralStock) :
                obtenerProductosParaExportacion(config);

        return switch (config.getFormato().toUpperCase()) {
            case FORMATO_PDF -> generarPDFInventario(productos, incluirBajoStock, umbralStock);
            case FORMATO_EXCEL -> generarExcelInventario(productos, incluirBajoStock, umbralStock);
            case FORMATO_CSV -> generarCSVInventario(productos, incluirBajoStock, umbralStock);
            default -> throw new IllegalArgumentException("Formato no soportado: " + config.getFormato());
        };
    }

    private List<ProductoExportDTO> obtenerProductosBajoStock(Integer umbral) {
        if (umbral == null) umbral = 5;
        List<Producto> productos = productoServicio.listarConBajoStock(umbral);

        return productos.stream()
                .map(this::convertirAProductoExportDTO)
                .collect(Collectors.toList());
    }

    private ReporteFinancieroDTO generarReporteFinanciero(LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean incluirComparativo) {
        ReporteFinancieroDTO reporte = new ReporteFinancieroDTO();

        Double ventasActuales = ventaServicio.calcularTotalVentas(fechaInicio, fechaFin);
        Long transaccionesActuales = ventaServicio.contarTransacciones(fechaInicio, fechaFin);

        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setVentasTotales(ventasActuales != null ? ventasActuales : 0.0);
        reporte.setTransaccionesTotales(transaccionesActuales != null ? transaccionesActuales : 0L);

        if (incluirComparativo) {
            long diasPeriodo = java.time.Duration.between(fechaInicio, fechaFin).toDays();
            LocalDateTime inicioAnterior = fechaInicio.minusDays(diasPeriodo);
            LocalDateTime finAnterior = fechaInicio.minusDays(1);

            Double ventasAnteriores = ventaServicio.calcularTotalVentas(inicioAnterior, finAnterior);
            Long transaccionesAnteriores = ventaServicio.contarTransacciones(inicioAnterior, finAnterior);

            reporte.setVentasAnteriores(ventasAnteriores != null ? ventasAnteriores : 0.0);
            reporte.setTransaccionesAnteriores(transaccionesAnteriores != null ? transaccionesAnteriores : 0L);

            Double porcentajeCambioVentas = ventaServicio.calcularPorcentajeCambio(ventasActuales, ventasAnteriores);
            Double porcentajeCambioTransacciones = ventaServicio.calcularPorcentajeCambio(
                    transaccionesActuales != null ? transaccionesActuales.doubleValue() : 0.0,
                    transaccionesAnteriores != null ? transaccionesAnteriores.doubleValue() : 0.0
            );

            reporte.setPorcentajeCambioVentas(porcentajeCambioVentas);
            reporte.setPorcentajeCambioTransacciones(porcentajeCambioTransacciones);
        }

        return reporte;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private int calcularRegistrosEstimados(String tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return switch (tipo.toLowerCase()) {
            case "clientes" -> clienteServicio.buscarTodos().size();
            case "productos" -> productoServicio.listar().size();
            case "usuarios" -> usuarioServicio.listarTodos().size();
            case "ventas" -> {
                if (fechaInicio != null && fechaFin != null) {
                    yield ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin).size();
                } else {
                    yield ventaServicio.obtenerTodasLasVentas().size();
                }
            }
            default -> 100; // Estimación por defecto
        };
    }

    private long calcularTamanoEstimado(int registros, String formato) {
        return switch (formato.toUpperCase()) {
            case FORMATO_PDF -> registros * 200L;
            case FORMATO_EXCEL -> registros * 150L;
            case FORMATO_CSV -> registros * 100L;
            default -> registros * 150L;
        };
    }

    private long calcularTiempoEstimado(int registros, String formato) {
        long tiempoBase = switch (formato.toUpperCase()) {
            case FORMATO_PDF -> registros * 2L;
            case FORMATO_EXCEL -> registros * 1L;
            case FORMATO_CSV -> registros / 2L;
            default -> registros * 1L;
        };

        return Math.max(tiempoBase, 100L);
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private String formatearTiempo(long millis) {
        if (millis < 1000) return millis + " ms";
        if (millis < 60000) return String.format("%.1f s", millis / 1000.0);
        return String.format("%.1f min", millis / 60000.0);
    }

    // ==================== MÉTODOS HEREDADOS ====================

    public byte[] exportarReporteVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoReporte) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("ventas");
        config.setFormato(formato);
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config.addFiltro("tipoReporte", tipoReporte);
        return exportarReporteVentas(config);
    }

    public byte[] exportarInventario(String formato, boolean incluirBajoStock, Integer umbralStock) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("inventario");
        config.setFormato(formato);
        config.addFiltro("incluirBajoStock", incluirBajoStock);
        config.addFiltro("umbralStock", umbralStock);
        return exportarReporteInventario(config);
    }

    public byte[] exportarReporteFinanciero(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean incluirComparativo) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("financiero");
        config.setFormato(formato);
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config.addFiltro("incluirComparativo", incluirComparativo);
        return exportarReporteFinanciero(config);
    }

    public List<String> getFormatosSoportados() {
        return Arrays.asList(FORMATO_PDF, FORMATO_EXCEL, FORMATO_CSV);
    }

    public boolean isFormatoSoportado(String formato) {
        return getFormatosSoportados().contains(formato.toUpperCase());
    }

    public String getMimeType(String formato) {
        return MIME_TYPES.get(formato.toUpperCase());
    }

    public String getExtensionArchivo(String formato) {
        return EXTENSIONES.get(formato.toUpperCase());
    }

    public byte[] exportarVentasExcel(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Agregar datos
            int rowIdx = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(venta.getId());
                row.createCell(1).setCellValue(venta.getFecha().format(FECHA_FORMATTER));
                row.createCell(2).setCellValue(venta.getCliente().getNombreCompleto());
                row.createCell(3).setCellValue(venta.getVendedor().getNombreCompleto());
                row.createCell(4).setCellValue(venta.getSubtotal());
                row.createCell(5).setCellValue(venta.getImpuesto());
                row.createCell(6).setCellValue(venta.getTotal());
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de ventas", e);
        }
    }

    // ==================== MÉTODOS DE GENERACIÓN PDF - CORREGIDOS CON OPENPDF ====================

    /**
     * ✅ CORREGIDO: Usar solo OpenPDF (com.lowagie.text)
     */
    private byte[] generarPDFClientes(List<ClienteExportDTO> clientes) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, output);
            document.open();

            // ✅ Título usando OpenPDF
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Listado de Clientes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // ✅ Tabla usando OpenPDF
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Encabezados
            String[] headers = {"RUT", "Nombre", "Email", "Teléfono", "Fecha Registro", "Total Compras"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.HELVETICA, 10, Font.BOLD)));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Datos
            for (ClienteExportDTO cliente : clientes) {
                table.addCell(cliente.getRut() != null ? cliente.getRut() : "");
                table.addCell(cliente.getNombreCompleto());
                table.addCell(cliente.getEmail() != null ? cliente.getEmail() : "");
                table.addCell(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                table.addCell(cliente.getFechaRegistro() != null ?
                        cliente.getFechaRegistro().format(FECHA_FORMATTER) : "");
                table.addCell(String.valueOf(cliente.getTotalCompras()));
            }

            document.add(table);
            document.close();
            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de clientes", e);
        }
    }

    // ==================== MÉTODOS DE GENERACIÓN EXCEL ====================

    private byte[] generarExcelClientes(List<ClienteExportDTO> clientes) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Clientes");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "RUT", "Nombre Completo", "Email", "Teléfono", "Dirección",
                    "Fecha Registro", "Categoría", "Total Compras", "Monto Total"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 1;
            for (ClienteExportDTO cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(cliente.getId());
                row.createCell(1).setCellValue(cliente.getRut() != null ? cliente.getRut() : "");
                row.createCell(2).setCellValue(cliente.getNombreCompleto());
                row.createCell(3).setCellValue(cliente.getEmail() != null ? cliente.getEmail() : "");
                row.createCell(4).setCellValue(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                row.createCell(5).setCellValue(cliente.getDireccion() != null ? cliente.getDireccion() : "");
                row.createCell(6).setCellValue(cliente.getFechaRegistro() != null ?
                        cliente.getFechaRegistro().format(FECHA_FORMATTER) : "");
                row.createCell(7).setCellValue(cliente.getCategoria() != null ? cliente.getCategoria() : "");
                row.createCell(8).setCellValue(cliente.getTotalCompras());
                row.createCell(9).setCellValue(cliente.getMontoTotalCompras());
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de clientes", e);
        }
    }

    // ==================== MÉTODOS DE GENERACIÓN CSV ====================

    private byte[] generarCSVClientes(List<ClienteExportDTO> clientes) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,RUT,Nombre Completo,Email,Teléfono,Dirección,Fecha Registro,Categoría,Total Compras,Monto Total\n");

        // Datos
        for (ClienteExportDTO cliente : clientes) {
            csv.append(cliente.getId()).append(",");
            csv.append("\"").append(cliente.getRut() != null ? cliente.getRut() : "").append("\",");
            csv.append("\"").append(cliente.getNombreCompleto()).append("\",");
            csv.append("\"").append(cliente.getEmail() != null ? cliente.getEmail() : "").append("\",");
            csv.append("\"").append(cliente.getTelefono() != null ? cliente.getTelefono() : "").append("\",");
            csv.append("\"").append(cliente.getDireccion() != null ? cliente.getDireccion() : "").append("\",");
            csv.append(cliente.getFechaRegistro() != null ?
                    cliente.getFechaRegistro().format(FECHA_FORMATTER) : "").append(",");
            csv.append("\"").append(cliente.getCategoria() != null ? cliente.getCategoria() : "").append("\",");
            csv.append(cliente.getTotalCompras()).append(",");
            csv.append(cliente.getMontoTotalCompras());
            csv.append("\n");
        }

        return csv.toString().getBytes();
    }

    // ==================== MÉTODOS PLACEHOLDER (IMPLEMENTAR SEGÚN NECESIDADES) ====================

    private byte[] generarPDFProductos(List<ProductoExportDTO> productos) { return new byte[0]; }
    private byte[] generarPDFVentas(List<VentaExportDTO> ventas) { return new byte[0]; }
    private byte[] generarPDFUsuarios(List<UsuarioExportDTO> usuarios) { return new byte[0]; }
    private byte[] generarPDFReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) { return new byte[0]; }
    private byte[] generarPDFInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) { return new byte[0]; }
    private byte[] generarPDFReporteFinanciero(ReporteFinancieroDTO reporte) { return new byte[0]; }

    private byte[] generarExcelProductos(List<ProductoExportDTO> productos) { return new byte[0]; }
    private byte[] generarExcelVentas(List<VentaExportDTO> ventas) { return new byte[0]; }
    private byte[] generarExcelUsuarios(List<UsuarioExportDTO> usuarios) { return new byte[0]; }
    private byte[] generarExcelReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) { return new byte[0]; }
    private byte[] generarExcelInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) { return new byte[0]; }
    private byte[] generarExcelReporteFinanciero(ReporteFinancieroDTO reporte) { return new byte[0]; }

    private byte[] generarCSVProductos(List<ProductoExportDTO> productos) { return new byte[0]; }
    private byte[] generarCSVVentas(List<VentaExportDTO> ventas) { return new byte[0]; }
    private byte[] generarCSVUsuarios(List<UsuarioExportDTO> usuarios) { return new byte[0]; }
    private byte[] generarCSVReporteVentas(VentaResumenDTO resumen, String tipoReporte, LocalDateTime fechaInicio, LocalDateTime fechaFin) { return new byte[0]; }
    private byte[] generarCSVInventario(List<ProductoExportDTO> productos, boolean incluirBajoStock, Integer umbralStock) { return new byte[0]; }
    private byte[] generarCSVReporteFinanciero(ReporteFinancieroDTO reporte) { return new byte[0]; }
}