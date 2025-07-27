package informviva.gest.service.impl;

import informviva.gest.exception.RecursoNoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación base abstracta que proporciona operaciones CRUD comunes
 * para todos los servicios del sistema.
 *
 * Esta clase aplica los principios SOLID:
 * - S: Responsabilidad única para operaciones CRUD básicas
 * - O: Abierto para extensión mediante herencia
 * - L: Las subclases pueden sustituir a la clase base
 * - I: Interface segregada por tipo de entidad
 * - D: Depende de abstracciones (JpaRepository)
 *
 * @param <T> Tipo de entidad
 * @param <ID> Tipo del identificador de la entidad
 *
 * @author Tu nombre
 * @version 1.0
 */
public abstract class BaseServiceImpl<T, ID> {

    protected final JpaRepository<T, ID> repository;

    /**
     * Constructor que inyecta la dependencia del repositorio
     *
     * @param repository Repositorio JPA para la entidad
     */
    protected BaseServiceImpl(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    /**
     * Busca todas las entidades
     *
     * @return Lista de todas las entidades
     */
    @Transactional(readOnly = true)
    public List<T> buscarTodos() {
        return repository.findAll();
    }

    /**
     * Busca todas las entidades con paginación
     *
     * @param pageable Información de paginación
     * @return Página de entidades
     */
    @Transactional(readOnly = true)
    public Page<T> buscarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Busca una entidad por su identificador
     *
     * @param id Identificador de la entidad
     * @return Optional con la entidad si existe
     */
    @Transactional(readOnly = true)
    public Optional<T> buscarPorId(ID id) {
        return repository.findById(id);
    }

    /**
     * Busca una entidad por su identificador y lanza excepción si no existe
     *
     * @param id Identificador de la entidad
     * @return La entidad encontrada
     * @throws RecursoNoEncontradoException si la entidad no existe
     */
    @Transactional(readOnly = true)
    public T obtenerPorId(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Recurso no encontrado con ID: " + id));
    }

    /**
     * Guarda una nueva entidad o actualiza una existente
     *
     * @param entidad Entidad a guardar
     * @return Entidad guardada
     */
    @Transactional
    public T guardar(T entidad) {
        return repository.save(entidad);
    }

    /**
     * Guarda múltiples entidades
     *
     * @param entidades Lista de entidades a guardar
     * @return Lista de entidades guardadas
     */
    @Transactional
    public List<T> guardarTodos(List<T> entidades) {
        return repository.saveAll(entidades);
    }

    /**
     * Elimina una entidad por su identificador
     *
     * @param id Identificador de la entidad a eliminar
     * @throws RecursoNoEncontradoException si la entidad no existe
     */
    @Transactional
    public void eliminar(ID id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "No se puede eliminar. Recurso no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Elimina una entidad
     *
     * @param entidad Entidad a eliminar
     */
    @Transactional
    public void eliminar(T entidad) {
        repository.delete(entidad);
    }

    /**
     * Verifica si existe una entidad con el identificador dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existe(ID id) {
        return repository.existsById(id);
    }

    /**
     * Cuenta el número total de entidades
     *
     * @return Número total de entidades
     */
    @Transactional(readOnly = true)
    public long contar() {
        return repository.count();
    }

    /**
     * Obtiene el nombre de la clase de entidad para mensajes de error
     *
     * @return Nombre simple de la clase
     */
    protected abstract String getNombreEntidad();
}