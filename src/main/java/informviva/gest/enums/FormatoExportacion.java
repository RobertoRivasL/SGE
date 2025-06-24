package informviva.gest.enums;

/**
 * Enum para los formatos de exportación soportados
 * @author Roberto Rivas
 * @version 1.0
 */
public enum FormatoExportacion {
    PDF("PDF", "application/pdf"),
    EXCEL("Excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("CSV", "text/csv");

    private final String descripcion;
    private final String mimeType;

    FormatoExportacion(String descripcion, String mimeType) {
        this.descripcion = descripcion;
        this.mimeType = mimeType;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMimeType() {
        return mimeType;
    }
}
