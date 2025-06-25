package informviva.gest.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación completa del servicio para la gestión de clientes
 * Refactorizada siguiendo principios SOLID y mejores prácticas
 *
 * @author Roberto Rivas
 * @version 5.0
 */
@Service
@Transactional
public class ClienteServicioImpl implements ClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServicioImpl.class);

    // Constantes para mejorar mantenibilidad
    private static final String ESTADO_ANULADA = "ANULADA";
    private static final String FORMATO_JSON = "JSON";
    private static final String FORMATO_CSV = "CSV";
    private static final int DEFAULT_LIMIT = 10;

    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;
    private final ObjectMapper objectMapper;

    // Constructor injection (Principio de Inversión de Dependencias)
    public ClienteServicioImpl(ClienteRepositorio clienteRepositorio, VentaRepositorio ventaRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.objectMapper = createObjectMapper();
    }

    // ========== MÉTODOS BÁSICOS CRUD ==========

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        logger.debug("Obteniendo todos los clientes");
        return clienteRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        if (!esIdValido(id)) {
            logger.warn("ID de cliente es nulo o inválido: {}", id);
            return null;
        }
        logger.debug("Buscando cliente por ID: {}", id);
        return clienteRepositorio.findById(id).orElse(null);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        validarCliente(cliente);
        String nombreCompleto = obtenerNombreSeguro(cliente);
        logger.debug("Guardando cliente: {}", nombreCompleto);
        return clienteRepositorio.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        if (!esIdValido(id)) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        logger.debug("Eliminando cliente con ID: {}", id);
        clienteRepositorio.deleteById(id);
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    @Override
    @Transactional(readOnly = true)
    public boolean rutEsValido(String rut) {
        return ValidadorRutUtil.validar(rut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConEmail(String email) {
        if (!esTextoValido(email)) {
            return false;
        }
        return clienteRepositorio.existsByEmail(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRut(String rut) {
        if (!esTextoValido(rut)) {
            return false;
        }
        return clienteRepositorio.findByRut(rut.trim()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorRut(String rut) {
        return existeClienteConRut(rut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConEmailExcluyendo(String email, Long id) {
        if (!esTextoValido(email) || !esIdValido(id)) {
            return false;
        }
        return clienteRepositorio.existeClienteConEmailExcluyendo(email.trim(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRutExcluyendo(String rut, Long id) {
        if (!esTextoValido(rut) || !esIdValido(id)) {
            return false;
        }
        return clienteRepositorio.existeClienteConRutExcluyendo(rut.trim(), id);
    }

    // ===== MÉTODOS ADICIONALES PARA CONTROLADORES =====

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return existeClienteConEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmailOtroCliente(String email, Long id) {
        return existeClienteConEmailExcluyendo(email, id);
    }

    // ========== MÉTODOS DE BÚSQUEDA ==========

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorRut(String rut) {
        if (!esTextoValido(rut)) {
            return null;
        }
        logger.debug("Buscando cliente por RUT: {}", rut);
        return clienteRepositorio.findByRut(rut.trim()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombre(String nombre) {
        if (!esTextoValido(nombre)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por nombre: {}", nombre);
        return clienteRepositorio.findByNombreContainingIgnoreCase(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorApellido(String apellido) {
        if (!esTextoValido(apellido)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por apellido: {}", apellido);
        return clienteRepositorio.findByApellidoContainingIgnoreCase(apellido.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorEmail(String email) {
        if (!esTextoValido(email)) {
            return Optional.empty();
        }
        logger.debug("Buscando cliente por email: {}", email);
        return clienteRepositorio.findByEmail(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTelefono(String telefono) {
        if (!esTextoValido(telefono)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por teléfono: {}", telefono);
        Optional<Cliente> cliente = clienteRepositorio.findByTelefono(telefono.trim());
        return cliente.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorDireccion(String direccion) {
        if (!esTextoValido(direccion)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por dirección: {}", direccion);
        return clienteRepositorio.findByDireccionContainingIgnoreCase(direccion.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTermino(String termino) {
        if (!esTextoValido(termino)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por término: {}", termino);
        return clienteRepositorio.buscarPorTexto(termino.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTermino(String termino, int limite) {
        List<Cliente> todos = buscarPorTermino(termino);
        return limitarResultados(todos, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorTermino(String termino, Pageable pageable) {
        if (!esTextoValido(termino)) {
            return clienteRepositorio.findAll(pageable);
        }
        logger.debug("Buscando clientes por término paginado: {}", termino);
        return clienteRepositorio.buscarPorTexto(termino.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable) {
        if (!esTextoValido(busqueda)) {
            return clienteRepositorio.findAll(pageable);
        }
        logger.debug("Buscando clientes por nombre o email: {}", busqueda);
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
                busqueda.trim(), busqueda.trim(), pageable);
    }

    // ========== MÉTODOS POR CATEGORÍA Y ESTADO ==========

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesPorCategoria(String categoria) {
        if (!esTextoValido(categoria)) {
            return new ArrayList<>();
        }
        logger.debug("Obteniendo clientes por categoría: {}", categoria);
        return clienteRepositorio.findByCategoria(categoria.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorCategorias(List<String> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por categorías: {}", categorias);
        return clienteRepositorio.findByCategoriaIn(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorEstadoActivo(boolean activo) {
        logger.debug("Buscando clientes por estado activo: {}", activo);
        return clienteRepositorio.findByActivo(activo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerActivos() {
        logger.debug("Obteniendo clientes activos");
        return clienteRepositorio.findByActivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerActivosPaginados(Pageable pageable) {
        logger.debug("Obteniendo clientes activos paginados");
        return clienteRepositorio.findByActivo(true, pageable);
    }

    // ========== MÉTODOS POR FECHAS ==========

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin) {
        if (!sonFechasValidas(fechaInicio, fechaFin)) {
            return new ArrayList<>();
        }
        logger.debug("Buscando clientes por fecha de registro: {} - {}", fechaInicio, fechaFin);
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorFechaRegistro(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesRegistradosRecientemente(LocalDate fechaInicio) {
        if (fechaInicio == null) {
            return new ArrayList<>();
        }
        logger.debug("Obteniendo clientes registrados desde: {}", fechaInicio);
        return clienteRepositorio.findByFechaRegistroAfter(fechaInicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorEdadEntre(int edadMin, int edadMax) {
        logger.debug("Buscando clientes por edad entre: {} - {}", edadMin, edadMax);
        LocalDate fechaMax = LocalDate.now().minusYears(edadMin);
        LocalDate fechaMin = LocalDate.now().minusYears(edadMax + 1L);
        return clienteRepositorio.findByFechaNacimientoBetween(fechaMin, fechaMax);
    }

    // ========== MÉTODOS DE PAGINACIÓN ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerTodosPaginados(Pageable pageable) {
        logger.debug("Obteniendo todos los clientes paginados");
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientesPaginadas(Pageable pageable) {
        logger.debug("Listando clientes paginados");
        return clienteRepositorio.findAll(pageable);
    }

    // ========== MÉTODOS DE CONTEO ==========

    @Override
    @Transactional(readOnly = true)
    public Long contarTotalClientes() {
        logger.debug("Contando total de clientes");
        return clienteRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTodos() {
        return contarTotalClientes();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        if (!sonFechasValidas(fechaInicio, fechaFin)) {
            return 0L;
        }
        logger.debug("Contando clientes nuevos: {} - {}", fechaInicio, fechaFin);
        return clienteRepositorio.countByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarClientesActivos() {
        logger.debug("Contando clientes activos");
        return clienteRepositorio.countByActivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivos() {
        return clienteRepositorio.countByActivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarInactivos() {
        return clienteRepositorio.countByActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNuevosHoy() {
        LocalDate hoy = LocalDate.now();
        return clienteRepositorio.countByFechaRegistroBetween(hoy, hoy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNuevosMes() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        return clienteRepositorio.countByFechaRegistroBetween(inicioMes, finMes);
    }

    // ========== MÉTODOS DE REPORTES ==========

    @Override
    @Transactional(readOnly = true)
    public List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin) {
        logger.debug("Obteniendo clientes con compras: {} - {}", fechaInicio, fechaFin);
        List<Cliente> clientes = clienteRepositorio.findClientesConComprasEntreFechas(fechaInicio, fechaFin);
        return convertirAClienteReporteDTOs(clientes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite) {
        logger.debug("Obteniendo top {} clientes por compras", limite);
        Pageable pageable = PageRequest.of(0, Math.max(limite, 1));
        List<Cliente> topClientes = clienteRepositorio.findTopClientesPorCompras(pageable);
        return convertirAClienteReporteDTOs(topClientes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesSinVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (!sonFechasValidas(fechaInicio, fechaFin)) {
            return new ArrayList<>();
        }
        logger.debug("Obteniendo clientes sin ventas: {} - {}", fechaInicio, fechaFin);
        return clienteRepositorio.findClientesSinVentas(fechaInicio, fechaFin);
    }

    // ========== MÉTODOS DE ANÁLISIS ==========

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioComprasPorCliente() {
        logger.debug("Calculando promedio de compras por cliente");
        List<Cliente> clientes = clienteRepositorio.findAll();
        if (clientes.isEmpty()) {
            return 0.0;
        }

        double totalCompras = clientes.stream()
                .mapToDouble(this::calcularTotalComprasCliente)
                .sum();

        return totalCompras / clientes.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        logger.debug("Obteniendo estadísticas para período: {} - {}", fechaInicio, fechaFin);

        Map<String, Object> estadisticas = new HashMap<>();
        long clientesNuevos = sonFechasValidas(fechaInicio, fechaFin) ?
                clienteRepositorio.countByFechaRegistroBetween(fechaInicio, fechaFin) : 0;

        estadisticas.put("clientesNuevos", clientesNuevos);
        estadisticas.put("totalClientes", clienteRepositorio.count());
        estadisticas.put("clientesActivos", clienteRepositorio.countByActivo(true));
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);

        return estadisticas;
    }

    // ========== MÉTODOS DE BÚSQUEDA POR CIUDAD ==========

    @Override
    @Transactional(readOnly = true)
    public List<String> listarCiudades() {
        logger.debug("Listando ciudades disponibles");
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> esTextoValido(cliente.getDireccion()))
                .map(cliente -> extraerCiudadDeDireccion(cliente.getDireccion()))
                .filter(Objects::nonNull)
                .filter(ciudad -> !ciudad.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarCiudades() {
        return (long) listarCiudades().size();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorCiudad(String ciudad, Pageable pageable) {
        if (!esTextoValido(ciudad)) {
            return Page.empty(pageable);
        }

        // Implementación alternativa usando búsqueda por dirección
        List<Cliente> clientes = clienteRepositorio.findByDireccionContainingIgnoreCase(ciudad.trim());
        return convertirListaAPage(clientes, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorCiudadYActivos(String ciudad, Pageable pageable) {
        if (!esTextoValido(ciudad)) {
            return clienteRepositorio.findByActivo(true, pageable);
        }

        List<Cliente> clientes = clienteRepositorio.findByDireccionContainingIgnoreCase(ciudad.trim())
                .stream()
                .filter(this::esClienteActivo)
                .collect(Collectors.toList());

        return convertirListaAPage(clientes, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorTerminoYActivos(String termino, Pageable pageable) {
        if (!esTextoValido(termino)) {
            return clienteRepositorio.findByActivo(true, pageable);
        }

        List<Cliente> todos = clienteRepositorio.buscarPorTexto(termino.trim());
        List<Cliente> activos = todos.stream()
                .filter(this::esClienteActivo)
                .collect(Collectors.toList());

        return convertirListaAPage(activos, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorTerminoYCiudad(String termino, String ciudad, Pageable pageable) {
        List<Cliente> resultado = obtenerClientesPorFiltros(termino, ciudad, null);
        return convertirListaAPage(resultado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorTerminoYCiudadYActivos(String termino, String ciudad, Pageable pageable) {
        List<Cliente> resultado = obtenerClientesPorFiltros(termino, ciudad, true);
        return convertirListaAPage(resultado, pageable);
    }

    // ========== MÉTODOS DE IMPORTACIÓN/EXPORTACIÓN ==========

    @Override
    public String exportarDatos(String formato, LocalDate fechaInicio, LocalDate fechaFin) {
        logger.info("Exportando datos de clientes en formato: {}", formato);
        List<Cliente> clientes = obtenerClientesParaExportar(fechaInicio, fechaFin);
        return exportarDatos(formato, clientes);
    }

    @Override
    public String exportarDatos(String formato, List<Cliente> clientes) {
        validarParametrosExportacion(formato, clientes);

        try {
            return procesarExportacion(formato.toUpperCase(), clientes);
        } catch (Exception e) {
            logger.error("Error al exportar datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en exportación", e);
        }
    }

    @Override
    public int importarDatos(String datos, String formato) {
        logger.info("Importando datos de clientes en formato: {}", formato);

        try {
            return procesarImportacion(datos, formato.toUpperCase());
        } catch (Exception e) {
            logger.error("Error importando datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en importación", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validarIntegridadDatos() {
        logger.info("Validando integridad de datos de clientes");

        long totalClientes = clienteRepositorio.count();
        long clientesSinEmail = clienteRepositorio.countByEmailIsNullOrEmailEquals("");
        long clientesSinRut = clienteRepositorio.countByRutIsNullOrRutEquals("");
        long clientesSinNombre = clienteRepositorio.countByNombreIsNullOrNombreEquals("");

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalClientes", totalClientes);
        resultado.put("clientesSinEmail", clientesSinEmail);
        resultado.put("clientesSinRut", clientesSinRut);
        resultado.put("clientesSinNombre", clientesSinNombre);
        resultado.put("porcentajeIntegridad",
                calcularPorcentajeIntegridad(totalClientes, clientesSinEmail, clientesSinRut, clientesSinNombre));

        return resultado;
    }

    // ========== MÉTODOS DE ACTUALIZACIÓN MASIVA ==========

    @Override
    public int actualizarCategoriaEnLote(List<Long> clienteIds, String nuevaCategoria) {
        if (clienteIds == null || clienteIds.isEmpty() || nuevaCategoria == null) {
            return 0;
        }

        logger.info("Actualizando categoría en lote: {} clientes", clienteIds.size());
        List<Cliente> clientes = clienteRepositorio.findAllById(clienteIds);

        clientes.forEach(cliente -> cliente.setCategoria(nuevaCategoria));
        clienteRepositorio.saveAll(clientes);

        return clientes.size();
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS (Principio de Responsabilidad Única) ==========

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private boolean esIdValido(Long id) {
        return id != null && id > 0;
    }

    private boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    private boolean sonFechasValidas(LocalDate fechaInicio, LocalDate fechaFin) {
        return fechaInicio != null && fechaFin != null;
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
    }

    private String obtenerNombreSeguro(Cliente cliente) {
        return cliente.getNombreCompleto() != null ?
                cliente.getNombreCompleto() : "Cliente sin nombre";
    }

    private List<Cliente> limitarResultados(List<Cliente> clientes, int limite) {
        return clientes.stream()
                .limit(Math.max(limite, 0))
                .collect(Collectors.toList());
    }

    private List<ClienteReporteDTO> convertirAClienteReporteDTOs(List<Cliente> clientes) {
        return clientes.stream()
                .map(this::convertirAClienteReporteDTO)
                .collect(Collectors.toList());
    }

    private ClienteReporteDTO convertirAClienteReporteDTO(Cliente cliente) {
        List<Venta> compras = ventaRepositorio.findByClienteId(cliente.getId());

        ComprasEstadisticas estadisticas = calcularEstadisticasCompras(compras);
        LocalDate fechaRegistroLocal = extraerFechaRegistro(cliente);

        return new ClienteReporteDTO(
                cliente.getId(),
                cliente.getRut(),
                cliente.getNombreCompleto(),
                cliente.getEmail(),
                fechaRegistroLocal,
                estadisticas.comprasRealizadas,
                estadisticas.totalCompras,
                estadisticas.promedioPorCompra,
                estadisticas.ultimaCompra
        );
    }

    // Clase interna para encapsular estadísticas de compras (Principio de Responsabilidad Única)
    private static class ComprasEstadisticas {
        final int comprasRealizadas;
        final BigDecimal totalCompras;
        final BigDecimal promedioPorCompra;
        final LocalDate ultimaCompra;

        ComprasEstadisticas(int comprasRealizadas, BigDecimal totalCompras,
                            BigDecimal promedioPorCompra, LocalDate ultimaCompra) {
            this.comprasRealizadas = comprasRealizadas;
            this.totalCompras = totalCompras;
            this.promedioPorCompra = promedioPorCompra;
            this.ultimaCompra = ultimaCompra;
        }
    }

    private ComprasEstadisticas calcularEstadisticasCompras(List<Venta> compras) {
        int comprasRealizadas = compras.size();
        BigDecimal totalCompras = compras.stream()
                .map(venta -> BigDecimal.valueOf(venta.getTotal() != null ? venta.getTotal() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioPorCompra = comprasRealizadas > 0
                ? totalCompras.divide(BigDecimal.valueOf(comprasRealizadas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate ultimaCompra = compras.stream()
                .filter(venta -> venta.getFecha() != null)
                .map(venta -> venta.getFecha().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);

        return new ComprasEstadisticas(comprasRealizadas, totalCompras, promedioPorCompra, ultimaCompra);
    }

    private LocalDate extraerFechaRegistro(Cliente cliente) {
        return cliente.getFechaRegistro() != null ?
                cliente.getFechaRegistro().toLocalDate() : null;
    }

    private double calcularTotalComprasCliente(Cliente cliente) {
        return ventaRepositorio.findByClienteId(cliente.getId()).stream()
                .filter(venta -> venta.getTotal() != null)
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    private boolean esClienteActivo(Cliente cliente) {
        // Implementación flexible para diferentes estructuras de la entidad Cliente
        try {
            // Intentar métodos comunes para obtener estado activo
            if (cliente.getClass().getMethod("isActivo") != null) {
                return (Boolean) cliente.getClass().getMethod("isActivo").invoke(cliente);
            }
        } catch (Exception e) {
            // Si no existe el método, asumir activo por defecto
            logger.debug("No se pudo determinar estado activo para cliente {}, asumiendo activo", cliente.getId());
        }
        return true; // Valor por defecto
    }

    private String extraerCiudadDeDireccion(String direccion) {
        if (!esTextoValido(direccion)) {
            return null;
        }

        String[] partes = direccion.split(",");
        return partes.length > 1 ?
                partes[partes.length - 1].trim() :
                direccion.trim();
    }

    private List<Cliente> obtenerClientesPorFiltros(String termino, String ciudad, Boolean soloActivos) {
        List<Cliente> resultado = new ArrayList<>();

        // Aplicar filtro de término
        if (esTextoValido(termino)) {
            resultado = clienteRepositorio.buscarPorTexto(termino.trim());
        } else {
            resultado = clienteRepositorio.findAll();
        }

        // Aplicar filtro de ciudad
        if (esTextoValido(ciudad)) {
            resultado = resultado.stream()
                    .filter(cliente -> cliente.getDireccion() != null &&
                            cliente.getDireccion().toLowerCase().contains(ciudad.trim().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Aplicar filtro de activos si es necesario
        if (Boolean.TRUE.equals(soloActivos)) {
            resultado = resultado.stream()
                    .filter(this::esClienteActivo)
                    .collect(Collectors.toList());
        }

        return resultado;
    }

    private Page<Cliente> convertirListaAPage(List<Cliente> clientes, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clientes.size());

        List<Cliente> subList = start >= clientes.size() ?
                new ArrayList<>() : clientes.subList(start, end);

        return new PageImpl<>(subList, pageable, clientes.size());
    }

    private List<Cliente> obtenerClientesParaExportar(LocalDate fechaInicio, LocalDate fechaFin) {
        return sonFechasValidas(fechaInicio, fechaFin) ?
                clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin) :
                clienteRepositorio.findAll();
    }

    private void validarParametrosExportacion(String formato, List<Cliente> clientes) {
        if (formato == null || clientes == null) {
            throw new IllegalArgumentException("Formato y lista de clientes no pueden ser nulos");
        }
    }

    private String procesarExportacion(String formato, List<Cliente> clientes) throws Exception {
        switch (formato) {
            case FORMATO_JSON:
                return objectMapper.writeValueAsString(clientes);
            case FORMATO_CSV:
                return generarCSV(clientes);
            default:
                throw new IllegalArgumentException("Formato no soportado: " + formato);
        }
    }

    private int procesarImportacion(String datos, String formato) throws IOException {
        switch (formato) {
            case FORMATO_JSON:
                return importarDesdeJSON(datos);
            case FORMATO_CSV:
                return importarDesdeCSV(datos);
            default:
                throw new IllegalArgumentException("Formato no soportado: " + formato);
        }
    }

    private String generarCSV(List<Cliente> clientes) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido,Email,Telefono,Direccion,FechaRegistro,Categoria,Activo\n");

        clientes.forEach(cliente -> {
            csv.append(valorSeguro(cliente.getId())).append(",")
                    .append(valorSeguro(cliente.getRut())).append(",")
                    .append(valorSeguro(cliente.getNombre())).append(",")
                    .append(valorSeguro(cliente.getApellido())).append(",")
                    .append(valorSeguro(cliente.getEmail())).append(",")
                    .append(valorSeguro(cliente.getTelefono())).append(",")
                    .append(valorSeguro(cliente.getDireccion())).append(",");

            if (cliente.getFechaRegistro() != null) {
                csv.append(cliente.getFechaRegistro().toLocalDate().toString());
            }

            csv.append(",")
                    .append(valorSeguro(cliente.getCategoria())).append(",")
                    .append(esClienteActivo(cliente)).append("\n");
        });

        return csv.toString();
    }

    private String valorSeguro(Object valor) {
        return valor != null ? valor.toString() : "";
    }

    private int importarDesdeJSON(String datos) throws IOException {
        List<Cliente> clientes = objectMapper.readValue(datos, new TypeReference<List<Cliente>>() {});
        clienteRepositorio.saveAll(clientes);
        return clientes.size();
    }

    private int importarDesdeCSV(String datos) throws IOException {
        List<Cliente> clientes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new StringReader(datos))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Saltar encabezados
                }

                String[] values = line.split(",");
                if (values.length >= 8) {
                    Cliente cliente = crearClienteDesdeCSV(values);
                    clientes.add(cliente);
                }
            }
        }

        clienteRepositorio.saveAll(clientes);
        return clientes.size();
    }

    private Cliente crearClienteDesdeCSV(String[] values) {
        Cliente cliente = new Cliente();
        cliente.setRut(values[1]);
        cliente.setNombre(values[2]);
        cliente.setApellido(values[3]);
        cliente.setEmail(values[4]);
        cliente.setTelefono(values[5]);
        cliente.setDireccion(values[6]);

        if (!values[7].isEmpty()) {
            LocalDate fecha = LocalDate.parse(values[7]);
            cliente.setFechaRegistro(fecha.atStartOfDay());
        }

        if (values.length > 8) {
            cliente.setCategoria(values[8]);
        }

        return cliente;
    }

    private double calcularPorcentajeIntegridad(long total, long sinEmail, long sinRut, long sinNombre) {
        if (total == 0) return 100.0;

        long clientesConProblemas = Math.max(sinEmail, 0) + Math.max(sinRut, 0) + Math.max(sinNombre, 0);
        return Math.max(0.0, Math.min(100.0, ((double)(total - clientesConProblemas) / total) * 100));
    }
}