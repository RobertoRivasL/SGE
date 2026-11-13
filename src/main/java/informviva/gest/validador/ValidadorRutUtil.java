package informviva.gest.validador;

public final class ValidadorRutUtil {

    private ValidadorRutUtil() {
    }

    public static boolean validar(String rut) {
        if (rut == null) {
            return false;
        }

        String rutNormalizado = rut.replace(".", "").replace("-", "").toUpperCase();
        if (!rutNormalizado.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        int suma = 0;
        int factor = 2;
        for (int i = rutNormalizado.length() - 2; i >= 0; i--) {
            suma += Character.getNumericValue(rutNormalizado.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }

        int dvEsperado = 11 - (suma % 11);
        char dvCalculado = dvEsperado == 11
                ? '0'
                : dvEsperado == 10
                ? 'K'
                : Character.forDigit(dvEsperado, 10);
        return rutNormalizado.charAt(rutNormalizado.length() - 1) == dvCalculado;
    }
}
