package informviva.gest.validador;

public class ValidadorRutUtil {

    public static boolean validar(String rut) {
        rut = rut.replace(".", "").replace("-", "").toUpperCase();
        if (!rut.matches("\\d{7,8}[0-9K]")) return false;

        try {
            int suma = 0;
            int factor = 2;
            for (int i = rut.length() - 2; i >= 0; i--) {
                suma += Character.getNumericValue(rut.charAt(i)) * factor;
                factor = factor == 7 ? 2 : factor + 1;
            }
            int dvEsperado = 11 - (suma % 11);
            char dvCalculado = (dvEsperado == 11) ? '0' : (dvEsperado == 10) ? 'K' : Character.forDigit(dvEsperado, 10);
            return rut.charAt(rut.length() - 1) == dvCalculado;
        } catch (Exception e) {
            return false;
        }
    }
}
