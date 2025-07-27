package informviva.gest.service;

import informviva.gest.dto.ClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface del servicio de clientes que define el contrato
 * para la gestión de clientes en el sistema.
 *
 * Aplicación del principio de Inversión de Dependencias (D de SOLID):
 * - Los módulos de alto nivel no dependen de módulos de bajo nivel
 * - Ambos dependen de abstracciones (esta interface)
 *
 * @author Tu nombre
 * @version 1.0
 */
public interface ClienteServicio {

    /**
     * Obtiene todos los clientes como DTOs
     *
     * @return Lista de ClienteDTO
     */
    List<ClienteDTO> buscarTodosDTO();

    /**
     * Obtiene todos los clientes con paginación como DTOs
     *
     * @param pageable Información de paginación
     * @return Página de ClienteDTO
     */
    Page<ClienteDTO> buscarTodosDTO(Pageable pageable);

    /**
     * Busca un cliente por su ID y lo devuelve como DTO
     *
     * @param id Identificador del cliente
     * @return ClienteDTO encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ClienteDTO buscarPorIdDTO(Long id);

    /**
     * Guarda un nuevo cliente desde un DTO
     *
     * @param clienteDTO Datos del cliente a guardar
     * @return ClienteDTO guardado con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ClienteDTO guardarDTO(ClienteDTO clienteDTO);

    /**
     * Actualiza un cliente existente desde un DTO
     *
     * @param id Identificador del cliente a actualizar
     * @param clienteDTO Nuevos datos del cliente
     * @return ClienteDTO actualizado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ClienteDTO actualizarDTO(Long id, ClienteDTO clienteDTO);

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
     * Busca clientes por texto libre (nombre, apellido, email)
     *
     * @param texto Texto a buscar
     * @return Lista de ClienteDTO que coinciden
     */
    List<ClienteDTO> buscarPorTexto(String texto);

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
    boolean rutEsValido(String rut);

    /**
     * Valida si un email tiene el formato correcto
     *
     * @param email Email a validar
     * @return true si es válido, false en caso contrario
     */
    boolean emailEsValido(String email);

    /**
     * Cuenta el número de clientes activos
     *
     * @return Número de clientes activos
     */
    long contarClientesActivos();

    /**
     * Busca todos los clientes activos
     *
     * @return Lista de ClienteDTO activos
     */
    List<ClienteDTO> buscarClientesActivos();

    /**
     * Activa un cliente (marca como activo)
     *
     * @param id Identificador del cliente
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void activarCliente(Long id);

    /**
     * Desactiva un cliente (marca como inactivo)
     *
     * @param id Identificador del cliente
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void desactivarCliente(Long id);

    /**
     * Cuenta el número de ventas realizadas por un cliente
     *
     * @param clienteId Identificador del cliente
     * @return Número de ventas del cliente
     */
    long contarVentasPorCliente(Long clienteId);

    /**
     * Elimina un cliente por su ID
     *
     * @param id Identificador del cliente a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el cliente no existe
     */
    void eliminar(Long id);

    /**
     * Verifica si existe un cliente con el ID dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existe(Long id);

    /**
     * Cuenta el número total de clientes
     *
     * @return Número total de clientes
     */
    long contar();
}