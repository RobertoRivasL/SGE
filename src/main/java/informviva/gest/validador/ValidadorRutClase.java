package informviva.gest.validador;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementación del validador para RUTs chilenos
 * Incluye lógica de validación centralizada
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public class ValidadorRutClase implements ConstraintValidator<ValidadorRut, String> {

    @Override
    public void initialize(ValidadorRut constraintAnnotation) {
        // No requiere inicialización específica
    }

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        // Si el RUT es null o vacío, se considera válido (usar @NotNull o @NotEmpty para campos obligatorios)
        if (rut == null || rut.trim().isEmpty()) {
            return true;
        }

        return validar(rut);
    }

    /**
     * Valida un RUT chileno
     *
     * @param rut RUT a validar (puede contener puntos y guión)
     * @return true si el RUT es válido, false en caso contrario
     */
    public static boolean validar(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        // Limpiar el RUT (eliminar puntos y guión)
        rut = rut.replace(".", "").replace("-", "").toUpperCase();

        // Verificar formato básico
        if (!rut.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        try {
            // Calcular dígito verificador
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