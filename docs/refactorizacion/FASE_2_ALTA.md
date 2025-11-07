# FASE 2 - PROBLEMAS DE ALTA PRIORIDAD (Semana 3-4)

## Objetivo
Resolver problemas de diseño y arquitectura que generan duplicación de código, repositorios sobrecargados y validaciones inconsistentes.

---

## TAREA 1: Consolidar Validación (Eliminar Duplicación)

### Problema Identificado
La validación se repite en 3 lugares diferentes:
1. **Controladores:** Validación manual con `resultado.rejectValue()`
2. **Servicios:** Método `validarClienteDTO()` lanza excepciones
3. **DTOs:** Anotaciones de validación (`@NotNull`, `@Email`, etc.)

### Archivos Afectados
1. `controlador/ClienteControlador.java:201-221` - Validación manual
2. `service/impl/ClienteServicioImpl.java:344-370` - validarClienteDTO()
3. `dto/ClienteDTO.java` - Anotaciones de validación
4. `validador/ClienteValidador.java` (si existe)

### Solución Propuesta

#### Paso 1.1: Estrategia de validación unificada

**Principio:** Validaciones en un solo lugar con niveles:
- **Nivel 1 (DTOs):** Validaciones sintácticas (formato, nulidad, longitud)
- **Nivel 2 (Servicios):** Validaciones semánticas (lógica de negocio, reglas complejas)
- **Nivel 3 (Controladores):** Solo orquestación, sin validación

#### Paso 1.2: Refactorizar validaciones de DTO

**Mejorar: `dto/ClienteDTO.java`**
```java
package informviva.gest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$",
            message = "RUT debe tener formato XX.XXX.XXX-X")
    private String rut;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe tener formato válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    @Pattern(regexp = "^(\\+56)?[0-9]{9,11}$",
            message = "Teléfono debe tener formato válido (+569XXXXXXXX o 9XXXXXXXX)")
    private String telefono;

    @Size(max = 200, message = "Dirección no debe exceder 200 caracteres")
    private String direccion;

    private Boolean activo;

    // Getters y setters
}
```

#### Paso 1.3: Crear validador de negocio centralizado

**Nuevo archivo: `validador/ClienteNegocioValidador.java`**
```java
package informviva.gest.validador;

import informviva.gest.dto.ClienteDTO;
import informviva.gest.exception.ValidacionNegocioException;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.util.RutValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteNegocioValidador {

    private final ClienteRepositorio clienteRepositorio;
    private final RutValidator rutValidator;

    /**
     * Valida reglas de negocio complejas
     */
    public void validarParaCreacion(ClienteDTO dto) {
        validarRutUnico(dto.getRut(), null);
        validarEmailUnico(dto.getEmail(), null);
        validarRutValido(dto.getRut());
    }

    /**
     * Valida reglas de negocio para actualización
     */
    public void validarParaActualizacion(ClienteDTO dto) {
        if (dto.getId() == null) {
            throw new ValidacionNegocioException("ID es requerido para actualización");
        }

        validarRutUnico(dto.getRut(), dto.getId());
        validarEmailUnico(dto.getEmail(), dto.getId());
        validarRutValido(dto.getRut());
    }

    private void validarRutUnico(String rut, Long idExcluir) {
        boolean existe;

        if (idExcluir != null) {
            // Para actualización: excluir el mismo cliente
            existe = clienteRepositorio.findByRut(rut)
                .filter(c -> !c.getId().equals(idExcluir))
                .isPresent();
        } else {
            // Para creación
            existe = clienteRepositorio.existsByRut(rut);
        }

        if (existe) {
            throw new ValidacionNegocioException("Ya existe un cliente con el RUT: " + rut);
        }
    }

    private void validarEmailUnico(String email, Long idExcluir) {
        boolean existe;

        if (idExcluir != null) {
            existe = clienteRepositorio.findByEmail(email)
                .filter(c -> !c.getId().equals(idExcluir))
                .isPresent();
        } else {
            existe = clienteRepositorio.existsByEmail(email);
        }

        if (existe) {
            throw new ValidacionNegocioException("Ya existe un cliente con el email: " + email);
        }
    }

    private void validarRutValido(String rut) {
        if (!rutValidator.esValido(rut)) {
            throw new ValidacionNegocioException("El RUT no es válido: " + rut);
        }
    }
}
```

