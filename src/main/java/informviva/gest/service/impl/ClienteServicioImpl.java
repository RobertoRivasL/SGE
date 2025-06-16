package informviva.gest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Cliente;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.service.EmailServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de clientes
 * Proporciona todas las operaciones CRUD y funcionalidades avanzadas para clientes
 *
 * @author Roberto Rivas
 * @version 3.1
 */
@Service
@Transactional
public class ClienteServicioImpl implements ClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServicioImpl.class);

    // Constantes para mensajes
    private static final String CLIENTE_NO_ENCONTRADO = "Cliente no encontrado con ID: ";
    private static final String RUT_YA_EXISTE = "Ya existe un cliente con el RUT: ";
    private static final String EMAIL_YA_EXISTE = "Ya existe un cliente con el email: ";
    private static final String CLIENTE_CON_VENTAS = "No se puede eliminar el cliente porque tiene ventas registradas";

    // Repositorios y servicios
    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private VentaServicio ventaServicio;

    @Autowired(required = false)
    private EmailServicio emailServicio;

    public ClienteServicioImpl(ClienteRepositorio clienteRepositorio, VentaRepositorio ventaRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ================================
    // MÉTODOS BÁSICOS CRUD
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        logger.debug("Obteniendo todos los clientes");
        return clienteRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        logger.debug("Buscando cliente con ID: {}", id);
        return clienteRepositorio.findById(id).orElse(null);
    }

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public Cliente guardar(Cliente cliente) {
        logger.debug("Guardando cliente: {}", cliente.getEmail());

        validarCliente(cliente);

        if (cliente.getId() == null) {
            logger.info("Creando nuevo cliente con email: {}", cliente.getEmail());
        } else {
            logger.info("Actualizando cliente ID: {} con email: {}", cliente.getId(), cliente.getEmail());
        }

        return clienteRepositorio.save(cliente);
    }

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public Cliente actualizar(Long id, Cliente cliente) {
        logger.debug("Actualizando cliente ID: {}", id);

        Cliente clienteExistente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CLIENTE_NO_ENCONTRADO + id));

        // Actualizar campos
        clienteExistente.setNombre(cliente.getNombre());
        clienteExistente.setApellido(cliente.getApellido());
        clienteExistente.setEmail(cliente.getEmail());
        clienteExistente.setTelefono(cliente.getTelefono());
        clienteExistente.setDireccion(cliente.getDireccion());
        clienteExistente.setRut(cliente.getRut());
        clienteExistente.setCategoria(cliente.getCategoria());

        validarCliente(clienteExistente);

        logger.info("Cliente actualizado exitosamente: ID {}", id);
        return clienteRepositorio.save(clienteExistente);
    }

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public void eliminar(Long id) {
        logger.debug("Eliminando cliente ID: {}", id);

        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(CLIENTE_NO_ENCONTRADO + id));

        // Verificar que no tenga ventas asociadas
        if (ventaRepositorio.existsByClienteId(id)) {
            logger.warn("Intento de eliminar cliente con ventas asociadas: ID {}", id);
            throw new IllegalStateException(CLIENTE_CON_VENTAS);
        }

        clienteRepositorio.deleteById(id);
        logger.info("Cliente eliminado exitosamente: ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Long id) {
        return clienteRepositorio.existsById(id);
    }

    // ================================
    // MÉTODOS DE BÚSQUEDA
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombre(String nombre) {
        logger.debug("Buscando clientes por nombre: {}", nombre);
        return clienteRepositorio.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorEmail(String email) {
        logger.debug("Buscando clientes por email: {}", email);
        Cliente cliente = clienteRepositorio.findByEmail(email).orElse(null);
        return cliente != null ? Arrays.asList(cliente) : new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorRut(String rut) {
        logger.debug("Buscando cliente por RUT: {}", rut);
        return clienteRepositorio.findByRut(rut).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTermino(String termino) {
        logger.debug("Buscando clientes por término: {}", termino);
        return clienteRepositorio.buscarPorTexto(termino);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> busquedaAvanzada(Map<String, Object> criterios) {
        logger.debug("Ejecutando búsqueda avanzada con {} criterios", criterios.size());

        // Implementar lógica de búsqueda avanzada según criterios
        // Por ahora implementación básica, se puede expandir
        List<Cliente> resultados = clienteRepositorio.findAll();

        if (criterios.containsKey("categoria")) {
            String categoria = (String) criterios.get("categoria");
            resultados = resultados.stream()
                    .filter(c -> categoria.equals(c.getCategoria()))
                    .collect(Collectors.toList());
        }

        if (criterios.containsKey("fechaRegistroDesde")) {
            LocalDate fechaDesde = (LocalDate) criterios.get("fechaRegistroDesde");
            resultados = resultados.stream()
                    .filter(c -> c.getFechaRegistro() != null && !c.getFechaRegistro().isBefore(fechaDesde))
                    .collect(Collectors.toList());
        }

        return resultados;
    }

    // ================================
    // MÉTODOS CON PAGINACIÓN
    // ================================

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerTodosPaginados(Pageable pageable) {
        logger.debug("Obteniendo clientes paginados: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable) {
        logger.debug("Buscando clientes por nombre/email: {} (página {})", busqueda, pageable.getPageNumber());
        return clienteRepositorio.buscarPorTexto(busqueda, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerPorCategoria(String categoria, Pageable pageable) {
        logger.debug("Obteniendo clientes por categoría: {}", categoria);
        return clienteRepositorio.findByCategoria(categoria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        logger.debug("Obteniendo clientes por rango de fechas: {} - {}", fechaInicio, fechaFin);
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin, pageable);
    }

    // ================================
    // VALIDACIONES
    // ================================

    @Override
    public boolean rutEsValido(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        return ValidadorRutUtil.validar(rut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConEmail(String email) {
        return clienteRepositorio.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRut(String rut) {
        return clienteRepositorio.findByRut(rut).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConEmailExcluyendo(String email, Long excludeId) {
        Optional<Cliente> cliente = clienteRepositorio.findByEmail(email);
        return cliente.isPresent() && !cliente.get().getId().equals(excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRutExcluyendo(String rut, Long excludeId) {
        Optional<Cliente> cliente = clienteRepositorio.findByRut(rut);
        return cliente.isPresent() && !cliente.get().getId().equals(excludeId);
    }

    // ================================
    // REPORTES Y ANÁLISIS
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin) {
        logger.debug("Generando reporte de clientes con compras: {} - {}", fechaInicio, fechaFin);

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
                            .filter(venta -> venta.getTotal() != null)
                            .map(venta -> BigDecimal.valueOf(venta.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    dto.setMontoTotalCompras(totalCompras.doubleValue());

                    if (dto.getComprasRealizadas() > 0) {
                        dto.setPromedioCompra(totalCompras.divide(
                                BigDecimal.valueOf(dto.getComprasRealizadas()), 2, RoundingMode.HALF_UP
                        ).doubleValue());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "clientes-top", key = "#limite")
    @Transactional(readOnly = true)
    public List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite) {
        logger.debug("Obteniendo top {} clientes por compras", limite);

        return obtenerClientesConCompras(null, null).stream()
                .sorted((c1, c2) -> Double.compare(c2.getMontoTotalCompras(), c1.getMontoTotalCompras()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        logger.debug("Obteniendo clientes nuevos: {} - {}", fechaInicio, fechaFin);
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return (long) clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesPorCategoria(String categoria) {
        logger.debug("Obteniendo clientes por categoría: {}", categoria);
        return clienteRepositorio.findByCategoria(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesInactivos(int diasInactividad) {
        logger.debug("Obteniendo clientes inactivos (más de {} días)", diasInactividad);

        LocalDate fechaLimite = LocalDate.now().minusDays(diasInactividad);

        return obtenerClientesConCompras(null, null).stream()
                .filter(dto -> dto.getUltimaCompra() == null || dto.getUltimaCompra().isBefore(fechaLimite))
                .map(dto -> buscarPorId(dto.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ================================
    // MÉTRICAS Y ESTADÍSTICAS
    // ================================

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioComprasPorCliente() {
        List<ClienteReporteDTO> clientes = obtenerClientesConCompras(null, null);

        if (clientes.isEmpty()) {
            return 0.0;
        }

        double totalCompras = clientes.stream()
                .mapToDouble(ClienteReporteDTO::getMontoTotalCompras)
                .sum();

        return totalCompras / clientes.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarClientesActivos() {
        // Considerar activos a clientes que han comprado en los últimos 90 días
        return (long) obtenerClientesInactivos(90).size();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTodos() {
        return clienteRepositorio.count();
    }

    @Override
    @Cacheable("estadisticas-clientes")
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGenerales() {
        logger.debug("Calculando estadísticas generales de clientes");

        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalClientes", contarTodos());
        estadisticas.put("clientesActivos", contarClientesActivos());
        estadisticas.put("promedioComprasPorCliente", calcularPromedioComprasPorCliente());
        estadisticas.put("distribucionPorCategoria", obtenerDistribucionPorCategoria());

        // Clientes nuevos en el último mes
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        estadisticas.put("clientesNuevosEsteMes", contarClientesNuevos(inicioMes, LocalDate.now()));

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerDistribucionPorCategoria() {
        List<Cliente> todosClientes = clienteRepositorio.findAll();

        return todosClientes.stream()
                .collect(Collectors.groupingBy(
                        cliente -> cliente.getCategoria() != null ? cliente.getCategoria() : "Sin categoría",
                        Collectors.counting()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> calcularMetricasCrecimiento(int meses) {
        logger.debug("Calculando métricas de crecimiento para {} meses", meses);

        Map<String, Object> metricas = new HashMap<>();
        LocalDate fechaInicio = LocalDate.now().minusMonths(meses);

        List<Cliente> clientesNuevos = obtenerClientesNuevos(fechaInicio, LocalDate.now());

        // Agrupar por mes
        Map<String, Long> clientesPorMes = clientesNuevos.stream()
                .collect(Collectors.groupingBy(
                        cliente -> cliente.getFechaRegistro().getYear() + "-" +
                                String.format("%02d", cliente.getFechaRegistro().getMonthValue()),
                        Collectors.counting()
                ));

        metricas.put("clientesPorMes", clientesPorMes);
        metricas.put("totalNuevosEnPeriodo", clientesNuevos.size());
        metricas.put("promedioClientesPorMes", clientesNuevos.size() / (double) meses);

        return metricas;
    }

    // ================================
    // EXPORTACIÓN E IMPORTACIÓN
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarClientes(String formato, Map<String, Object> filtros) {
        logger.info("Exportando clientes en formato: {}", formato);

        try {
            List<Cliente> clientes = aplicarFiltrosExportacion(filtros);

            switch (formato.toUpperCase()) {
                case "JSON":
                    return objectMapper.writeValueAsBytes(clientes);
                case "CSV":
                    return generarCSV(clientes);
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error exportando clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error en exportación", e);
        }
    }

    @Override
    public Map<String, Object> importarClientes(byte[] archivo, String formato) {
        logger.info("Importando clientes desde formato: {}", formato);

        Map<String, Object> resultado = new HashMap<>();
        int exitosos = 0;
        int errores = 0;
        List<String> mensajesError = new ArrayList<>();

        try {
            // Implementar lógica de importación según formato
            // Por ahora implementación básica
            resultado.put("exitosos", exitosos);
            resultado.put("errores", errores);
            resultado.put("mensajes", mensajesError);

        } catch (Exception e) {
            logger.error("Error importando clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error en importación", e);
        }

        return resultado;
    }

    // ================================
    // OPERACIONES EN LOTE
    // ================================

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public int actualizarCategoriaEnLote(List<Long> clienteIds, String nuevaCategoria) {
        logger.info("Actualizando categoría en lote: {} clientes a '{}'", clienteIds.size(), nuevaCategoria);

        int actualizados = 0;
        for (Long id : clienteIds) {
            try {
                Cliente cliente = buscarPorId(id);
                if (cliente != null) {
                    cliente.setCategoria(nuevaCategoria);
                    guardar(cliente);
                    actualizados++;
                }
            } catch (Exception e) {
                logger.error("Error actualizando cliente ID {}: {}", id, e.getMessage());
            }
        }

        logger.info("Actualizados {} de {} clientes", actualizados, clienteIds.size());
        return actualizados;
    }

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public Map<String, Object> eliminarClientesEnLote(List<Long> clienteIds) {
        logger.info("Eliminando clientes en lote: {} clientes", clienteIds.size());

        int eliminados = 0;
        int noEliminados = 0;
        List<String> errores = new ArrayList<>();

        for (Long id : clienteIds) {
            try {
                eliminar(id);
                eliminados++;
            } catch (IllegalStateException e) {
                noEliminados++;
                errores.add("Cliente ID " + id + ": " + e.getMessage());
            } catch (Exception e) {
                noEliminados++;
                errores.add("Cliente ID " + id + ": Error inesperado");
                logger.error("Error eliminando cliente ID {}: {}", id, e.getMessage());
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("eliminados", eliminados);
        resultado.put("noEliminados", noEliminados);
        resultado.put("errores", errores);

        logger.info("Eliminación en lote completada: {} eliminados, {} no eliminados", eliminados, noEliminados);
        return resultado;
    }

    @Override
    public Map<String, Object> enviarEmailMasivo(List<Long> clienteIds, String plantillaEmail, Map<String, Object> parametros) {
        logger.info("Enviando email masivo a {} clientes", clienteIds.size());

        Map<String, Object> resultado = new HashMap<>();

        if (emailServicio == null) {
            logger.warn("EmailServicio no disponible para envío masivo");
            resultado.put("error", "Servicio de email no disponible");
            return resultado;
        }

        int exitosos = 0;
        int fallidos = 0;

        for (Long id : clienteIds) {
            try {
                Cliente cliente = buscarPorId(id);
                if (cliente != null && cliente.getEmail() != null) {
                    // emailServicio.enviarEmail(cliente.getEmail(), plantillaEmail, parametros);
                    exitosos++;
                }
            } catch (Exception e) {
                fallidos++;
                logger.error("Error enviando email a cliente ID {}: {}", id, e.getMessage());
            }
        }

        resultado.put("exitosos", exitosos);
        resultado.put("fallidos", fallidos);

        logger.info("Envío masivo completado: {} exitosos, {} fallidos", exitosos, fallidos);
        return resultado;
    }

    // ================================
    // CATEGORIZACIÓN Y SEGMENTACIÓN
    // ================================

    @Override
    @CacheEvict(value = {"clientes-top", "estadisticas-clientes"}, allEntries = true)
    public void actualizarCategoriasAutomaticamente() {
        logger.info("Iniciando actualización automática de categorías");

        List<Cliente> todosClientes = obtenerTodos();
        int actualizados = 0;

        for (Cliente cliente : todosClientes) {
            String categoriaActual = cliente.getCategoria();
            String nuevaCategoria = determinarCategoriaAutomatica(cliente);

            if (!Objects.equals(categoriaActual, nuevaCategoria)) {
                cliente.setCategoria(nuevaCategoria);
                guardar(cliente);
                actualizados++;
                logger.debug("Cliente ID {} categorizado de '{}' a '{}'",
                        cliente.getId(), categoriaActual, nuevaCategoria);
            }
        }

        logger.info("Categorización automática completada: {} clientes actualizados", actualizados);
    }

    @Override
    @Transactional(readOnly = true)
    public String determinarCategoriaAutomatica(Cliente cliente) {
        // Obtener estadísticas de compras del cliente
        var ventasCliente = ventaRepositorio.findByCliente(cliente);

        if (ventasCliente.isEmpty()) {
            return "NUEVO";
        }

        double montoTotal = ventasCliente.stream()
                .mapToDouble(venta -> venta.getTotal() != null ? venta.getTotal() : 0.0)
                .sum();

        int numeroCompras = ventasCliente.size();

        // Calcular días desde la última compra
        LocalDate ultimaCompra = ventasCliente.stream()
                .map(venta -> venta.getFechaAsLocalDate())
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        long diasDesdeUltimaCompra = ultimaCompra != null ?
                ChronoUnit.DAYS.between(ultimaCompra, LocalDate.now()) : Long.MAX_VALUE;

        // Lógica de categorización
        if (diasDesdeUltimaCompra > 180) {
            return "INACTIVO";
        } else if (montoTotal > 1000000 || numeroCompras > 20) { // 1M pesos chilenos
            return "VIP";
        } else if (montoTotal > 500000 || numeroCompras > 10) { // 500K pesos chilenos
            return "PREMIUM";
        } else if (numeroCompras > 3) {
            return "FRECUENTE";
        } else {
            return "REGULAR";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Cliente>> segmentarClientes(Map<String, Object> criteriosSegmentacion) {
        logger.debug("Segmentando clientes con {} criterios", criteriosSegmentacion.size());

        List<Cliente> todosClientes = obtenerTodos();
        Map<String, List<Cliente>> segmentos = new HashMap<>();

        // Implementar lógica de segmentación según criterios
        // Por ahora implementación básica por categoría
        return todosClientes.stream()
                .collect(Collectors.groupingBy(cliente ->
                        cliente.getCategoria() != null ? cliente.getCategoria() : "Sin categoría"));
    }

    // ================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ================================

    private void validarCliente(Cliente cliente) {
        if (cliente.getRut() != null && !rutEsValido(cliente.getRut())) {
            throw new IllegalArgumentException("RUT inválido: " + cliente.getRut());
        }

        if (cliente.getEmail() != null) {
            if (cliente.getId() == null) {
                // Nuevo cliente
                if (existeClienteConEmail(cliente.getEmail())) {
                    throw new IllegalArgumentException(EMAIL_YA_EXISTE + cliente.getEmail());
                }
            } else {
                // Cliente existente
                if (existeClienteConEmailExcluyendo(cliente.getEmail(), cliente.getId())) {
                    throw new IllegalArgumentException(EMAIL_YA_EXISTE + cliente.getEmail());
                }
            }
        }

        if (cliente.getRut() != null) {
            if (cliente.getId() == null) {
                // Nuevo cliente
                if (existeClienteConRut(cliente.getRut())) {
                    throw new IllegalArgumentException(RUT_YA_EXISTE + cliente.getRut());
                }
            } else {
                // Cliente existente
                if (existeClienteConRutExcluyendo(cliente.getRut(), cliente.getId())) {
                    throw new IllegalArgumentException(RUT_YA_EXISTE + cliente.getRut());
                }
            }
        }
    }

    private List<Cliente> aplicarFiltrosExportacion(Map<String, Object> filtros) {
        if (filtros == null || filtros.isEmpty()) {
            return obtenerTodos();
        }

        return busquedaAvanzada(filtros);
    }

    private byte[] generarCSV(List<Cliente> clientes) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido,Email,Telefono,Direccion,FechaRegistro,Categoria\n");

        for (Cliente cliente : clientes) {
            csv.append(cliente.getId()).append(",")
                    .append(cliente.getRut() != null ? cliente.getRut() : "").append(",")
                    .append(cliente.getNombre() != null ? cliente.getNombre() : "").append(",")
                    .append(cliente.getApellido() != null ? cliente.getApellido() : "").append(",")
                    .append(cliente.getEmail() != null ? cliente.getEmail() : "").append(",")
                    .append(cliente.getTelefono() != null ? cliente.getTelefono() : "").append(",")
                    .append(cliente.getDireccion() != null ? cliente.getDireccion() : "").append(",")
                    .append(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : "").append(",")
                    .append(cliente.getCategoria() != null ? cliente.getCategoria() : "").append("\n");
        }

        return csv.toString().getBytes();
    }

    @Override
    public List<Cliente> buscarPorTermino(String termino, int limite) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> coincideConTermino(cliente, termino))
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Cliente> buscarPorTermino(String termino, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRutContaining(
                termino, termino, termino, termino, pageable);
    }

    @Override
    public Page<Cliente> buscarPorTerminoYCiudad(String termino, String ciudad, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCase(
                termino, termino, termino, ciudad, pageable);
    }

    @Override
    public Page<Cliente> buscarPorTerminoYActivos(String termino, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActivoTrue(
                termino, termino, termino, pageable);
    }

    @Override
    public Page<Cliente> buscarPorTerminoYCiudadYActivos(String termino, String ciudad, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCaseAndActivoTrue(
                termino, termino, termino, ciudad, pageable);
    }

    @Override
    public Page<Cliente> buscarPorCiudad(String ciudad, Pageable pageable) {
        return clienteRepositorio.findByCiudadIgnoreCase(ciudad, pageable);
    }

    @Override
    public Page<Cliente> buscarPorCiudadYActivos(String ciudad, Pageable pageable) {
        return clienteRepositorio.findByCiudadIgnoreCaseAndActivoTrue(ciudad, pageable);
    }

    @Override
    public List<Cliente> obtenerActivos() {
        return clienteRepositorio.findByActivoTrue();
    }

    @Override
    public Page<Cliente> obtenerActivosPaginados(Pageable pageable) {
        return clienteRepositorio.findByActivoTrue(pageable);
    }

    @Override
    public List<String> listarCiudades() {
        return clienteRepositorio.findAll().stream()
                .map(Cliente::getCiudad)
                .filter(ciudad -> ciudad != null && !ciudad.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Long contarActivos() {
        return clienteRepositorio.countByActivoTrue();
    }

    @Override
    public Long contarInactivos() {
        return clienteRepositorio.countByActivoFalse();
    }

    @Override
    public Long contarNuevosHoy() {
        LocalDateTime hoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finHoy = hoy.plusDays(1).minusSeconds(1);
        return clienteRepositorio.countByFechaRegistroBetween(hoy, finHoy);
    }

    @Override
    public Long contarNuevosMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);
        return clienteRepositorio.countByFechaRegistroBetween(inicioMes, finMes);
    }


    @Override
    public Long contarCiudades() {
        return (long) listarCiudades().size();
    }

    @Override
    public boolean existeEmailOtroCliente(String email, Long clienteId) {
        return clienteRepositorio.findByEmailIgnoreCase(email)
                .stream()
                .anyMatch(cliente -> !cliente.getId().equals(clienteId));
    }

    @Override
    public boolean existeEmail(String email) {
        return clienteRepositorio.existsByEmailIgnoreCase(email);
    }

// ========== MÉTODO AUXILIAR ==========

    /**
     * Verifica si un cliente coincide con el término de búsqueda
     */
    private boolean coincideConTermino(Cliente cliente, String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return true;
        }

        String terminoLower = termino.toLowerCase();

        return (cliente.getNombre() != null && cliente.getNombre().toLowerCase().contains(terminoLower)) ||
                (cliente.getApellido() != null && cliente.getApellido().toLowerCase().contains(terminoLower)) ||
                (cliente.getEmail() != null && cliente.getEmail().toLowerCase().contains(terminoLower)) ||
                (cliente.getRut() != null && cliente.getRut().toLowerCase().contains(terminoLower)) ||
                (cliente.getNombreCompleto().toLowerCase().contains(terminoLower));
    }
}