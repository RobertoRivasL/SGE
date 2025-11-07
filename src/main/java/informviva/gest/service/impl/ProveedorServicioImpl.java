package informviva.gest.service.impl;

import informviva.gest.dto.EstadisticasComprasDTO;
import informviva.gest.dto.ProveedorDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Proveedor;
import informviva.gest.repository.ProveedorRepositorio;
import informviva.gest.service.ProveedorServicio;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de proveedores siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de proveedores
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de ProveedorServicio
 * - I: Implementa interfaz específica de proveedores
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos trabajan con DTOs
 * - Los métodos privados manejan entidades JPA
 * - Usa ModelMapper para conversiones automáticas
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Slf4j
@Service
@Transactional
public class ProveedorServicioImpl extends BaseServiceImpl<Proveedor, Long>
        implements ProveedorServicio {

    private final ProveedorRepositorio proveedorRepositorio;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     */
    public ProveedorServicioImpl(ProveedorRepositorio proveedorRepositorio,
                                 ModelMapper modelMapper) {
        super(proveedorRepositorio);
        this.proveedorRepositorio = proveedorRepositorio;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    @Override
    public ProveedorDTO guardar(ProveedorDTO proveedorDTO) {
        log.debug("Guardando nuevo proveedor: {}", proveedorDTO.getNombre());

        validarProveedorDTO(proveedorDTO);
        validarRutUnico(proveedorDTO.getRut(), null);

        Proveedor proveedor = convertirAEntidad(proveedorDTO);
        proveedor.setFechaCreacion(LocalDateTime.now());
        proveedor.setActivo(true);

        Proveedor proveedorGuardado = proveedorRepositorio.save(proveedor);

        log.info("Proveedor guardado exitosamente con ID: {}", proveedorGuardado.getId());
        return convertirADTO(proveedorGuardado);
    }

    @Override
    public ProveedorDTO actualizar(Long id, ProveedorDTO proveedorDTO) {
        log.debug("Actualizando proveedor ID: {}", id);

        Proveedor proveedorExistente = obtenerEntidadPorId(id);
        validarProveedorDTO(proveedorDTO);
        validarRutUnico(proveedorDTO.getRut(), id);

        actualizarCamposProveedor(proveedorExistente, proveedorDTO);
        proveedorExistente.setFechaActualizacion(LocalDateTime.now());

        Proveedor proveedorActualizado = proveedorRepositorio.save(proveedorExistente);

        log.info("Proveedor actualizado exitosamente: {}", proveedorActualizado.getId());
        return convertirADTO(proveedorActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorDTO buscarPorId(Long id) {
        log.debug("Buscando proveedor por ID: {}", id);

        Proveedor proveedor = obtenerEntidadPorId(id);
        return convertirADTO(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando proveedores paginados - Página: {}", pageable.getPageNumber());

        return proveedorRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> buscarTodos() {
        log.debug("Buscando todos los proveedores");

        return proveedorRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> buscarActivos() {
        log.debug("Buscando proveedores activos");

        return proveedorRepositorio.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarActivos(Pageable pageable) {
        log.debug("Buscando proveedores activos paginados");

        return proveedorRepositorio.findByActivoTrue(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarInactivos(Pageable pageable) {
        log.debug("Buscando proveedores inactivos paginados");

        return proveedorRepositorio.findByActivoFalse(pageable)
                .map(this::convertirADTO);
    }

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarPorTermino(String termino, Pageable pageable) {
        log.debug("Buscando proveedores por término: {}", termino);

        if (termino == null || termino.trim().isEmpty()) {
            return buscarTodos(pageable);
        }

        return proveedorRepositorio.buscarPorTermino(termino.trim(), pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarPorNombre(String nombre, Pageable pageable) {
        log.debug("Buscando proveedores por nombre: {}", nombre);

        return proveedorRepositorio.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorDTO buscarPorRut(String rut) {
        log.debug("Buscando proveedor por RUT: {}", rut);

        Proveedor proveedor = proveedorRepositorio.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor no encontrado con RUT: " + rut));

        return convertirADTO(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarPorCategoria(String categoria, Pageable pageable) {
        log.debug("Buscando proveedores por categoría: {}", categoria);

        return proveedorRepositorio.findByCategoria(categoria, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarPorCiudad(String ciudad, Pageable pageable) {
        log.debug("Buscando proveedores por ciudad: {}", ciudad);

        return proveedorRepositorio.findByCiudadIgnoreCase(ciudad, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarPorCalificacionMinima(Integer calificacion, Pageable pageable) {
        log.debug("Buscando proveedores con calificación mínima: {}", calificacion);

        return proveedorRepositorio.findByCalificacionGreaterThanEqual(calificacion, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorDTO> buscarConCriterios(String nombre, String categoria, String ciudad,
                                                 boolean soloActivos, Pageable pageable) {
        log.debug("Buscando proveedores con criterios - Nombre: {}, Categoría: {}, Ciudad: {}, Solo activos: {}",
                nombre, categoria, ciudad, soloActivos);

        return proveedorRepositorio.buscarConCriterios(nombre, categoria, ciudad, soloActivos, pageable)
                .map(this::convertirADTO);
    }

    // ============================================
    // OPERACIONES DE ESTADO
    // ============================================

    @Override
    public ProveedorDTO activar(Long id) {
        log.debug("Activando proveedor ID: {}", id);

        Proveedor proveedor = obtenerEntidadPorId(id);
        proveedor.setActivo(true);
        proveedor.setFechaActualizacion(LocalDateTime.now());

        Proveedor proveedorActualizado = proveedorRepositorio.save(proveedor);

        log.info("Proveedor activado exitosamente: {}", id);
        return convertirADTO(proveedorActualizado);
    }

    @Override
    public ProveedorDTO desactivar(Long id) {
        log.debug("Desactivando proveedor ID: {}", id);

        Proveedor proveedor = obtenerEntidadPorId(id);
        proveedor.setActivo(false);
        proveedor.setFechaActualizacion(LocalDateTime.now());

        Proveedor proveedorActualizado = proveedorRepositorio.save(proveedor);

        log.info("Proveedor desactivado exitosamente: {}", id);
        return convertirADTO(proveedorActualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.debug("Eliminando proveedor ID: {}", id);

        if (!existe(id)) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + id);
        }

        if (tieneOrdenesCompra(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el proveedor porque tiene órdenes de compra asociadas. " +
                            "Desactive el proveedor en su lugar.");
        }

        proveedorRepositorio.deleteById(id);
        log.info("Proveedor eliminado exitosamente: {}", id);
    }

    // ============================================
    // VALIDACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existeRut(String rut) {
        return proveedorRepositorio.existsByRut(rut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRutExcluyendo(String rut, Long id) {
        return proveedorRepositorio.existsByRutAndIdNot(rut, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return proveedorRepositorio.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneOrdenesCompra(Long id) {
        return proveedorRepositorio.tieneOrdenesCompra(id);
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public EstadisticasComprasDTO.ProveedorEstadisticaDTO obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de proveedores");

        EstadisticasComprasDTO.ProveedorEstadisticaDTO estadisticas =
                new EstadisticasComprasDTO.ProveedorEstadisticaDTO();

        // Aquí se pueden agregar más estadísticas según sea necesario

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerCategorias() {
        log.debug("Obteniendo categorías de proveedores");
        return proveedorRepositorio.findDistinctCategorias();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTodos() {
        return proveedorRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return proveedorRepositorio.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarInactivos() {
        return proveedorRepositorio.countByActivoFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorCategoria(String categoria) {
        return proveedorRepositorio.countByCategoria(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> obtenerTopProveedoresPorOrdenes(int limite) {
        log.debug("Obteniendo top {} proveedores por número de órdenes", limite);

        List<Object[]> resultados = proveedorRepositorio.findTopProveedoresPorOrdenes(
                PageRequest.of(0, limite));

        return resultados.stream()
                .map(resultado -> convertirADTO((Proveedor) resultado[0]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> obtenerTopProveedoresPorMonto(int limite) {
        log.debug("Obteniendo top {} proveedores por monto total", limite);

        List<Object[]> resultados = proveedorRepositorio.findTopProveedoresPorMonto(
                PageRequest.of(0, limite));

        return resultados.stream()
                .map(resultado -> convertirADTO((Proveedor) resultado[0]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDTO> obtenerProveedoresSinOrdenes() {
        log.debug("Obteniendo proveedores sin órdenes");

        return proveedorRepositorio.findProveedoresSinOrdenes()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProveedoresConCredito() {
        return proveedorRepositorio.countProveedoresConCredito();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarOrdenesProveedor(Long id) {
        return proveedorRepositorio.contarOrdenesProveedor(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMontoTotalProveedor(Long id) {
        return proveedorRepositorio.calcularMontoTotalProveedor(id);
    }

    // ============================================
    // IMPLEMENTACIÓN DE BaseServiceImpl
    // ============================================

    @Override
    protected String getNombreEntidad() {
        return "Proveedor";
    }

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    /**
     * Obtiene una entidad Proveedor por ID (uso interno)
     *
     * @param id ID del proveedor
     * @return Entidad Proveedor
     * @throws RecursoNoEncontradoException si no existe
     */
    private Proveedor obtenerEntidadPorId(Long id) {
        return proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor no encontrado con ID: " + id));
    }

    /**
     * Convierte entidad Proveedor a ProveedorDTO
     *
     * @param proveedor Entidad
     * @return DTO
     */
    private ProveedorDTO convertirADTO(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }

        ProveedorDTO dto = modelMapper.map(proveedor, ProveedorDTO.class);

        // Mapear campos adicionales si es necesario
        if (proveedor.getUsuarioCreacion() != null) {
            dto.setUsuarioCreacionId(proveedor.getUsuarioCreacion().getId());
            dto.setUsuarioCreacionNombre(proveedor.getUsuarioCreacion().getNombreCompleto());
        }

        if (proveedor.getUsuarioActualizacion() != null) {
            dto.setUsuarioActualizacionId(proveedor.getUsuarioActualizacion().getId());
            dto.setUsuarioActualizacionNombre(proveedor.getUsuarioActualizacion().getNombreCompleto());
        }

        return dto;
    }

    /**
     * Convierte ProveedorDTO a entidad Proveedor
     *
     * @param dto DTO
     * @return Entidad
     */
    private Proveedor convertirAEntidad(ProveedorDTO dto) {
        if (dto == null) {
            return null;
        }

        return modelMapper.map(dto, Proveedor.class);
    }

    /**
     * Valida los datos del ProveedorDTO
     *
     * @param dto DTO a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validarProveedorDTO(ProveedorDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del proveedor son obligatorios");
        }

        if (dto.getRut() == null || dto.getRut().trim().isEmpty()) {
            throw new IllegalArgumentException("El RUT del proveedor es obligatorio");
        }

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El email del proveedor no es válido");
            }
        }

        if (dto.getContactoEmail() != null && !dto.getContactoEmail().trim().isEmpty()) {
            if (!dto.getContactoEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El email del contacto no es válido");
            }
        }

        if (dto.getDiasCredito() != null && dto.getDiasCredito() < 0) {
            throw new IllegalArgumentException("Los días de crédito no pueden ser negativos");
        }

        if (dto.getCalificacion() != null) {
            if (dto.getCalificacion() < 1 || dto.getCalificacion() > 5) {
                throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
            }
        }
    }

    /**
     * Valida que el RUT sea único
     *
     * @param rut RUT a validar
     * @param idExcluir ID a excluir de la validación (para actualizaciones)
     * @throws IllegalArgumentException si el RUT ya existe
     */
    private void validarRutUnico(String rut, Long idExcluir) {
        boolean rutExiste;

        if (idExcluir != null) {
            rutExiste = proveedorRepositorio.existsByRutAndIdNot(rut, idExcluir);
        } else {
            rutExiste = proveedorRepositorio.existsByRut(rut);
        }

        if (rutExiste) {
            throw new IllegalArgumentException("Ya existe un proveedor con el RUT: " + rut);
        }
    }

    /**
     * Actualiza los campos de un proveedor existente
     *
     * @param proveedor Proveedor a actualizar
     * @param dto DTO con los nuevos datos
     */
    private void actualizarCamposProveedor(Proveedor proveedor, ProveedorDTO dto) {
        proveedor.setRut(dto.getRut());
        proveedor.setNombre(dto.getNombre());
        proveedor.setNombreFantasia(dto.getNombreFantasia());
        proveedor.setGiro(dto.getGiro());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setCiudad(dto.getCiudad());
        proveedor.setRegion(dto.getRegion());
        proveedor.setPais(dto.getPais());
        proveedor.setCodigoPostal(dto.getCodigoPostal());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setTelefonoAlternativo(dto.getTelefonoAlternativo());
        proveedor.setEmail(dto.getEmail());
        proveedor.setSitioWeb(dto.getSitioWeb());
        proveedor.setContactoNombre(dto.getContactoNombre());
        proveedor.setContactoCargo(dto.getContactoCargo());
        proveedor.setContactoTelefono(dto.getContactoTelefono());
        proveedor.setContactoEmail(dto.getContactoEmail());
        proveedor.setCondicionesPago(dto.getCondicionesPago());
        proveedor.setDiasCredito(dto.getDiasCredito());
        proveedor.setObservaciones(dto.getObservaciones());
        proveedor.setCalificacion(dto.getCalificacion());
        proveedor.setCategoria(dto.getCategoria());

        if (dto.getActivo() != null) {
            proveedor.setActivo(dto.getActivo());
        }
    }
}