**Nuevo archivo: `util/RutValidator.java`**
```java
package informviva.gest.util;

import org.springframework.stereotype.Component;

@Component
public class RutValidator {

    public boolean esValido(String rut) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }

        // Eliminar puntos y guión
        String rutLimpio = rut.replaceAll("[.-]", "");

        if (rutLimpio.length() < 2) {
            return false;
        }

        // Separar dígito verificador
        String rutNumero = rutLimpio.substring(0, rutLimpio.length() - 1);
        String dvString = rutLimpio.substring(rutLimpio.length() - 1).toUpperCase();

        try {
            int numero = Integer.parseInt(rutNumero);
            char dvCalculado = calcularDigitoVerificador(numero);
            char dvIngresado = dvString.charAt(0);

            return dvCalculado == dvIngresado;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private char calcularDigitoVerificador(int rut) {
        int suma = 0;
        int multiplicador = 2;

        while (rut > 0) {
            suma += (rut % 10) * multiplicador;
            rut /= 10;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);

        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return (char) ('0' + resto);
    }
}
```

#### Paso 1.4: Refactorizar servicio para usar validador

**Antes (ClienteServicioImpl.java:344-370):**
```java
private void validarClienteDTO(ClienteDTO clienteDTO) {
    // ❌ Validación duplicada - ya está en DTO y en controlador
    if (clienteDTO.getNombre() == null || clienteDTO.getNombre().trim().isEmpty()) {
        throw new IllegalArgumentException("El nombre es obligatorio");
    }

    if (!rutEsValido(clienteDTO.getRut())) {
        throw new IllegalArgumentException("RUT no válido");
    }

    if (existePorRut(clienteDTO.getRut())) {
        throw new IllegalArgumentException("Ya existe cliente con ese RUT");
    }

    if (existePorEmail(clienteDTO.getEmail())) {
        throw new IllegalArgumentException("Ya existe cliente con ese email");
    }
}
```

**Después (ClienteServicioImpl.java):**
```java
@Service
@RequiredArgsConstructor
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;
    private final ClienteNegocioValidador negocioValidador;
    private final ModelMapper mapper;

    @Override
    @Transactional
    public ClienteDTO guardar(ClienteDTO dto) {
        // ✅ Validación de negocio en un solo lugar
        if (dto.getId() == null) {
            negocioValidador.validarParaCreacion(dto);
        } else {
            negocioValidador.validarParaActualizacion(dto);
        }

        Cliente cliente = mapper.map(dto, Cliente.class);
        Cliente guardado = clienteRepositorio.save(cliente);

        return mapper.map(guardado, ClienteDTO.class);
    }

    // ❌ ELIMINAR estos métodos
    // private void validarClienteDTO(ClienteDTO clienteDTO) { ... }
    // private boolean rutEsValido(String rut) { ... }
}
```

#### Paso 1.5: Simplificar controlador

**Antes (ClienteControlador.java:201-221):**
```java
@PostMapping("/guardar")
public String guardarCliente(@Valid @ModelAttribute("cliente") ClienteDTO cliente,
                            BindingResult resultado,
                            RedirectAttributes redirectAttributes) {

    // ❌ Validación manual redundante
    if (!clienteServicio.rutEsValido(cliente.getRut())) {
        resultado.rejectValue("rut", "error.cliente",
            MensajesConstantes.ERROR_RUT_INVALIDO);
    }

    if (clienteServicio.existeClienteConEmail(cliente.getEmail())) {
        resultado.rejectValue("email", "error.cliente", "Ya existe un cliente con ese email");
    }

    if (resultado.hasErrors()) {
        return "clientes/formulario";
    }

    try {
        clienteServicio.guardar(cliente);
        redirectAttributes.addFlashAttribute("success", "Cliente guardado");
        return "redirect:/clientes/lista";
    } catch (Exception e) {
        resultado.rejectValue("nombre", "error.cliente", e.getMessage());
        return "clientes/formulario";
    }
}
```

**Después (ClienteControlador.java):**
```java
@PostMapping("/guardar")
public String guardarCliente(@Valid @ModelAttribute("cliente") ClienteDTO cliente,
                            BindingResult resultado,
                            RedirectAttributes redirectAttributes) {

    // ✅ Solo verificar errores de validación sintáctica (DTO)
    if (resultado.hasErrors()) {
        return "clientes/formulario";
    }

    try {
        // ✅ Servicio maneja validación de negocio
        clienteServicio.guardar(cliente);
        redirectAttributes.addFlashAttribute("success", "Cliente guardado exitosamente");
        return "redirect:/clientes/lista";

    } catch (ValidacionNegocioException e) {
        // ✅ Manejar excepciones de negocio
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/clientes/formulario";
    }
}
```

#### Paso 1.6: Crear exception personalizada

**Nuevo archivo: `exception/ValidacionNegocioException.java`**
```java
package informviva.gest.exception;

public class ValidacionNegocioException extends RuntimeException {

    public ValidacionNegocioException(String message) {
        super(message);
    }

    public ValidacionNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Actualizar GlobalExceptionHandler:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    // ... manejadores existentes

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<ErrorResponse> handleValidacionNegocio(ValidacionNegocioException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validación de Negocio")
            .message(ex.getMessage())
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacionDTO(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errores.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validación de Datos")
            .message("Errores de validación")
            .detalles(errores)
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}
```

