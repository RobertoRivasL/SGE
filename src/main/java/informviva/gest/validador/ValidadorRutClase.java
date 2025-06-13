package informviva.gest.validador;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementación de la lógica para validar un RUT chileno.
 */
public class ValidadorRutClase implements ConstraintValidator<ValidadorRut, String> {

    @Override
    public void initialize(ValidadorRut constraintAnnotation) {
        // No necesitas inicializar nada en este caso.
    }

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        rut = rut.replace(".", "").replace("-", "").toUpperCase();
        if (rut.length() < 2) return false;

        try {
            String cuerpo = rut.substring(0, rut.length() - 1);
            String dv = rut.substring(rut.length() - 1);

            int rutNum = Integer.parseInt(cuerpo);
            int m = 0;
            int r = 1;
            for (; rutNum != 0; rutNum /= 10) {
                r = (r + rutNum % 10 * (9 - m++ % 6)) % 11;
            }

            String dvEsperado = (r != 0) ? String.valueOf((char) (r + 47)) : "K";

            return dv.equals(dvEsperado);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
