package informviva.gest.service.procesador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;


/**
 * Factory para obtener el procesador adecuado según el tipo de entidad
 */
/**
 * Factory para obtener el procesador adecuado según el tipo de entidad
 */
@Component
public class ProcesadorFactory {

    @Autowired
    private List<ProcesadorEntidad<?>> procesadores;

    @SuppressWarnings("unchecked")
    public <T> ProcesadorEntidad<T> obtenerProcesador(String tipoEntidad) {
        return (ProcesadorEntidad<T>) procesadores.stream()
                .filter(procesador -> procesador.getTipoEntidad().equalsIgnoreCase(tipoEntidad))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay procesador para: " + tipoEntidad));
    }

    public List<String> obtenerTiposDisponibles() {
        return procesadores.stream()
                .map(ProcesadorEntidad::getTipoEntidad)
                .toList();
    }
}