### Checklist de Implementación

- [ ] **Día 1:** Crear infraestructura de validación
  - [ ] Crear `RutValidator.java`
  - [ ] Crear `ValidacionNegocioException.java`
  - [ ] Actualizar `GlobalExceptionHandler`

- [ ] **Día 2:** Validadores de negocio
  - [ ] Crear `ClienteNegocioValidador.java`
  - [ ] Crear `ProductoNegocioValidador.java`
  - [ ] Crear `VentaNegocioValidador.java`

- [ ] **Día 3:** Refactorizar DTOs
  - [ ] Mejorar anotaciones en `ClienteDTO`
  - [ ] Mejorar anotaciones en `ProductoDTO`
  - [ ] Mejorar anotaciones en `VentaDTO`

- [ ] **Día 4:** Refactorizar servicios
  - [ ] Eliminar validación duplicada en `ClienteServicioImpl`
  - [ ] Eliminar validación duplicada en `ProductoServicioImpl`
  - [ ] Eliminar validación duplicada en `VentaServicioImpl`

- [ ] **Día 5:** Refactorizar controladores
  - [ ] Simplificar `ClienteControlador`
  - [ ] Simplificar `ProductoControlador`
  - [ ] Simplificar `VentaControlador`

- [ ] **Día 6:** Testing
  - [ ] Tests unitarios para `RutValidator`
  - [ ] Tests unitarios para validadores de negocio
  - [ ] Tests de integración para validación end-to-end

### Métricas de Éxito
- ✅ 0 validaciones duplicadas entre capas
- ✅ DTOs con validaciones sintácticas completas
- ✅ Servicios con validaciones de negocio centralizadas
- ✅ Controladores sin lógica de validación

---

## TAREA 2: Refactorizar ClienteRepositorio (470 líneas → ~100 líneas)

### Problema Identificado
`ClienteRepositorio.java` tiene:
- **470 líneas de código**
- **60+ métodos**
- Queries duplicadas
- Métodos innecesarios que Spring Data resuelve automáticamente
- Nombres de métodos extremadamente largos

### Archivos Afectados
1. `repository/ClienteRepositorio.java` (470 líneas, 60+ métodos)

### Problemas Específicos Identificados

#### Problema 2.1: Queries Duplicadas (Líneas 74-87)
```java
@Query("SELECT c FROM Cliente c WHERE " +
    "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR ...")
List<Cliente> buscarPorTexto(@Param("busqueda") String busqueda);

@Query("SELECT c FROM Cliente c WHERE " +
    "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR ...")  // ❌ DUPLICADA
Page<Cliente> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);
```

#### Problema 2.2: Nombres Extremadamente Largos (Línea 378)
```java
Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRutContaining(
    String nombre, String apellido, String email, String rut, Pageable pageable);
```

#### Problema 2.3: Métodos Innecesarios
Spring Data JPA ya resuelve automáticamente:
- `findByActivoTrue()` → `findByActivo(true)`
- `existsByEmailIgnoreCase()` → Ya existe `existsByEmail()`
- `countByActivoTrue()` → `countByActivo(true)`

#### Problema 2.4: Proyecciones Complicadas (Línea 286)
```java
interface ClienteListProjection {
    Long getId();
    String getRut();
    default String getNombreCompleto() {
        return getNombre() + " " + getApellido();
    }
}
```

### Solución Propuesta

#### Paso 2.1: Crear especificaciones para búsquedas complejas

