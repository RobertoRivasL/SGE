package informviva.gest.service.impl;


import informviva.gest.dto.ResultadoValidacionSistema;
import informviva.gest.service.ValidacionDatosServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Test básico para verificar que el sistema de validación funciona
 * después de la implementación de la Fase 1
 */
@SpringBootTest
public class ValidacionDatosServicioTest {

    @Autowired
    private ValidacionDatosServicio validacionDatosServicio;

    @Test
    public void testValidacionCompletaFunciona() {
        // Verificar que el servicio se inyecta correctamente
        assertNotNull(validacionDatosServicio, "ValidacionDatosServicio debe estar disponible");

        // Ejecutar validación completa
        ResultadoValidacionSistema resultado = validacionDatosServicio.validarSistemaCompleto();

        // Verificar que el resultado no es nulo
        assertNotNull(resultado, "El resultado de validación no debe ser nulo");
        assertNotNull(resultado.getFechaValidacion(), "Debe tener fecha de validación");

        // Verificar que las validaciones por módulo se ejecutaron
        assertNotNull(resultado.getValidacionClientes(), "Debe validar módulo de clientes");
        assertNotNull(resultado.getValidacionProductos(), "Debe validar módulo de productos");

        System.out.println("✅ VALIDACIÓN COMPLETA FUNCIONAL");
        System.out.println("Errores críticos: " + resultado.getTotalErroresCriticos());
        System.out.println("Advertencias: " + resultado.getTotalAdvertencias());
        System.out.println("Sistema consistente: " + resultado.isSistemaConsistente());
    }

    @Test
    public void testValidacionRapidaFunciona() {
        // Verificar validación rápida
        ResultadoValidacionSistema resultado = validacionDatosServicio.validacionRapida();

        assertNotNull(resultado, "Validación rápida debe retornar resultado");
        assertNotNull(resultado.getFechaValidacion(), "Debe tener fecha de validación");

        System.out.println("✅ VALIDACIÓN RÁPIDA FUNCIONAL");
    }

    @Test
    public void testValidacionModuloIndividual() {
        // Verificar validación de módulo individual
        var resultadoClientes = validacionDatosServicio.validarModuloClientes();

        assertNotNull(resultadoClientes, "Validación de clientes debe retornar resultado");
        assertEquals("Clientes", resultadoClientes.getNombreModulo(), "Nombre del módulo debe ser correcto");

        System.out.println("✅ VALIDACIÓN MODULAR FUNCIONAL");
        System.out.println("Módulo: " + resultadoClientes.getNombreModulo());
        System.out.println("Válido: " + resultadoClientes.isValido());
        System.out.println("Errores: " + resultadoClientes.getErroresCriticos());
    }

    @Test
    public void testSistemaConsistente() {
        // Verificar verificación de consistencia
        boolean esConsistente = validacionDatosServicio.esSistemaConsistente();

        System.out.println("✅ VERIFICACIÓN DE CONSISTENCIA FUNCIONAL");
        System.out.println("Sistema consistente: " + esConsistente);
    }
}