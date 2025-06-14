package informviva.gest.service.procesador;


import informviva.gest.dto.ValidacionResultadoDTO;

import java.util.Map;

/**
 * Interfaz común para procesadores de entidades
 */
public interface ProcesadorEntidad<T> {
    String getTipoEntidad();
    T mapearDesdeArchivo(Map<String, Object> fila, int numeroFila);
    boolean existeEntidad(T entidad);
    void guardarEntidad(T entidad);
    ValidacionResultadoDTO validarEntidad(T entidad, int numeroFila);
}