**Nuevo archivo: `repository/specification/ClienteSpecification.java`**
```java
package informviva.gest.repository.specification;

import informviva.gest.model.Cliente;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ClienteSpecification {

    public static Specification<Cliente> buscarPorTexto(String busqueda) {
        return (root, query, cb) -> {
            if (busqueda == null || busqueda.trim().isEmpty()) {
                return cb.conjunction();
            }

            String patron = "%" + busqueda.toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("apellido")), patron),
                cb.like(cb.lower(root.get("email")), patron),
                cb.like(cb.lower(root.get("rut")), patron),
                cb.like(cb.lower(root.get("telefono")), patron)
            );
        };
    }

    public static Specification<Cliente> activo(Boolean activo) {
        return (root, query, cb) -> {
            if (activo == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("activo"), activo);
        };
    }

    public static Specification<Cliente> porCiudad(String ciudad) {
        return (root, query, cb) -> {
            if (ciudad == null || ciudad.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("ciudad")), ciudad.toLowerCase());
        };
    }

    public static Specification<Cliente> conCriterios(ClienteCriteriosBusqueda criterios) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criterios.getBusqueda() != null && !criterios.getBusqueda().trim().isEmpty()) {
                String patron = "%" + criterios.getBusqueda().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("apellido")), patron),
                    cb.like(cb.lower(root.get("email")), patron),
                    cb.like(cb.lower(root.get("rut")), patron)
                ));
            }

            if (criterios.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), criterios.getActivo()));
            }

            if (criterios.getCiudad() != null && !criterios.getCiudad().trim().isEmpty()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("ciudad")),
                    criterios.getCiudad().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

**Nuevo DTO de criterios: `dto/ClienteCriteriosBusqueda.java`**
```java
package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteCriteriosBusqueda {
    private String busqueda;
    private Boolean activo;
    private String ciudad;
}
```

#### Paso 2.2: Refactorizar repositorio

**Antes (ClienteRepositorio.java - 470 líneas):**
```java
@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    // ❌ 60+ métodos con queries duplicadas y nombres largos

    @Query("SELECT c FROM Cliente c WHERE " +
        "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR ...")
    List<Cliente> buscarPorTexto(@Param("busqueda") String busqueda);

    @Query("SELECT c FROM Cliente c WHERE " +
        "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR ...")
    Page<Cliente> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRutContaining(
        String nombre, String apellido, String email, String rut, Pageable pageable);

    List<Cliente> findByActivoTrue();
    List<Cliente> findByActivo(boolean activo);  // ❌ Duplicado

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);  // ❌ Duplicado

    // ... 50+ métodos más
}
```

**Después (ClienteRepositorio.java - ~100 líneas):**
```java
package informviva.gest.repository;

