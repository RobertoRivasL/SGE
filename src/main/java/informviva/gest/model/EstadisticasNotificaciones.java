package informviva.gest.model;

// Archivo: EstadisticasNotificaciones.java


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasNotificaciones {
    private int totalNotificaciones;
    private int noLeidas;
    private int leidas;
    private Map<TipoNotificacion, Integer> porTipo;
    private int usuariosConNotificaciones;

}