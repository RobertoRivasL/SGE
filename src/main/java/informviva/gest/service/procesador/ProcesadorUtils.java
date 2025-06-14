package informviva.gest.service.procesador;


import java.util.Map;

public class ProcesadorUtils {

    public static String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}