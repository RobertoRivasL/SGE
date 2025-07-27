package informviva.gest.service.impl;

import informviva.gest.dto.ClienteDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Cliente;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de clientes siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de clientes
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de ClienteServicio
 * - I: Implementa interface específica de clientes
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * @author Tu nombre
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ClienteServicioImpl extends BaseServiceImpl<Cliente, Long>
        implements ClienteServicio {

    // Patrón para validar RUT chileno
    private static final Pattern PATRON_RUT = Pattern.compile("^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9kK]{1}$");

    // Patrón para validar email
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final ClienteRepositorio clienteRepositorio;
    private final VentaRepositorio ventaRepositorio;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     *
     * @param clienteRepositorio Repositorio de clientes
     * @param ventaRepositorio Repositorio de ventas
     * @param modelMapper Mapper para conversión DTO/Entidad
     */
    public ClienteServicioImpl(ClienteRepositorio clienteRepositorio,
                               VentaRepositorio ventaRepositorio,
                               ModelMapper modelMapper) {
        super(clienteRepositorio);
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.modelMapper = modelMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarTodosDTO() {
        log.debug("Buscando todos los clientes como DTO");

        return clienteRepositorio.findAll()
                .stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarTodosDTO(Pageable pageable) {
        log.debug("Buscando clientes paginados como DTO - Página: {}", pageable.getPageNumber());

        return clienteRepositorio.findAll(pageable)
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorIdDTO(Long id) {
        log.debug("Buscando cliente por ID como DTO: {}", id);

        Cliente cliente = obtenerPorId(id);
        return modelMapper.map(cliente, ClienteDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClienteDTO guardarDTO(ClienteDTO clienteDTO) {
        log.debug("Guardando cliente DTO: {} {}", clienteDTO.getNombre(), clienteDTO.getApellido());

        validarClienteDTO(clienteDTO);

        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        Cliente clienteGuardado = guardar(cliente);

        log.info("Cliente guardado exitosamente con ID: {}", clienteGuardado.getId());
        return modelMapper.map(clienteGuardado, ClienteDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClienteDTO actualizarDTO(Long id, ClienteDTO clienteDTO) {
        log.debug("Actualizando cliente ID: {} con datos: {} {}",
                id, clienteDTO.getNombre(), clienteDTO.getApellido());

        Cliente clienteExistente = obtenerPorId(id);
        validarClienteDTO(clienteDTO);

        // Mapear solo los campos actualizables
        actualizarCamposCliente(clienteExistente, clienteDTO);

        Cliente clienteActualizado = guardar(clienteExistente);

        log.info("Cliente actualizado exitosamente: {}", clienteActualizado.getId());
        return modelMapper.map(clienteActualizado, ClienteDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando clientes por nombre: {}", nombre);

        return clienteRepositorio.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorApellido(String apellido) {
        log.debug("Buscando clientes por apellido: {}", apellido);

        return clienteRepositorio.findByApellidoContainingIgnoreCase(apellido)
                .stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorRut(String rut) {
        log.debug("Buscando cliente por RUT: {}", rut);

        if (!rutEsValido(rut)) {
            log.warn("RUT inválido: {}", rut);
            return Optional.empty();
        }

        return clienteRepositorio.findByRut(rut)
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorEmail(String email) {
        log.debug("Buscando cliente por email: {}", email);

        if (!emailEsValido(email)) {
            log.warn("Email inválido: {}", email);
            return Optional.empty();
        }

        return clienteRepositorio.findByEmail(email)
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorTexto(String texto) {
        log.debug("Buscando clientes por texto: {}", texto);

        return clienteRepositorio.buscarPorTexto(texto)
                .stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existePorRut(String rut) {
        log.debug("Verificando existencia de RUT: {}", rut);
        return rutEsValido(rut) && clienteRepositorio.existsByRut(rut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        log.debug("Verificando existencia de email: {}", email);
        return emailEsValido(email) && clienteRepositorio.existsByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean rutEsValido(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        // Validar formato básico
        if (!PATRON_RUT.matcher(rut).matches()) {
            return false;
        }

        // Validar dígito verificador
        return validarDigitoVerificador(rut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean emailEsValido(String email) {
        return email != null && PATRON_EMAIL.matcher(email).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long contarClientesActivos() {
        return clienteRepositorio.countByActivoTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarClientesActivos() {
        log.debug("Buscando clientes activos");

        return clienteRepositorio.findByActivoTrue()
                .stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activarCliente(Long id) {
        log.debug("Activando cliente ID: {}", id);

        Cliente cliente = obtenerPorId(id);
        cliente.setActivo(true);
        guardar(cliente);

        log.info("Cliente activado: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void desactivarCliente(Long id) {
        log.debug("Desactivando cliente ID: {}", id);

        Cliente cliente = obtenerPorId(id);
        cliente.setActivo(false);
        guardar(cliente);

        log.info("Cliente desactivado: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long contarVentasPorCliente(Long clienteId) {
        log.debug("Contando ventas para cliente ID: {}", clienteId);
        return ventaRepositorio.countByClienteId(clienteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNombreEntidad() {
        return "Cliente";
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida los datos del ClienteDTO
     *
     * @param clienteDTO DTO a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarClienteDTO(ClienteDTO clienteDTO) {
        if (clienteDTO.getNombre() == null || clienteDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        if (clienteDTO.getApellido() == null || clienteDTO.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del cliente es obligatorio");
        }

        if (!rutEsValido(clienteDTO.getRut())) {
            throw new IllegalArgumentException("El RUT del cliente no es válido");
        }

        if (!emailEsValido(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("El email del cliente no es válido");
        }

        // Validar unicidad (solo para nuevos clientes)
        if (clienteDTO.getId() == null) {
            if (existePorRut(clienteDTO.getRut())) {
                throw new IllegalArgumentException("Ya existe un cliente con este RUT");
            }

            if (existePorEmail(clienteDTO.getEmail())) {
                throw new IllegalArgumentException("Ya existe un cliente con este email");
            }
        }
    }

    /**
     * Actualiza los campos modificables de un cliente existente
     *
     * @param clienteExistente Cliente a actualizar
     * @param clienteDTO Datos nuevos
     */
    private void actualizarCamposCliente(Cliente clienteExistente, ClienteDTO clienteDTO) {
        clienteExistente.setNombre(clienteDTO.getNombre());
        clienteExistente.setApellido(clienteDTO.getApellido());
        clienteExistente.setEmail(clienteDTO.getEmail());
        clienteExistente.setTelefono(clienteDTO.getTelefono());
        clienteExistente.setDireccion(clienteDTO.getDireccion());
        clienteExistente.setActivo(clienteDTO.getActivo());
        clienteExistente.setFechaActualizacion(LocalDateTime.now());
    }

    /**
     * Valida el dígito verificador del RUT chileno
     *
     * @param rut RUT en formato xx.xxx.xxx-x
     * @return true si el dígito verificador es válido
     */
    private boolean validarDigitoVerificador(String rut) {
        try {
            // Remover puntos y guión
            String rutLimpio = rut.replace(".", "").replace("-", "");
            String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
            String dv = rutLimpio.substring(rutLimpio.length() - 1).toLowerCase();

            // Calcular dígito verificador
            int suma = 0;
            int multiplicador = 2;

            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
                multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
            }

            int resto = 11 - (suma % 11);
            String dvCalculado = resto == 11 ? "0" : resto == 10 ? "k" : String.valueOf(resto);

            return dvCalculado.equals(dv);

        } catch (Exception e) {
            log.warn("Error validando RUT: {}", rut, e);
            return false;
        }
    }
}