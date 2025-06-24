package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para resultado de importación de clientes
 * @author Roberto Rivas
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoImportacionCliente {
    private int registrosExitosos;
    private int registrosConError;
    private int registrosOmitidos;
    private String mensaje;
    private boolean exitoso;

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
}