import informviva.gest.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long>,
                                           JpaSpecificationExecutor<Cliente> {

    // ============================================
    // BÚSQUEDAS BÁSICAS (Spring Data automático)
    // ============================================

    Optional<Cliente> findByRut(String rut);

    Optional<Cliente> findByEmail(String email);

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);

    List<Cliente> findByActivo(boolean activo);

    Page<Cliente> findByActivo(boolean activo, Pageable pageable);

    // ============================================
    // BÚSQUEDAS PERSONALIZADAS (queries optimizadas)
    // ============================================

    /**
     * Busca clientes que han realizado compras
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "INNER JOIN c.ventas v " +
           "WHERE c.activo = true")
    List<Cliente> findClientesConVentas();

    /**
     * Busca clientes sin ventas
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.activo = true " +
           "AND NOT EXISTS (SELECT 1 FROM Venta v WHERE v.cliente = c)")
    List<Cliente> findClientesSinVentas();

    /**
     * Busca clientes por rango de fechas de registro
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.fechaRegistro BETWEEN :inicio AND :fin " +
           "ORDER BY c.fechaRegistro DESC")
    List<Cliente> findByFechaRegistroBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    /**
     * Busca clientes con compras mayores a un monto
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.totalCompras >= :montoMinimo " +
           "ORDER BY c.totalCompras DESC")
    List<Cliente> findClientesPorMontoMinimo(@Param("montoMinimo") Double montoMinimo);

    /**
     * Cuenta clientes activos
     */
    long countByActivo(boolean activo);

    /**
     * Top clientes por monto de compras
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.activo = true " +
           "ORDER BY c.totalCompras DESC")
    Page<Cliente> findTopClientesPorCompras(Pageable pageable);

    // ============================================
    // ELIMINADO:
    // - Queries duplicadas (buscarPorTexto con y sin Page)
    // - Métodos con nombres largos
    // - Proyecciones complejas
    // - Métodos que Spring resuelve automáticamente
    // ============================================

    // NOTA: Búsquedas complejas ahora usan Specifications
    // Ver: ClienteSpecification.buscarPorTexto()
    // Uso: clienteRepo.findAll(ClienteSpecification.buscarPorTexto(texto), pageable)
}
```

#### Paso 2.3: Actualizar servicio para usar Specifications

**Actualizar ClienteServicioImpl:**
```java
@Service
@RequiredArgsConstructor
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;

    @Override
    public Page<ClienteDTO> buscarPorTexto(String busqueda, Pageable pageable) {
        // ✅ Usar Specification en lugar de query duplicada
        Specification<Cliente> spec = ClienteSpecification.buscarPorTexto(busqueda);
        Page<Cliente> clientes = clienteRepositorio.findAll(spec, pageable);

        return clientes.map(this::convertirADTO);
    }

    @Override
    public Page<ClienteDTO> buscarConCriterios(ClienteCriteriosBusqueda criterios, Pageable pageable) {
        // ✅ Búsqueda compleja con Specification
        Specification<Cliente> spec = ClienteSpecification.conCriterios(criterios);
        Page<Cliente> clientes = clienteRepositorio.findAll(spec, pageable);

        return clientes.map(this::convertirADTO);
    }

    @Override
    public List<ClienteDTO> buscarActivos() {
        // ✅ Método simple de Spring Data
        return clienteRepositorio.findByActivo(true).stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
}
```

### Checklist de Implementación

- [ ] **Día 7:** Crear infraestructura de Specifications
  - [ ] Crear `ClienteSpecification.java`
  - [ ] Crear `ClienteCriteriosBusqueda.java`

- [ ] **Día 8:** Refactorizar ClienteRepositorio
  - [ ] Extender `JpaSpecificationExecutor`
  - [ ] Eliminar queries duplicadas (74-87)
  - [ ] Eliminar métodos innecesarios (356-375)
  - [ ] Eliminar proyecciones complejas (286-305)
  - [ ] Eliminar métodos con nombres largos (378)
  - [ ] Reducir a máximo 20 métodos esenciales

- [ ] **Día 9:** Actualizar servicios
  - [ ] Actualizar `ClienteServicioImpl` para usar Specifications
  - [ ] Actualizar controladores si es necesario

- [ ] **Día 10:** Aplicar mismo patrón a otros repositorios
  - [ ] Crear `ProductoSpecification`
  - [ ] Refactorizar `ProductoRepositorio`
  - [ ] Crear `VentaSpecification`
  - [ ] Refactorizar `VentaRepositorio`

- [ ] **Día 11:** Testing
  - [ ] Tests para Specifications
  - [ ] Tests de integración para repositorios
  - [ ] Verificar performance de queries

### Métricas de Éxito
- ✅ ClienteRepositorio < 150 líneas
- ✅ Máximo 20 métodos por repositorio
- ✅ 0 queries duplicadas
- ✅ 0 métodos innecesarios
- ✅ Uso de Specifications para búsquedas complejas

---

## TAREA 3: Crear Patrón Genérico para Importación

### Problema Identificado
Código casi idéntico entre `importarClientes()` e `importarProductos()`:
- Misma estructura: validar → leer → mapear → guardar
- Manejo de errores duplicado
- 62 líneas vs 28 líneas con lógica similar

### Archivos Afectados
1. `service/impl/ImportacionServicioImpl.java:57-119` (importarClientes)
2. `service/impl/ImportacionServicioImpl.java:122-150` (importarProductos)

### Solución Propuesta

#### Paso 3.1: Crear patrón Template Method genérico

**Nuevo archivo: `service/importacion/ImportacionTemplate.java`**
```java
package informviva.gest.service.importacion;

import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public abstract class ImportacionTemplate<T> {

    /**
     * Template Method: define el flujo de importación
     */
    public final ImportacionResultadoDTO importar(MultipartFile archivo) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();

        try {
            // Paso 1: Validar archivo
            if (!validarArchivo(archivo, resultado)) {
                return resultado;
            }

            // Paso 2: Leer datos
            List<Map<String, Object>> datos = leerArchivo(archivo);

            // Paso 3: Procesar datos
            procesarDatos(datos, resultado);

            // Paso 4: Post-procesamiento
            postProcesar(resultado);

        } catch (Exception e) {
            resultado.agregarError("Error general: " + e.getMessage());
        }

        return resultado;
    }

    // Métodos concretos (comunes a todas las importaciones)

    protected boolean validarArchivo(MultipartFile archivo, ImportacionResultadoDTO resultado) {
        if (archivo == null || archivo.isEmpty()) {
            resultado.agregarError("Archivo vacío o nulo");
            return false;
        }

        if (!esTipoValido(archivo)) {
            resultado.agregarError("Tipo de archivo no soportado");
            return false;
        }

        return true;
    }

    protected boolean esTipoValido(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        return contentType != null && (
            contentType.contains("spreadsheet") ||
            contentType.contains("excel") ||
            contentType.contains("csv")
        );
    }

    protected List<Map<String, Object>> leerArchivo(MultipartFile archivo) {
        if (esExcel(archivo)) {
            return getExcelReader().leer(archivo);
        }
        return getCsvReader().leer(archivo);
    }

    protected boolean esExcel(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        return contentType != null && contentType.contains("spreadsheet");
    }

    protected void procesarDatos(List<Map<String, Object>> datos,
                                 ImportacionResultadoDTO resultado) {
        for (int i = 0; i < datos.size(); i++) {
            try {
                Map<String, Object> fila = datos.get(i);
                procesarFila(fila, i + 2, resultado);  // +2 por header y 0-index
            } catch (Exception e) {
                resultado.agregarError("Error en fila " + (i + 2) + ": " + e.getMessage());
            }
        }
    }

    // Métodos abstractos (específicos de cada tipo de importación)

    protected abstract void procesarFila(Map<String, Object> fila,
                                        int numeroFila,
                                        ImportacionResultadoDTO resultado);

    protected abstract ExcelReader getExcelReader();

    protected abstract CsvReader getCsvReader();

    // Hook methods (opcionales)

    protected void postProcesar(ImportacionResultadoDTO resultado) {
        // Por defecto no hace nada
        // Las subclases pueden sobreescribir
    }
}
```

#### Paso 3.2: Implementar para Cliente

**Nuevo archivo: `service/importacion/ClienteImportacionTemplate.java`**
```java
package informviva.gest.service.importacion;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClienteImportacionTemplate extends ImportacionTemplate<Cliente> {

    private final ClienteServicio clienteServicio;
    private final ExcelReader excelReader;
    private final CsvReader csvReader;

    @Override
    protected void procesarFila(Map<String, Object> fila,
                                int numeroFila,
                                ImportacionResultadoDTO resultado) {

        String rut = extraerString(fila, "rut");

        // Verificar si ya existe
        if (clienteServicio.existePorRut(rut)) {
            resultado.agregarAdvertencia(
                "Fila " + numeroFila + ": Cliente con RUT " + rut + " ya existe"
            );
            return;
        }

        // Mapear y guardar
        Cliente cliente = mapearCliente(fila, numeroFila);
        clienteServicio.guardar(convertirADTO(cliente));

        resultado.incrementarExitosos();
    }

    private Cliente mapearCliente(Map<String, Object> fila, int numeroFila) {
        Cliente cliente = new Cliente();

        cliente.setRut(extraerString(fila, "rut"));
        cliente.setNombre(extraerString(fila, "nombre"));
        cliente.setApellido(extraerString(fila, "apellido"));
        cliente.setEmail(extraerString(fila, "email"));
        cliente.setTelefono(extraerString(fila, "telefono"));
        cliente.setDireccion(extraerString(fila, "direccion"));
        cliente.setCiudad(extraerString(fila, "ciudad"));
        cliente.setActivo(true);
        cliente.setFechaRegistro(LocalDateTime.now());

        return cliente;
    }

    private String extraerString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : null;
    }

    private ClienteDTO convertirADTO(Cliente cliente) {
        // Usar mapper
        return mapper.map(cliente, ClienteDTO.class);
    }

    @Override
    protected ExcelReader getExcelReader() {
        return excelReader;
    }

    @Override
    protected CsvReader getCsvReader() {
        return csvReader;
    }
}
```

#### Paso 3.3: Implementar para Producto

**Nuevo archivo: `service/importacion/ProductoImportacionTemplate.java`**
```java
package informviva.gest.service.importacion;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.Producto;
import informviva.gest.service.ProductoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductoImportacionTemplate extends ImportacionTemplate<Producto> {

    private final ProductoServicio productoServicio;
    private final ExcelReader excelReader;
    private final CsvReader csvReader;

    @Override
    protected void procesarFila(Map<String, Object> fila,
                                int numeroFila,
                                ImportacionResultadoDTO resultado) {

        String codigo = extraerString(fila, "codigo");

        // Verificar si ya existe
        if (productoServicio.existePorCodigo(codigo)) {
            resultado.agregarAdvertencia(
                "Fila " + numeroFila + ": Producto con código " + codigo + " ya existe"
            );
            return;
        }

        // Mapear y guardar
        Producto producto = mapearProducto(fila, numeroFila);
        productoServicio.guardar(convertirADTO(producto));

        resultado.incrementarExitosos();
    }

    private Producto mapearProducto(Map<String, Object> fila, int numeroFila) {
        Producto producto = new Producto();

        producto.setCodigo(extraerString(fila, "codigo"));
        producto.setNombre(extraerString(fila, "nombre"));
        producto.setDescripcion(extraerString(fila, "descripcion"));
        producto.setPrecio(extraerDouble(fila, "precio"));
        producto.setStock(extraerInteger(fila, "stock"));
        producto.setActivo(true);

        return producto;
    }

    private String extraerString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : null;
    }

    private Double extraerDouble(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        if (valor == null) return 0.0;
        if (valor instanceof Number) return ((Number) valor).doubleValue();
        return Double.parseDouble(valor.toString());
    }

    private Integer extraerInteger(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        if (valor == null) return 0;
        if (valor instanceof Number) return ((Number) valor).intValue();
        return Integer.parseInt(valor.toString());
    }

    private ProductoDTO convertirADTO(Producto producto) {
        return mapper.map(producto, ProductoDTO.class);
    }

    @Override
    protected ExcelReader getExcelReader() {
        return excelReader;
    }

    @Override
    protected CsvReader getCsvReader() {
        return csvReader;
    }
}
```

#### Paso 3.4: Refactorizar servicio principal

**Antes (ImportacionServicioImpl.java):**
```java
@Service
public class ImportacionServicioImpl implements ImportacionServicio {

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    // ... más dependencias

