package informviva.gest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.EmailServicio;
import informviva.gest.validador.ValidadorRutUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServicioImpl implements ClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServicioImpl.class);

    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;
    private final ObjectMapper objectMapper;
    private final EmailServicio emailServicio;



    public ClienteServicioImpl(ClienteRepositorio clienteRepositorio, VentaRepositorio ventaRepositorio, EmailServicio emailServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.emailServicio = emailServicio;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmailOtroCliente(String email, Long clienteId) {
        return clienteRepositorio.findByEmail(email)
                .map(cliente -> !cliente.getId().equals(clienteId))
                .orElse(false);
    }



    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        logger.debug("Buscando cliente por ID: {}", id);
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTermino(String termino) {
        logger.debug("Buscando clientes por término: {}", termino);
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(termino, termino);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConEmailExcluyendo(String email, Long excludeId) {
        return clienteRepositorio.findByEmail(email)
                .map(cliente -> !cliente.getId().equals(excludeId))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRutExcluyendo(String rut, Long excludeId) {
        return clienteRepositorio.findByRut(rut)
                .map(cliente -> !cliente.getId().equals(excludeId))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        logger.debug("Obteniendo todos los clientes");
        return clienteRepositorio.findAll();
    }

    @Override
    public String exportarDatos(String formato, LocalDate fechaInicio, LocalDate fechaFin) {
        logger.info("Exportando datos de clientes en formato: {}", formato);
        List<Cliente> clientes = clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);

        try {
            switch (formato.toUpperCase()) {
                case "JSON":
                    return new String(objectMapper.writeValueAsBytes(clientes));
                case "CSV":
                    return new String(generarCSV(clientes));
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error exportando datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en exportación", e);
        }
    }

    private byte[] generarCSV(List<Cliente> clientes) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,RUT,Nombre,Apellido,Email,Telefono,Direccion,FechaRegistro,Categoria\n");

        for (Cliente cliente : clientes) {
            csv.append(cliente.getId()).append(",")
                    .append(valorSeguro(cliente.getRut())).append(",")
                    .append(valorSeguro(cliente.getNombre())).append(",")
                    .append(valorSeguro(cliente.getApellido())).append(",")
                    .append(valorSeguro(cliente.getEmail())).append(",")
                    .append(valorSeguro(cliente.getTelefono())).append(",")
                    .append(valorSeguro(cliente.getDireccion())).append(",")
                    .append(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : "").append(",")
                    .append(valorSeguro(cliente.getCategoria())).append("\n");
        }

        return csv.toString().getBytes();
    }

    private String valorSeguro(Object valor) {
        return valor != null ? valor.toString() : "";
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return clienteRepositorio.findByEmail(email).isPresent();
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return clienteRepositorio.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        if (!clienteRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con ID: " + id);
        }
        clienteRepositorio.deleteById(id);
    }

    @Override
    public boolean rutEsValido(String rut) {
        return ValidadorRutUtil.validar(rut);
    }

    @Override
    public List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Cliente> clientes = clienteRepositorio.findClientesConComprasEntreFechas(fechaInicio, fechaFin);
        return clientes.stream()
                .map(this::convertirAClienteReporteDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    public Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.countByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    private ClienteReporteDTO convertirAClienteReporteDTO(Cliente cliente) {
        // Obtener las compras del cliente
        List<Venta> compras = ventaRepositorio.findByCliente(cliente);

        int comprasRealizadas = compras.size();
        BigDecimal totalCompras = compras.stream()
                .map(venta -> new BigDecimal(venta.getTotal().toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedioPorCompra = comprasRealizadas > 0
                ? totalCompras.divide(new BigDecimal(comprasRealizadas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        LocalDate ultimaCompra = compras.stream()
                .map(venta -> venta.getFecha().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);

        return new ClienteReporteDTO(
                cliente.getId(),
                cliente.getRut(),
                cliente.getNombre() + " " + cliente.getApellido(), // nombreCompleto
                cliente.getEmail(),
                cliente.getFechaRegistro().toLocalDate(), // Asumiendo que getFechaRegistro() devuelve LocalDateTime
                comprasRealizadas,
                totalCompras,
                promedioPorCompra,
                ultimaCompra
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorRut(String rut) {
        logger.debug("Buscando cliente por RUT: {}", rut);
        return clienteRepositorio.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con RUT: " + rut));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesPorCategoria(String categoria) {
        logger.debug("Obteniendo clientes por categoría: {}", categoria);
        return clienteRepositorio.findByCategoria(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite) {
        logger.debug("Obteniendo top {} clientes por compras", limite);
        Pageable pageable = PageRequest.of(0, limite);
        List<Cliente> topClientes = clienteRepositorio.findTopClientesPorCompras(pageable);
        return topClientes.stream()
                .map(this::convertirAClienteReporteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioComprasPorCliente() {
        logger.debug("Calculando promedio de compras por cliente");
        List<Cliente> clientes = clienteRepositorio.findAll();
        if (clientes.isEmpty()) {
            return 0.0;
        }

        double totalCompras = clientes.stream()
                .mapToDouble(cliente -> ventaRepositorio.findByCliente(cliente).stream()
                        .mapToDouble(Venta::getTotal)
                        .sum())
                .sum();

        return totalCompras / clientes.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarClientesActivos() {
        logger.debug("Contando clientes activos");
        return clienteRepositorio.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepositorio.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepositorio.findByEmail(email);
    }

    @Override
    public boolean existeClienteConEmail(String email) {
        return clienteRepositorio.findByEmail(email).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConRut(String rut) {
        return clienteRepositorio.findByRut(rut).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> obtenerTodosPaginados(Pageable pageable) {
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNombreOEmail(String termino, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(termino, termino, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Override
    public int importarDatos(String datos, String formato) {
        logger.info("Importando datos de clientes en formato: {}", formato);
        int registrosImportados = 0;

        try {
            switch (formato.toUpperCase()) {
                case "JSON":
                    registrosImportados = importarDesdeJSON(datos);
                    break;
                case "CSV":
                    registrosImportados = importarDesdeCSV(datos);
                    break;
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            logger.error("Error importando datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en importación", e);
        }

        return registrosImportados;
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
                    continue; // Saltar la primera línea (encabezados)
                }
                String[] values = line.split(",");
                Cliente cliente = new Cliente();
                cliente.setRut(values[1]);
                cliente.setNombre(values[2]);
                cliente.setApellido(values[3]);
                cliente.setEmail(values[4]);
                cliente.setTelefono(values[5]);
                cliente.setDireccion(values[6]);
                cliente.setFechaRegistro(LocalDateTime.parse(values[7]));
                cliente.setCategoria(values[8]);
                clientes.add(cliente);
            }
        }
        clienteRepositorio.saveAll(clientes);
        return clientes.size();
    }

    @Override
    public Map<String, Object> validarIntegridadDatos() {
        logger.info("Validando integridad de datos de clientes");
        Map<String, Object> resultado = new HashMap<>();

        long totalClientes = clienteRepositorio.count();
        long clientesSinEmail = clienteRepositorio.countByEmailIsNullOrEmailEquals("");
        long clientesSinRut = clienteRepositorio.countByRutIsNullOrRutEquals("");
        long clientesSinNombre = clienteRepositorio.countByNombreIsNullOrNombreEquals("");

        resultado.put("totalClientes", totalClientes);
        resultado.put("clientesSinEmail", clientesSinEmail);
        resultado.put("clientesSinRut", clientesSinRut);
        resultado.put("clientesSinNombre", clientesSinNombre);
        resultado.put("porcentajeIntegridad", calcularPorcentajeIntegridad(totalClientes, clientesSinEmail, clientesSinRut, clientesSinNombre));

        return resultado;
    }

    private double calcularPorcentajeIntegridad(long total, long sinEmail, long sinRut, long sinNombre) {
        if (total == 0) return 100.0; // Si no hay clientes, consideramos 100% de integridad
        long clientesConProblemas = sinEmail + sinRut + sinNombre;
        return ((double)(total - clientesConProblemas) / total) * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();
        long clientesNuevos = clienteRepositorio.countByFechaRegistroBetween(fechaInicio, fechaFin);
        long totalVentas = ventaRepositorio.countByFechaBetween(fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        estadisticas.put("clientesNuevos", clientesNuevos);
        estadisticas.put("totalVentas", totalVentas);
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
        return clienteRepositorio.findClientesSinVentas(fechaInicio, fechaFin);
    }

    @Override
    public int actualizarCategoriaEnLote(List<Long> ids, String nuevaCategoria) {
        List<Cliente> clientes = clienteRepositorio.findAllById(ids);
        for (Cliente cliente : clientes) {
            cliente.setCategoria(nuevaCategoria);
        }
        clienteRepositorio.saveAll(clientes);
        return clientes.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodasClientes() {
        return clienteRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientesPaginadas(Pageable pageable) {
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesRegistradosRecientemente(LocalDate fecha) {
        return clienteRepositorio.findByFechaRegistroAfter(fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Venta> listarVentasPaginadas(Pageable pageable) {
        return ventaRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarTodasVentas() {
        return ventaRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorDireccion(String direccion) {
        return clienteRepositorio.findByDireccionContainingIgnoreCase(direccion);
    }

    @Override
    public Optional<Cliente> buscarPorTelefono(String telefono) {
        return clienteRepositorio.findByTelefono(telefono);
    }

}