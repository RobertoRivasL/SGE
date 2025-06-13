package informviva.gest.model;

// Archivo: Notificacion.java


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    private String id;
    private String usuario;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private LocalDateTime fechaCreacion;
    private boolean leida;
    private LocalDateTime fechaLectura;
    private int prioridad;

}