    @Override
    public ResultadoImportacion importarClientes(MultipartFile archivo) {
        // ❌ 62 líneas de código
        ResultadoImportacion resultado = new ResultadoImportacion();

        try {
            if (archivo.isEmpty()) {
                resultado.agregarError("Archivo vacío");
                return resultado;
            }

            List<Map<String, Object>> datos;
            if (esExcel(archivo)) {
                datos = excelReader.leer(archivo);
            } else {
                datos = csvReader.leer(archivo);
            }

            for (int i = 0; i < datos.size(); i++) {
                // ... lógica repetida
            }

        } catch (Exception e) {
            resultado.agregarError("Error: " + e.getMessage());
        }

        return resultado;
    }

    @Override
    public ResultadoImportacion importarProductos(MultipartFile archivo) {
        // ❌ Código casi idéntico al anterior
        // ... mismo código con pequeñas variaciones
    }
}
```

**Después (ImportacionServicioImpl.java):**
```java
@Service
@RequiredArgsConstructor
public class ImportacionServicioImpl implements ImportacionServicio {

    private final ClienteImportacionTemplate clienteImportacion;
    private final ProductoImportacionTemplate productoImportacion;

    @Override
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        // ✅ 1 línea - delega al template
        return clienteImportacion.importar(archivo);
    }

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        // ✅ 1 línea - delega al template
        return productoImportacion.importar(archivo);
    }
}
```

### Checklist de Implementación

- [ ] **Día 12:** Crear patrón Template
  - [ ] Crear `ImportacionTemplate.java` (clase abstracta)
  - [ ] Crear `ExcelReader` interface/clase
  - [ ] Crear `CsvReader` interface/clase

- [ ] **Día 13:** Implementar templates concretos
  - [ ] Crear `ClienteImportacionTemplate`
  - [ ] Crear `ProductoImportacionTemplate`

- [ ] **Día 14:** Refactorizar servicio principal
  - [ ] Actualizar `ImportacionServicioImpl`
  - [ ] Eliminar código duplicado
  - [ ] Testing de importación

### Métricas de Éxito
- ✅ 0 código duplicado entre importaciones
- ✅ Fácil agregar nuevas entidades (solo extender template)
- ✅ Método `importar*` con máximo 5 líneas
- ✅ Tests unitarios para cada template

---

## TAREA 4: Mover Lógica de DTOs a Servicios

### Problema Identificado
DTOs tienen lógica de negocio que debería estar en servicios:
- `VentaDTO.calcularTotales()` (líneas 119-139)
- `ClienteDTO.formatearRut()` (método estático)
- `ProductoDTO.calcularPrecioConImpuesto()`

### Archivos Afectados
1. `dto/VentaDTO.java:119-139,147-162` - Lógica de cálculo y manipulación
2. `dto/ClienteDTO.java:254-276` - Métodos de utilidad
3. `dto/ProductoDTO.java` - Métodos de cálculo

### Solución Propuesta

#### Paso 4.1: Extraer lógica de cálculo de VentaDTO

**Antes (VentaDTO.java:119-139):**
```java
public class VentaDTO {
    private List<ProductoVentaDTO> detalles;

