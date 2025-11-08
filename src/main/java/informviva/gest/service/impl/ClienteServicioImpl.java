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
 * - I: Implementa interfaz específica de clientes
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos trabajan con DTOs
 * - Los métodos privados manejan entidades JPA
 * - Usa ModelMapper para conversiones automáticas
 *
 * @author Sistema de Gestión Empresarial
 * @version 2.0 - Refactorizado Fase 1
 */
@Slf4j
@Service
@Transactional
public class ClienteServicioImpl 
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
        this.clienteRepositorio = clienteRepositorio;
        this.ventaRepositorio = ventaRepositorio;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarTodos() {
        log.debug("Buscando todos los clientes");

        return clienteRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando clientes paginados - Página: {}", pageable.getPageNumber());

        return clienteRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Long id) {
        log.debug("Buscando cliente por ID: {}", id);

        Cliente cliente = obtenerEntidadPorId(id);
        return convertirADTO(cliente);
    }

    @Override
    public ClienteDTO guardar(ClienteDTO clienteDTO) {
        log.debug("Guardando cliente: {} {}", clienteDTO.getNombre(), clienteDTO.getApellido());

        validarClienteDTO(clienteDTO);

        Cliente cliente = convertirAEntidad(clienteDTO);
        Cliente clienteGuardado = guardarEntidad(cliente);

        log.info("Cliente guardado exitosamente con ID: {}", clienteGuardado.getId());
        return convertirADTO(clienteGuardado);
    }

    @Override
    public ClienteDTO actualizar(Long id, ClienteDTO clienteDTO) {
        log.debug("Actualizando cliente ID: {} con datos: {} {}",
                id, clienteDTO.getNombre(), clienteDTO.getApellido());

        Cliente clienteExistente = obtenerEntidadPorId(id);
        validarClienteDTO(clienteDTO);

        // Mapear solo los campos actualizables
        actualizarCamposCliente(clienteExistente, clienteDTO);

        Cliente clienteActualizado = guardarEntidad(clienteExistente);

        log.info("Cliente actualizado exitosamente: {}", clienteActualizado.getId());
        return convertirADTO(clienteActualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.debug("Eliminando cliente ID: {}", id);

        if (!existe(id)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con ID: " + id);
        }

        clienteRepositorio.deleteById(id);
        log.info("Cliente eliminado exitosamente: {}", id);
    }

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando clientes por nombre: {}", nombre);

        return clienteRepositorio.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorApellido(String apellido) {
        log.debug("Buscando clientes por apellido: {}", apellido);

        return clienteRepositorio.findByApellidoContainingIgnoreCase(apellido)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorRut(String rut) {
        log.debug("Buscando cliente por RUT: {}", rut);

        if (!esRutValido(rut)) {
            log.warn("RUT inválido: {}", rut);
            return Optional.empty();
        }

        return clienteRepositorio.findByRut(rut)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorEmail(String email) {
        log.debug("Buscando cliente por email: {}", email);

        if (!esEmailValido(email)) {
            log.warn("Email inválido: {}", email);
            return Optional.empty();
        }

        return clienteRepositorio.findByEmail(email)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorTexto(String texto) {
        log.debug("Buscando clientes por texto: {}", texto);

        return clienteRepositorio.buscarPorTexto(texto)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTexto(String texto, Pageable pageable) {
        log.debug("Buscando clientes por texto con paginación: {}", texto);

        return clienteRepositorio.buscarPorTexto(texto, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarActivos() {
        log.debug("Buscando clientes activos");

        return clienteRepositorio.findByActivo(true)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarActivos(Pageable pageable) {
        log.debug("Buscando clientes activos con paginación");

        return clienteRepositorio.findByActivo(true, pageable)
                .map(this::convertirADTO);
    }

    // ============================================
    // OPERACIONES DE ACTIVACIÓN/DESACTIVACIÓN
    // ============================================

    @Override
    public void activar(Long id) {
        log.debug("Activando cliente ID: {}", id);

        Cliente cliente = obtenerEntidadPorId(id);
        cliente.setActivo(true);
        guardarEntidad(cliente);

        log.info("Cliente activado: {}", id);
    }

    @Override
    public void desactivar(Long id) {
        log.debug("Desactivando cliente ID: {}", id);

        Cliente cliente = obtenerEntidadPorId(id);
        cliente.setActivo(false);
        guardarEntidad(cliente);

        log.info("Cliente desactivado: {}", id);
    }

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return clienteRepositorio.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorRut(String rut) {
        log.debug("Verificando existencia de RUT: {}", rut);
        return esRutValido(rut) && clienteRepositorio.existsByRut(rut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        log.debug("Verificando existencia de email: {}", email);
        return esEmailValido(email) && clienteRepositorio.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esRutValido(String rut) {
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

    @Override
    @Transactional(readOnly = true)
    public boolean esEmailValido(String email) {
        return email != null && PATRON_EMAIL.matcher(email).matches();
    }

    // ============================================
    // CONTADORES Y ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return clienteRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return clienteRepositorio.countByActivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVentasPorCliente(Long clienteId) {
        log.debug("Contando ventas para cliente ID: {}", clienteId);
        return ventaRepositorio.countByClienteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarInactivos() {
        log.debug("Contando clientes inactivos");
        return clienteRepositorio.countByActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNuevosHoy() {
        log.debug("Contando clientes registrados hoy");
        return clienteRepositorio.contarClientesRegistradosHoy();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTodos() {
        return contar();
    }

    // ============================================
    // MÉTODOS ADICIONALES PARA CONTROLADORES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return existePorEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmailOtroCliente(String email, Long id) {
        log.debug("Verificando si existe email {} en otro cliente (excluyendo ID: {})", email, id);

        if (id == null) {
            return existePorEmail(email);
        }

        return clienteRepositorio.existeClienteConEmailExcluyendo(email, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorTermino(String termino) {
        log.debug("Buscando clientes por término: {}", termino);

        return clienteRepositorio.buscarPorTermino(termino)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorTermino(String termino, int limite) {
        log.debug("Buscando clientes por término: {} (límite: {})", termino, limite);

        return clienteRepositorio.buscarPorTermino(termino)
                .stream()
                .limit(limite)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTermino(String search, Pageable pageable) {
        log.debug("Buscando clientes por término con paginación: {}", search);

        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRutContaining(
                search, search, search, search, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTerminoYCiudad(String search, String ciudad, Pageable pageable) {
        log.debug("Buscando clientes por término: {} y ciudad: {} (paginado)", search, ciudad);

        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCase(
                search, search, search, ciudad, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTerminoYActivos(String search, Pageable pageable) {
        log.debug("Buscando clientes activos por término: {} (paginado)", search);

        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActivoTrue(
                search, search, search, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTerminoYCiudadYActivos(String search, String ciudad, Pageable pageable) {
        log.debug("Buscando clientes activos por término: {} y ciudad: {} (paginado)", search, ciudad);

        return clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCiudadIgnoreCaseAndActivoTrue(
                search, search, search, ciudad, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorCiudad(String ciudad, Pageable pageable) {
        log.debug("Buscando clientes por ciudad: {} (paginado)", ciudad);

        return clienteRepositorio.findByCiudadIgnoreCase(ciudad, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorCiudadYActivos(String ciudad, Pageable pageable) {
        log.debug("Buscando clientes activos por ciudad: {} (paginado)", ciudad);

        return clienteRepositorio.findByCiudadIgnoreCaseAndActivoTrue(ciudad, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorNombreOEmail(String termino, Pageable pageable) {
        log.debug("Buscando clientes por nombre o email: {} (legacy)", termino);

        return clienteRepositorio.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
                termino, termino, pageable)
                .map(this::convertirADTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    /**
     * Obtiene una entidad Cliente por ID (uso interno)
     *
     * @param id ID del cliente
     * @return Entidad Cliente
     * @throws RecursoNoEncontradoException si no existe
     */
    private Cliente obtenerEntidadPorId(Long id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con ID: " + id));
    }

    /**
     * Guarda una entidad Cliente (uso interno)
     *
     * @param cliente Entidad a guardar
     * @return Entidad guardada
     */
    private Cliente guardarEntidad(Cliente cliente) {
        return clienteRepositorio.save(cliente);
    }

    /**
     * Convierte entidad Cliente a ClienteDTO
     *
     * @param cliente Entidad
     * @return DTO
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        return modelMapper.map(cliente, ClienteDTO.class);
    }

    /**
     * Convierte ClienteDTO a entidad Cliente
     *
     * @param dto DTO
     * @return Entidad
     */
    private Cliente convertirAEntidad(ClienteDTO dto) {
        return modelMapper.map(dto, Cliente.class);
    }

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

        if (!esRutValido(clienteDTO.getRut())) {
            throw new IllegalArgumentException("El RUT del cliente no es válido");
        }

        if (!esEmailValido(clienteDTO.getEmail())) {
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