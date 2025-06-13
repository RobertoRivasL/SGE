package informviva.gest.model;

// Archivo: TipoNotificacion.java


public enum TipoNotificacion {
    ERROR(3),
    ADVERTENCIA(2),
    EXITO(1),
    INFORMACION(0);

    private final int prioridadDefecto;

    TipoNotificacion(int prioridadDefecto) {
        this.prioridadDefecto = prioridadDefecto;
    }

    public int getPrioridadDefecto() {
        return prioridadDefecto;
    }
}