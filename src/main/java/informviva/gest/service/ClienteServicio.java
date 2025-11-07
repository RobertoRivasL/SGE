package informviva.gest.service;

import informviva.gest.dto.ClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de clientes que define el contrato
 * para la gestión de clientes en el sistema.
 *
 * IMPORTANTE: Todos los métodos públicos trabajan exclusivamente con DTOs.
 * Las entidades JPA son manejadas internamente por la implementación.
 *
 * Aplicación del principio de Inversión de Dependencias (D de SOLID):
 * - Los módulos de alto nivel no dependen de módulos de bajo nivel
 * - Ambos dependen de abstracciones (esta interfaz)
 *
 * @author Sistema de Gestión Empresarial
 * @version 2.0 - Refactorizado Fase 1
 */
public interface ClienteServicio {

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    /**
     * Obtiene todos los clientes
     *
     * @return Lista de ClienteDTO
     */
    List<ClienteDTO> buscarTodos();

    /**
     * Obtiene todos los clientes con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ClienteDTO
     */
    Page<ClienteDTO> buscarTodos(Pageable pageable);

    /**
     * Busca un cliente por su ID
     *
     * @param id Identificador del cliente
     * @return ClienteDTO encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ClienteDTO buscarPorId(Long id);

    /**
     * Guarda un nuevo cliente o actualiza uno existente
     *
     * @param clienteDTO Datos del cliente a guardar
     * @return ClienteDTO guardado con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ClienteDTO guardar(ClienteDTO clienteDTO);

    /**
     * Actualiza un cliente existente
     *
     * @param id Identificador del cliente a actualizar
     * @param clienteDTO Nuevos datos del cliente
     * @return ClienteDTO actualizado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ClienteDTO actualizar(Long id, ClienteDTO clienteDTO);

    /**
     * Elimina un cliente por su ID
     *
     * @param id Identificador del cliente a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el cliente no existe
     */
    void eliminar(Long id);

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    /**
     * Busca clientes por nombre (búsqueda parcial, insensible a mayúsculas)
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de ClienteDTO que coinciden
     */
    List<ClienteDTO> buscarPorNombre(String nombre);

    /**
     * Busca clientes por apellido (búsqueda parcial, insensible a mayúsculas)
     *
     * @param apellido Apellido o parte del apellido a buscar
     * @return Lista de ClienteDTO que coinciden
     */
    List<ClienteDTO> buscarPorApellido(String apellido);

    /**
     * Busca un cliente por su RUT
     *
     * @param rut RUT del cliente (formato: xx.xxx.xxx-x)
     * @return Optional con ClienteDTO si existe
     */
    Optional<ClienteDTO> buscarPorRut(String rut);

    /**
     * Busca un cliente por su email
     *
     * @param email Email del cliente
     * @return Optional con ClienteDTO si existe
     */
    Optional<ClienteDTO> buscarPorEmail(String email);

    /**
     * Busca clientes por texto libre (nombre, apellido, email, RUT)
     *
     * @param texto Texto a buscar
     * @return Lista de ClienteDTO que coinciden
     */
    List<ClienteDTO> buscarPorTexto(String texto);

    /**
     * Busca clientes por texto con paginación
     *
     * @param texto Texto a buscar
     * @param pageable Información de paginación
     * @return Página de ClienteDTO que coinciden
     */
    Page<ClienteDTO> buscarPorTexto(String texto, Pageable pageable);

    /**
     * Busca todos los clientes activos
     *
     * @return Lista de ClienteDTO activos
     */
    List<ClienteDTO> buscarActivos();

    /**
     * Busca clientes activos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ClienteDTO activos
     */
    Page<ClienteDTO> buscarActivos(Pageable pageable);

    // ============================================
    // OPERACIONES DE ACTIVACIÓN/DESACTIVACIÓN
    // ============================================

    /**
     * Activa un cliente (marca como activo)
     *
     * @param id Identificador del cliente
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void activar(Long id);

    /**
     * Desactiva un cliente (marca como inactivo, no se elimina)
     *
     * @param id Identificador del cliente
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void desactivar(Long id);

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    /**
     * Verifica si existe un cliente con el ID dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existe(Long id);

    /**
     * Verifica si existe un cliente con el RUT dado
     *
     * @param rut RUT a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existePorRut(String rut);

    /**
     * Verifica si existe un cliente con el email dado
     *
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existePorEmail(String email);

    /**
     * Valida si un RUT tiene el formato y dígito verificador correctos
     *
     * @param rut RUT a validar (formato: xx.xxx.xxx-x)
     * @return true si es válido, false en caso contrario
     */
    boolean esRutValido(String rut);

    /**
     * Valida si un email tiene el formato correcto
     *
     * @param email Email a validar
     * @return true si es válido, false en caso contrario
     */
    boolean esEmailValido(String email);

    // ============================================
    // CONTADORES Y ESTADÍSTICAS
    // ============================================

    /**
     * Cuenta el número total de clientes
     *
     * @return Número total de clientes
     */
    long contar();

    /**
     * Cuenta el número de clientes activos
     *
     * @return Número de clientes activos
     */
    long contarActivos();

    /**
     * Cuenta el número de ventas realizadas por un cliente
     *
     * @param clienteId Identificador del cliente
     * @return Número de ventas del cliente
     */
    long contarVentasPorCliente(Long clienteId);
}