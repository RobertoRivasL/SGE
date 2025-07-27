package informviva.gest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para la entidad Cliente
 *
 * Aplicación de principios:
 * - Encapsulación: Datos del cliente encapsulados en un objeto
 * - Separación de Responsabilidades: DTO para transferencia, Entidad para persistencia
 * - Abstracción: Representa el cliente desde la perspectiva del cliente
 *
 * @author Tu nombre
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    /**
     * Identificador único del cliente
     */
    private Long id;

    /**
     * Nombre del cliente
     * Obligatorio, longitud entre 2 y 50 caracteres
     */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    /**
     * Apellido del cliente
     * Obligatorio, longitud entre 2 y 50 caracteres
     */
    @NotBlank(message = "El apellido del cliente es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    /**
     * RUT del cliente en formato chileno
     * Obligatorio, formato: xx.xxx.xxx-x
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9kK]{1}$",
            message = "El RUT debe tener el formato xx.xxx.xxx-x")
    private String rut;

    /**
     * Email del cliente
     * Obligatorio, debe ser un email válido
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    /**
     * Teléfono del cliente
     * Opcional, máximo 20 caracteres
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    /**
     * Dirección del cliente
     * Opcional, máximo 200 caracteres
     */
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    /**
     * Estado del cliente (activo/inactivo)
     * Por defecto es true
     */
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del cliente
     * Solo lectura
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización
     * Solo lectura
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Notas adicionales sobre el cliente
     * Opcional, máximo 500 caracteres
     */
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    /**
     * Número de ventas realizadas al cliente
     * Campo calculado, no se persiste
     */
    private Long numeroVentas;

    /**
     * Constructor para crear DTO con datos básicos
     *
     * @param nombre Nombre del cliente
     * @param apellido Apellido del cliente
     * @param rut RUT del cliente
     * @param email Email del cliente
     */
    public ClienteDTO(String nombre, String apellido, String rut, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.email = email;
        this.activo = true;
    }

    /**
     * Obtiene el nombre completo del cliente
     *
     * @return Nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();

        if (nombre != null) {
            nombreCompleto.append(nombre);
        }

        if (apellido != null) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellido);
        }

        return nombreCompleto.toString();
    }

    /**
     * Obtiene las iniciales del cliente
     *
     * @return Iniciales del nombre y apellido
     */
    public String getIniciales() {
        StringBuilder iniciales = new StringBuilder();

        if (nombre != null && !nombre.isEmpty()) {
            iniciales.append(nombre.charAt(0));
        }

        if (apellido != null && !apellido.isEmpty()) {
            iniciales.append(apellido.charAt(0));
        }

        return iniciales.toString().toUpperCase();
    }

    /**
     * Verifica si el cliente tiene datos de contacto completos
     *
     * @return true si tiene email y teléfono
     */
    public boolean tieneContactoCompleto() {
        return email != null && !email.trim().isEmpty() &&
                telefono != null && !telefono.trim().isEmpty();
    }

    /**
     * Verifica si el cliente tiene dirección registrada
     *
     * @return true si tiene dirección
     */
    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    /**
     * Método para validar si el DTO tiene los datos mínimos requeridos
     *
     * @return true si tiene los datos básicos válidos
     */
    public boolean esValido() {
        return nombre != null && !nombre.trim().isEmpty() &&
                apellido != null && !apellido.trim().isEmpty() &&
                rut != null && !rut.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    /**
     * Genera una representación compacta del cliente
     *
     * @return String con formato "Nombre Apellido (RUT)"
     */
    public String toStringCompacto() {
        return String.format("%s (%s)",
                getNombreCompleto(),
                rut != null ? rut : "Sin RUT");
    }

    /**
     * Obtiene el RUT sin formato (solo números y dígito verificador)
     *
     * @return RUT sin puntos ni guión
     */
    public String getRutSinFormato() {
        if (rut == null) {
            return null;
        }
        return rut.replace(".", "").replace("-", "");
    }

    /**
     * Obtiene solo el cuerpo del RUT (sin dígito verificador)
     *
     * @return Cuerpo del RUT sin dígito verificador
     */
    public String getCuerpoRut() {
        String rutSinFormato = getRutSinFormato();
        if (rutSinFormato == null || rutSinFormato.length() < 2) {
            return null;
        }
        return rutSinFormato.substring(0, rutSinFormato.length() - 1);
    }

    /**
     * Obtiene el dígito verificador del RUT
     *
     * @return Dígito verificador del RUT
     */
    public String getDigitoVerificador() {
        String rutSinFormato = getRutSinFormato();
        if (rutSinFormato == null || rutSinFormato.isEmpty()) {
            return null;
        }
        return rutSinFormato.substring(rutSinFormato.length() - 1);
    }

    /**
     * Formatea el RUT si viene sin formato
     *
     * @param rutSinFormato RUT sin puntos ni guión
     * @return RUT formateado
     */
    public static String formatearRut(String rutSinFormato) {
        if (rutSinFormato == null || rutSinFormato.length() < 2) {
            return rutSinFormato;
        }

        String cuerpo = rutSinFormato.substring(0, rutSinFormato.length() - 1);
        String dv = rutSinFormato.substring(rutSinFormato.length() - 1);

        // Agregar puntos al cuerpo
        StringBuilder rutFormateado = new StringBuilder();
        int contador = 0;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            if (contador > 0 && contador % 3 == 0) {
                rutFormateado.insert(0, ".");
            }
            rutFormateado.insert(0, cuerpo.charAt(i));
            contador++;
        }

        rutFormateado.append("-").append(dv);
        return rutFormateado.toString();
    }
}