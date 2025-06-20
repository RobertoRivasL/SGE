package informviva.gest.validador;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementación del validador para RUTs chilenos
 *
 * @author Roberto Rivas
 * @version 2.0
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

        return ValidadorRutUtil.validar(rut);
    }
}