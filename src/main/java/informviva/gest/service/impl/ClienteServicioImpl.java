package informviva.gest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de clientes
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Service
@Transactional
public class ClienteServicioImpl implements ClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServicioImpl.class);

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private VentaRepositorio ventaRepositorio;

    private final ObjectMapper objectMapper;

    public ClienteServicioImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Métodos básicos CRUD
    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepositorio.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return clienteRepositorio.findById(id).orElse(null);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return clienteRepositorio.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        clienteRepositorio.deleteById(id);
    }

    @Override
    public boolean rutEsValido(String rut) {
        return ValidadorRutUtil.validar(rut);
    }

    // Métodos para reportes
    @Override
    public List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Cliente> todosLosClientes = clienteRepositorio.findAll();

        return todosLosClientes.stream()
                .map(cliente -> {
                    ClienteReporteDTO dto = new ClienteReporteDTO();
                    dto.setId(cliente.getId());
                    dto.setRut(cliente.getRut());
                    dto.setNombreCompleto(cliente.getNombreCompleto());
                    dto.setEmail(cliente.getEmail());
                    dto.setFechaRegistro(cliente.getFechaRegistro());

                    // Calcular estadísticas de compras
                    var ventasCliente = ventaRepositorio.findByCliente(cliente);

                    // Filtrar por fechas si se proporcionan
                    if (fechaInicio != null && fechaFin != null) {
                        ventasCliente = ventasCliente.stream()
                                .filter(venta -> {
                                    LocalDate fechaVenta = venta.getFechaAsLocalDate();
                                    return fechaVenta != null &&
                                            !fechaVenta.isBefore(fechaInicio) &&
                                            !fechaVenta.isAfter(fechaFin);
                                })
                                .collect(Collectors.toList());
                    }

                    dto.setComprasRealizadas(ventasCliente.size());

                    BigDecimal totalCompras = ventasCliente.stream()
                            .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                            .map(venta -> BigDecimal.valueOf(venta.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    dto.setTotalCompras(totalCompras);

                    // Calcular promedio por compra
                    if (dto.getComprasRealizadas() > 0) {
                        dto.setPromedioPorCompra(totalCompras.divide(
                                BigDecimal.valueOf(dto.getComprasRealizadas()),
                                2, RoundingMode.HALF_UP));
                    } else {
                        dto.setPromedioPorCompra(BigDecimal.ZERO);
                    }

                    // Encontrar última compra
                    dto.setUltimaCompra(ventasCliente.stream()
                            .map(venta -> venta.getFechaAsLocalDate())
                            .filter(fecha -> fecha != null)
                            .max(LocalDate::compareTo)
                            .orElse(null));

                    return dto;
                })
                .filter(dto -> dto.getComprasRealizadas() > 0) // Solo clientes con compras
                .collect(Collectors.toList());
    }

    @Override
    public List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> {
                    LocalDate fechaRegistro = cliente.getFechaRegistro();
                    return fechaRegistro != null &&
                            !fechaRegistro.isBefore(fechaInicio) &&
                            !fechaRegistro.isAfter(fechaFin);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return (long) obtenerClientesNuevos(fechaInicio, fechaFin).size();
    }

    @Override
    public List<Cliente> obtenerClientesPorCategoria(String categoria) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> categoria.equals(cliente.getCategoria()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite) {
        return obtenerClientesConCompras(null, null).stream()
                .sorted((c1, c2) -> c2.getTotalCompras().compareTo(c1.getTotalCompras()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    // Métodos de análisis
    @Override
    public Double calcularPromedioComprasPorCliente() {
        List<ClienteReporteDTO> clientesConCompras = obtenerClientesConCompras(null, null);

        if (clientesConCompras.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalCompras = clientesConCompras.stream()
                .map(ClienteReporteDTO::getTotalCompras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCompras.divide(BigDecimal.valueOf(clientesConCompras.size()),
                2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public Long contarClientesActivos() {
        // Considerar activos a los clientes que han comprado en los últimos 90 días
        LocalDate hace90Dias = LocalDate.now().minusDays(90);

        return obtenerClientesConCompras(hace90Dias, LocalDate.now()).stream()
                .filter(cliente -> cliente.getUltimaCompra() != null)
                .count();
    }

    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> cliente.getNombre().toLowerCase().contains(nombre.toLowerCase()) ||
                        cliente.getApellido().toLowerCase().contains(nombre.toLowerCase()) ||
                        cliente.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Cliente> buscarPorEmail(String email) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> cliente.getEmail() != null &&
                        cliente.getEmail().toLowerCase().contains(email.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeClienteConEmail(String email) {
        return clienteRepositorio.existsByEmail(email);
    }

    @Override
    public boolean existeClienteConRut(String rut) {
        return clienteRepositorio.findAll().stream()
                .anyMatch(cliente -> rut.equals(cliente.getRut()));
    }

    @Override
    public Page<Cliente> obtenerTodosPaginados(Pageable pageable) {
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    public Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingOrEmailContainingIgnoreCase(
                busqueda, busqueda, pageable);
    }

    // ===== IMPLEMENTACIÓN DE NUEVOS MÉTODOS PARA RESPALDO =====

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    public String exportarDatos(String formato, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<Cliente> clientes;

            if (fechaInicio != null && fechaFin != null) {
                clientes = buscarPorFechaRegistro(fechaInicio, fechaFin);
            } else {
                clientes = obtenerTodos();
            }

            switch (formato.toUpperCase()) {
                case "JSON":
                    return exportarComoJSON(clientes);
                case "CSV":
                    return exportarComoCSV(clientes);
                case "XML":
                    return exportarComoXML(clientes);
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error al exportar datos de clientes", e);
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
            logger.error("Error al importar datos de clientes", e);
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
            // Validar emails duplicados
            Map<String, Long> emailsCount = clienteRepositorio.findAll().stream()
                    .filter(c -> c.getEmail() != null)
                    .collect(Collectors.groupingBy(Cliente::getEmail, Collectors.counting()));

            emailsCount.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .forEach(entry -> errores.add("Email duplicado: " + entry.getKey()));

            // Validar RUTs duplicados
            Map<String, Long> rutsCount = clienteRepositorio.findAll().stream()
                    .filter(c -> c.getRut() != null)
                    .collect(Collectors.groupingBy(Cliente::getRut, Collectors.counting()));

            rutsCount.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .forEach(entry -> errores.add("RUT duplicado: " + entry.getKey()));

            // Validar RUTs inválidos
            long rutsInvalidos = clienteRepositorio.findAll().stream()
                    .filter(c -> c.getRut() != null && !rutEsValido(c.getRut()))
                    .count();

            if (rutsInvalidos > 0) {
                advertencias.add("Hay " + rutsInvalidos + " clientes con RUT inválido");
            }

            // Validar clientes sin datos de contacto
            long clientesSinContacto = clienteRepositorio.findAll().stream()
                    .filter(c -> (c.getEmail() == null || c.getEmail().isEmpty()) &&
                            (c.getTelefono() == null || c.getTelefono().isEmpty()))
                    .count();

            if (clientesSinContacto > 0) {
                advertencias.add("Hay " + clientesSinContacto + " clientes sin datos de contacto");
            }

            resultados.put("valido", errores.isEmpty());
            resultados.put("errores", errores);
            resultados.put("advertencias", advertencias);
            resultados.put("totalClientesValidados", clienteRepositorio.count());

        } catch (Exception e) {
            logger.error("Error al validar integridad de datos", e);
            errores.add("Error general: " + e.getMessage());
            resultados.put("valido", false);
            resultados.put("errores", errores);
        }

        return resultados;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Clientes nuevos en el período
            Long clientesNuevos = contarClientesNuevos(fechaInicio, fechaFin);
            estadisticas.put("clientesNuevos", clientesNuevos);

            // Clientes con compras
            List<ClienteReporteDTO> clientesConCompras = obtenerClientesConCompras(fechaInicio, fechaFin);
            estadisticas.put("clientesActivos", clientesConCompras.size());

            // Total de ventas del período
            BigDecimal totalVentas = clientesConCompras.stream()
                    .map(ClienteReporteDTO::getTotalCompras)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            estadisticas.put("totalVentasPeriodo", totalVentas);

            // Promedio de compras por cliente
            BigDecimal promedioCompras = clientesConCompras.isEmpty() ? BigDecimal.ZERO :
                    totalVentas.divide(BigDecimal.valueOf(clientesConCompras.size()), 2, RoundingMode.HALF_UP);
            estadisticas.put("promedioComprasPorCliente", promedioCompras);

            // Distribución por categoría
            Map<String, Long> clientesPorCategoria = clienteRepositorio.findAll().stream()
                    .filter(c -> c.getCategoria() != null)
                    .collect(Collectors.groupingBy(Cliente::getCategoria, Collectors.counting()));
            estadisticas.put("clientesPorCategoria", clientesPorCategoria);

            // Top 5 clientes
            List<ClienteReporteDTO> topClientes = clientesConCompras.stream()
                    .sorted((c1, c2) -> c2.getTotalCompras().compareTo(c1.getTotalCompras()))
                    .limit(5)
                    .collect(Collectors.toList());
            estadisticas.put("topClientes", topClientes);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas del período", e);
        }

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTotalClientes() {
        return clienteRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesSinVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener IDs de clientes con ventas en el período
        Set<Long> clientesConVentas = obtenerClientesConCompras(fechaInicio, fechaFin).stream()
                .map(ClienteReporteDTO::getId)
                .collect(Collectors.toSet());

        // Filtrar clientes sin ventas
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> !clientesConVentas.contains(cliente.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int actualizarCategoriaEnLote(List<Long> clienteIds, String nuevaCategoria) {
        int actualizados = 0;

        for (Long clienteId : clienteIds) {
            try {
                Cliente cliente = buscarPorId(clienteId);
                if (cliente != null) {
                    cliente.setCategoria(nuevaCategoria);
                    guardar(cliente);
                    actualizados++;
                }
            } catch (Exception e) {
                logger.error("Error al actualizar categoría del cliente {}", clienteId, e);
            }
        }

        logger.info("Actualizados {} de {} clientes con nueva categoría: {}",
                actualizados, clienteIds.size(), nuevaCategoria);

        return actualizados;
    }

    // Métodos auxiliares de exportación

    private String exportarComoJSON(List<Cliente> clientes) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(clientes);
    }

    private String exportarComoCSV(List<Cliente> clientes) {
        StringWriter writer = new StringWriter();

        // Encabezados
        writer.write("ID,RUT,Nombre,Apellido,Email,Telefono,Direccion,FechaRegistro,Categoria\n");

        // Datos
        for (Cliente cliente : clientes) {
            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    cliente.getId(),
                    cliente.getRut() != null ? cliente.getRut() : "",
                    cliente.getNombre() != null ? cliente.getNombre() : "",
                    cliente.getApellido() != null ? cliente.getApellido() : "",
                    cliente.getEmail() != null ? cliente.getEmail() : "",
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getDireccion() != null ? cliente.getDireccion().replace(",", ";") : "",
                    cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : "",
                    cliente.getCategoria() != null ? cliente.getCategoria() : ""
            ));
        }

        return writer.toString();
    }

    private String exportarComoXML(List<Cliente> clientes) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<clientes>\n");

        for (Cliente cliente : clientes) {
            xml.append("  <cliente>\n");
            xml.append("    <id>").append(cliente.getId()).append("</id>\n");
            xml.append("    <rut>").append(cliente.getRut()).append("</rut>\n");
            xml.append("    <nombre>").append(cliente.getNombre()).append("</nombre>\n");
            xml.append("    <apellido>").append(cliente.getApellido()).append("</apellido>\n");
            xml.append("    <email>").append(cliente.getEmail()).append("</email>\n");
            xml.append("  </cliente>\n");
        }

        xml.append("</clientes>");
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
}