    // ❌ Lógica de negocio en DTO
    public void calcularTotales() {
        if (this.detalles == null || this.detalles.isEmpty()) {
            this.subtotal = 0.0;
            this.impuestos = 0.0;
            this.total = 0.0;
            return;
        }

        double sumaSubtotal = detalles.stream()
            .mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad())
            .sum();

        this.subtotal = sumaSubtotal;
        this.impuestos = sumaSubtotal * 0.19;  // IVA 19%
        this.total = this.subtotal + this.impuestos;
    }
}
```

**Después (VentaDTO.java):**
```java
@Data
public class VentaDTO {
    private List<ProductoVentaDTO> detalles;
    private Double subtotal;
    private Double impuestos;
    private Double total;

    // ✅ Sin lógica de negocio
    // Solo getters/setters
}
```

**Nuevo servicio: `service/VentaCalculoServicio.java`**
```java
package informviva.gest.service;

import informviva.gest.dto.VentaDTO;

public interface VentaCalculoServicio {
    VentaDTO calcularTotales(VentaDTO venta);
    Double calcularSubtotal(VentaDTO venta);
    Double calcularImpuestos(Double subtotal);
    Double calcularTotal(Double subtotal, Double impuestos);
}
```

**Implementación: `service/impl/VentaCalculoServicioImpl.java`**
```java
package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.service.VentaCalculoServicio;
import org.springframework.stereotype.Service;

@Service
public class VentaCalculoServicioImpl implements VentaCalculoServicio {

    private static final double IVA = 0.19;

    @Override
    public VentaDTO calcularTotales(VentaDTO venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            venta.setSubtotal(0.0);
            venta.setImpuestos(0.0);
            venta.setTotal(0.0);
            return venta;
        }

        Double subtotal = calcularSubtotal(venta);
        Double impuestos = calcularImpuestos(subtotal);
        Double total = calcularTotal(subtotal, impuestos);

        venta.setSubtotal(subtotal);
        venta.setImpuestos(impuestos);
        venta.setTotal(total);

        return venta;
    }

    @Override
    public Double calcularSubtotal(VentaDTO venta) {
        return venta.getDetalles().stream()
            .mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad())
            .sum();
    }

    @Override
    public Double calcularImpuestos(Double subtotal) {
        return subtotal * IVA;
    }

    @Override
    public Double calcularTotal(Double subtotal, Double impuestos) {
        return subtotal + impuestos;
    }
}
```

#### Paso 4.2: Extraer utilidades de ClienteDTO

**Antes (ClienteDTO.java:254-276):**
```java
public class ClienteDTO {

    // ❌ Método estático de utilidad en DTO
    public static String formatearRut(String rut) {
        if (rut == null || rut.isEmpty()) {
            return "";
        }

        String rutLimpio = rut.replaceAll("[^0-9kK]", "");

        if (rutLimpio.length() < 2) {
            return rut;
        }

        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);

        // Formatear con puntos
        StringBuilder formatted = new StringBuilder();
        // ... lógica de formato
        return formatted.toString();
    }
}
```

**Después:**

**Nuevo archivo: `util/RutFormateador.java`**
```java
package informviva.gest.util;

import org.springframework.stereotype.Component;

@Component
public class RutFormateador {

    /**
     * Formatea RUT al formato XX.XXX.XXX-X
     */
    public String formatear(String rut) {
        if (rut == null || rut.isEmpty()) {
            return "";
        }

        String rutLimpio = limpiar(rut);

        if (rutLimpio.length() < 2) {
            return rut;
        }

        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);

        return formatearConPuntos(numero) + "-" + dv;
    }

    /**
     * Limpia RUT dejando solo números y K
     */
    public String limpiar(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }

    private String formatearConPuntos(String numero) {
        StringBuilder formatted = new StringBuilder(numero);
        int pos = formatted.length() - 3;

        while (pos > 0) {
            formatted.insert(pos, ".");
            pos -= 3;
        }

        return formatted.toString();
    }
}
```

**ClienteDTO ahora es un simple POJO:**
```java
@Data
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String rut;
    private String email;
    // ... solo campos

    // ✅ Sin métodos de utilidad
    // ✅ Sin lógica de negocio
}
```

### Checklist de Implementación

- [ ] **Día 15:** Crear servicios de cálculo
  - [ ] Crear `VentaCalculoServicio` y su implementación
  - [ ] Crear `ProductoCalculoServicio` (si aplica)

- [ ] **Día 16:** Crear utilidades
  - [ ] Mover `formatearRut()` a `RutFormateador`
  - [ ] Crear otros formateadores necesarios

- [ ] **Día 17:** Limpiar DTOs
  - [ ] Eliminar lógica de `VentaDTO`
  - [ ] Eliminar métodos estáticos de `ClienteDTO`
  - [ ] Eliminar cálculos de `ProductoDTO`

- [ ] **Día 18:** Actualizar controladores y servicios
  - [ ] Inyectar `VentaCalculoServicio` donde sea necesario
  - [ ] Inyectar `RutFormateador` donde sea necesario
  - [ ] Testing completo

### Métricas de Éxito
- ✅ DTOs son POJOs puros (solo datos)
- ✅ 0 lógica de negocio en DTOs
- ✅ 0 métodos de cálculo en DTOs
- ✅ Utilidades en clases `@Component` dedicadas

---

## RESUMEN DE LA FASE 2

### Duración Total: 18 días (aproximadamente 3 semanas)

### Resultado Esperado
Al finalizar la Fase 2, el proyecto deberá tener:

✅ **Validación consistente:**
- Validaciones sintácticas en DTOs
- Validaciones de negocio en servicios especializados
- Controladores sin lógica de validación

✅ **Repositorios optimizados:**
- Máximo 20 métodos por repositorio
- Uso de Specifications para búsquedas complejas
- 0 código duplicado

✅ **Código reutilizable:**
- Patrón Template Method para importaciones
- Fácil agregar nuevas entidades
- Servicios de cálculo separados

✅ **DTOs limpios:**
- Solo datos (POJOs)
- Sin lógica de negocio
- Sin métodos de utilidad

### Siguiente Paso
En la **FASE 3** (Media Prioridad) abordaremos:
- Try-catch excesivo → GlobalExceptionHandler
- Limpiar lógica de entidades
- Usar Enums en lugar de Strings
- Object Parameter Pattern

---

## Notas Importantes

1. **Migración gradual:** Aplicar cambios de forma incremental
2. **Testing exhaustivo:** Cada cambio debe tener tests
3. **Code reviews:** Revisar especialmente Specifications y Templates
4. **Documentación:** Actualizar docs con nuevos patrones
5. **Performance:** Medir tiempo de queries antes y después

---

**Última actualización:** 2025-11-07
**Autor:** Análisis automatizado Claude Code
**Versión:** 1.0
