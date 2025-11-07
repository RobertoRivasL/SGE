# FASE 1 - PROBLEMAS CRÍTICOS (Semana 1-2)

## Objetivo
Resolver los problemas arquitectónicos más graves que impactan la estabilidad, mantenibilidad y testabilidad del sistema.

---

## TAREA 1: Separar Interfaces - Métodos DTO vs Entidad

### Problema Identificado
Las interfaces de servicio tienen inconsistencias:
- Definen métodos que devuelven DTOs (`buscarPorIdDTO`)
- Los controladores llaman a métodos que devuelven entidades (`buscarPorId`)
- Los métodos llamados **NO EXISTEN** en las interfaces

### Archivos Afectados
1. `service/ClienteServicio.java:45,89,97,105,151`
2. `service/ProductoServicio.java` (similar)
3. `service/VentaServicio.java` (similar)
4. `controlador/ClienteControlador.java:116,281,289`
5. `controlador/api/ClienteRestControlador.java:96`

### Solución Propuesta

#### Paso 1.1: Crear dos interfaces separadas por cada servicio

**Para Cliente:**
```java
// Nueva estructura
public interface ClienteServicio {
    // Métodos que trabajan con DTOs
    ClienteDTO buscarPorId(Long id);
    ClienteDTO buscarPorRut(String rut);
    ClienteDTO buscarPorEmail(String email);
    ClienteDTO guardar(ClienteDTO dto);
    void eliminar(Long id);
    Page<ClienteDTO> buscarTodos(Pageable pageable);
}

public interface ClienteEntityService {
    // Métodos internos que trabajan con entidades
    Cliente obtenerEntidadPorId(Long id);
    Cliente guardarEntidad(Cliente cliente);
    // Solo para uso interno entre servicios
}
```

#### Paso 1.2: Refactorizar implementaciones

**Antes (ClienteServicioImpl.java):**
```java
@Override
public Cliente buscarPorId(Long id) {  // ❌ Método no está en interfaz
    return clienteRepositorio.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
}

@Override
public ClienteDTO buscarPorIdDTO(Long id) {  // ✅ Está en interfaz
    Cliente cliente = buscarPorId(id);
    return convertirADTO(cliente);
}
```

**Después (ClienteServicioImpl.java):**
```java
@Override
public ClienteDTO buscarPorId(Long id) {  // ✅ Devuelve DTO
    Cliente cliente = obtenerEntidadPorId(id);
    return mapper.map(cliente, ClienteDTO.class);
}

// Método privado o protected
private Cliente obtenerEntidadPorId(Long id) {
    return clienteRepositorio.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
}
```

#### Paso 1.3: Actualizar controladores

**Antes (ClienteControlador.java:281-289):**
```java
@GetMapping("/{id}")
public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {  // ❌ Expone entidad
    Cliente cliente = clienteServicio.buscarPorId(id);  // ❌ Método no existe
    return ResponseEntity.ok(cliente);
}
```

**Después:**
```java
@GetMapping("/{id}")
public ResponseEntity<ClienteDTO> obtenerClientePorId(@PathVariable Long id) {  // ✅ Devuelve DTO
    ClienteDTO cliente = clienteServicio.buscarPorId(id);
    return ResponseEntity.ok(cliente);
}
```

### Checklist de Implementación

- [ ] **Día 1-2:** Refactorizar `ClienteServicio`
  - [ ] Eliminar método `buscarPorIdDTO()`, `buscarPorRutDTO()`, `buscarPorEmailDTO()`
  - [ ] Renombrar métodos principales para que devuelvan DTOs
  - [ ] Hacer métodos de entidad privados o en interfaz interna
  - [ ] Actualizar implementación `ClienteServicioImpl`

- [ ] **Día 2-3:** Actualizar controladores de Cliente
  - [ ] `ClienteControlador.java` - Cambiar todos los métodos para usar DTOs
  - [ ] `ClienteRestControlador.java` - Cambiar todos los métodos para usar DTOs

- [ ] **Día 3-4:** Refactorizar `ProductoServicio`
  - [ ] Aplicar mismos cambios que ClienteServicio
  - [ ] Actualizar `ProductoControlador.java`
  - [ ] Actualizar `ProductoRestControlador.java` (si existe)

- [ ] **Día 4-5:** Refactorizar `VentaServicio`
  - [ ] Aplicar mismos cambios
  - [ ] Actualizar `VentaControlador.java`
  - [ ] Actualizar `VentaRestControlador.java`

- [ ] **Día 5:** Testing y validación
  - [ ] Ejecutar suite de pruebas
  - [ ] Verificar que no haya métodos sin implementar
  - [ ] Verificar compilación exitosa

### Métricas de Éxito
- ✅ 0 métodos no implementados en interfaces
- ✅ 0 controladores que devuelven entidades
- ✅ 100% de métodos públicos en servicios devuelven DTOs
- ✅ Compilación exitosa

---

## TAREA 2: Mover Lógica de Negocio de Controladores a Servicios

### Problema Identificado
Los controladores tienen lógica de cálculo de estadísticas y reglas de negocio:
- Cálculo de totales de compras
- Cálculo de promedios
- Filtrado de datos
- Acceso directo a `SecurityContextHolder`

### Archivos Afectados
1. `controlador/ClienteControlador.java:153-173` (mostrarDetalleCliente)
2. `controlador/VentaControlador.java:153-173,505-523`
3. `controlador/VentaControlador.java:373-387` (asignación de vendedor)

### Solución Propuesta

#### Paso 2.1: Crear servicios especializados

**Nuevo archivo: `service/EstadisticasClienteServicio.java`**
```java
package informviva.gest.service;

import informviva.gest.dto.EstadisticasClienteDTO;
import java.util.List;

public interface EstadisticasClienteServicio {
    EstadisticasClienteDTO calcularEstadisticasCliente(Long clienteId);
    EstadisticasClienteDTO calcularEstadisticasVentas(List<VentaDTO> ventas);
}
```

**Nuevo archivo: `service/impl/EstadisticasClienteServicioImpl.java`**
```java
package informviva.gest.service.impl;

import informviva.gest.dto.EstadisticasClienteDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.service.EstadisticasClienteServicio;
import informviva.gest.service.VentaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadisticasClienteServicioImpl implements EstadisticasClienteServicio {

    private final VentaServicio ventaServicio;

    @Override
    public EstadisticasClienteDTO calcularEstadisticasCliente(Long clienteId) {
        List<VentaDTO> ventas = ventaServicio.buscarPorCliente(clienteId);
        return calcularEstadisticasVentas(ventas);
    }

    @Override
    public EstadisticasClienteDTO calcularEstadisticasVentas(List<VentaDTO> ventas) {
        if (ventas == null || ventas.isEmpty()) {
            return EstadisticasClienteDTO.vacio();
        }

        double totalCompras = ventas.stream()
            .mapToDouble(venta -> venta.getTotal() != null ? venta.getTotal() : 0.0)
            .sum();

        double promedioCompra = totalCompras / ventas.size();

        VentaDTO ultimaVenta = ventas.stream()
            .max((v1, v2) -> v1.getFecha().compareTo(v2.getFecha()))
            .orElse(null);

        return EstadisticasClienteDTO.builder()
            .totalCompras(BigDecimal.valueOf(totalCompras))
            .numeroCompras(ventas.size())
            .promedioCompra(BigDecimal.valueOf(promedioCompra))
            .ultimaVenta(ultimaVenta)
            .build();
    }
}
```

**Nuevo DTO: `dto/EstadisticasClienteDTO.java`**
```java
package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasClienteDTO {
    private BigDecimal totalCompras;
    private Integer numeroCompras;
    private BigDecimal promedioCompra;
    private VentaDTO ultimaVenta;

    public static EstadisticasClienteDTO vacio() {
        return EstadisticasClienteDTO.builder()
            .totalCompras(BigDecimal.ZERO)
            .numeroCompras(0)
            .promedioCompra(BigDecimal.ZERO)
            .build();
    }
}
```

#### Paso 2.2: Refactorizar controladores

**Antes (ClienteControlador.java:153-173):**
```java
@GetMapping("/{id}/detalle")
public String mostrarDetalleCliente(@PathVariable Long id, Model modelo) {
    Cliente cliente = clienteServicio.buscarPorId(id);
    List<Venta> ventasCliente = ventaServicio.buscarPorCliente(id);

    // ❌ LÓGICA DE NEGOCIO EN CONTROLADOR
    var totalCompras = ventasCliente.stream()
        .mapToDouble(venta -> venta.getTotal() != null ? venta.getTotal() : 0.0)
        .sum();

    var promedioCompra = totalCompras / ventasCliente.size();

    modelo.addAttribute("cliente", cliente);
    modelo.addAttribute("ventas", ventasCliente);
    modelo.addAttribute("totalCompras", totalCompras);
    modelo.addAttribute("promedioCompra", promedioCompra);

    return "clientes/detalle";
}
```

**Después (ClienteControlador.java):**
```java
private final EstadisticasClienteServicio estadisticasServicio;  // Inyección constructor

@GetMapping("/{id}/detalle")
public String mostrarDetalleCliente(@PathVariable Long id, Model modelo) {
    ClienteDTO cliente = clienteServicio.buscarPorId(id);
    EstadisticasClienteDTO estadisticas = estadisticasServicio.calcularEstadisticasCliente(id);

    modelo.addAttribute("cliente", cliente);
    modelo.addAttribute("estadisticas", estadisticas);

    return "clientes/detalle";
}
```

#### Paso 2.3: Crear servicio de seguridad para usuario actual

**Nuevo archivo: `service/UsuarioActualServicio.java`**
```java
package informviva.gest.service;

import informviva.gest.dto.UsuarioDTO;

public interface UsuarioActualServicio {
    UsuarioDTO obtenerUsuarioActual();
    Long obtenerIdUsuarioActual();
    boolean tieneRol(String rol);
}
```

**Implementación: `service/impl/UsuarioActualServicioImpl.java`**
```java
package informviva.gest.service.impl;

import informviva.gest.dto.UsuarioDTO;
import informviva.gest.model.Usuario;
import informviva.gest.service.UsuarioActualServicio;
import informviva.gest.service.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioActualServicioImpl implements UsuarioActualServicio {

    private final UsuarioServicio usuarioServicio;

    @Override
    public UsuarioDTO obtenerUsuarioActual() {
        String username = obtenerUsernameActual();
        return usuarioServicio.buscarPorUsername(username);
    }

    @Override
    public Long obtenerIdUsuarioActual() {
        return obtenerUsuarioActual().getId();
    }

    @Override
    public boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
            .anyMatch(ga -> ga.getAuthority().equals("ROLE_" + rol));
    }

    private String obtenerUsernameActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        return auth.getName();
    }
}
```

**Antes (VentaControlador.java:373-387):**
```java
@PostMapping("/guardar")
public String guardarVenta(@ModelAttribute VentaDTO ventaDTO, RedirectAttributes redirectAttributes) {
    // ❌ ACCESO DIRECTO A SECURITY EN CONTROLADOR
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    Usuario vendedor = usuarioServicio.buscarPorUsername(username);

    ventaDTO.setVendedorId(vendedor.getId());

    ventaServicio.guardar(ventaDTO);
    redirectAttributes.addFlashAttribute("success", "Venta guardada");
    return "redirect:/ventas/lista";
}
```

**Después (VentaControlador.java):**
```java
private final UsuarioActualServicio usuarioActualServicio;  // Inyección constructor

@PostMapping("/guardar")
public String guardarVenta(@ModelAttribute VentaDTO ventaDTO, RedirectAttributes redirectAttributes) {
    Long vendedorId = usuarioActualServicio.obtenerIdUsuarioActual();
    ventaDTO.setVendedorId(vendedorId);

    ventaServicio.guardar(ventaDTO);
    redirectAttributes.addFlashAttribute("success", "Venta guardada exitosamente");
    return "redirect:/ventas/lista";
}
```

### Checklist de Implementación

- [ ] **Día 6:** Crear servicio de estadísticas
  - [ ] Crear `EstadisticasClienteServicio` (interfaz)
  - [ ] Crear `EstadisticasClienteServicioImpl`
  - [ ] Crear `EstadisticasClienteDTO`

- [ ] **Día 7:** Refactorizar ClienteControlador
  - [ ] Inyectar `EstadisticasClienteServicio`
  - [ ] Reemplazar lógica de cálculo en `mostrarDetalleCliente`
  - [ ] Eliminar código de cálculo del controlador

- [ ] **Día 8:** Crear servicio de usuario actual
  - [ ] Crear `UsuarioActualServicio` (interfaz)
  - [ ] Crear `UsuarioActualServicioImpl`

- [ ] **Día 9:** Refactorizar VentaControlador
  - [ ] Inyectar `UsuarioActualServicio`
  - [ ] Eliminar acceso directo a `SecurityContextHolder`
  - [ ] Mover lógica de cálculo de estadísticas a servicio dedicado

- [ ] **Día 10:** Testing
  - [ ] Crear tests unitarios para `EstadisticasClienteServicio`
  - [ ] Crear tests unitarios para `UsuarioActualServicio`
  - [ ] Verificar controladores simplificados

### Métricas de Éxito
- ✅ 0 accesos a `SecurityContextHolder` en controladores
- ✅ 0 cálculos de estadísticas en controladores
- ✅ Todos los controladores con máximo 10 líneas por método
- ✅ Servicios con responsabilidad única

---

## TAREA 3: Cambiar Field Injection a Constructor Injection

### Problema Identificado
119 usos de `@Autowired` en field injection en 31 archivos:
- Dificulta testing (no se pueden inyectar mocks fácilmente)
- No es inmutable (pueden cambiar después de construcción)
- No es explícito sobre dependencias requeridas
- No permite detectar dependencias circulares en tiempo de compilación

### Archivos Afectados
**Principales:**
1. `service/impl/ImportacionServicioImpl.java:44-54` (7 dependencias)
2. `service/impl/ExportacionServicioImpl.java` (6 dependencias)
3. `service/impl/VentaServicioImpl.java` (4 dependencias)
4. +28 archivos más

### Solución Propuesta

#### Paso 3.1: Patrón a seguir

**Antes (ImportacionServicioImpl.java:44-54):**
```java
@Service
public class ImportacionServicioImpl implements ImportacionServicio {

    @Autowired  // ❌ Field injection
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ExcelReader excelReader;

    @Autowired
    private CsvReader csvReader;

    @Autowired
    private ImportacionHistorialRepositorio historialRepositorio;

    // ... métodos
}
```

**Después (ImportacionServicioImpl.java):**
```java
@Service
@RequiredArgsConstructor  // ✅ Lombok genera constructor
public class ImportacionServicioImpl implements ImportacionServicio {

    // ✅ Constructor injection (final + @RequiredArgsConstructor)
    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ExcelReader excelReader;
    private final CsvReader csvReader;
    private final ImportacionHistorialRepositorio historialRepositorio;

    // Constructor generado automáticamente por Lombok
    // public ImportacionServicioImpl(ClienteServicio clienteServicio, ...) {
    //     this.clienteServicio = clienteServicio;
    //     ...
    // }

    // ... métodos
}
```

**Alternativa sin Lombok (si no lo usas):**
```java
@Service
public class ImportacionServicioImpl implements ImportacionServicio {

    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    // ... otras dependencias

    // ✅ Constructor explícito
    public ImportacionServicioImpl(
            ClienteServicio clienteServicio,
            ProductoServicio productoServicio,
            UsuarioServicio usuarioServicio,
            BCryptPasswordEncoder passwordEncoder,
            ExcelReader excelReader,
            CsvReader csvReader,
            ImportacionHistorialRepositorio historialRepositorio) {
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
        this.excelReader = excelReader;
        this.csvReader = csvReader;
        this.historialRepositorio = historialRepositorio;
    }

    // ... métodos
}
```

### Script de Refactorización Automática

Crear archivo: `scripts/refactor_field_injection.sh`

```bash
#!/bin/bash

# Script para convertir @Autowired field injection a constructor injection

# Lista de archivos con field injection
FILES=(
    "src/main/java/informviva/gest/service/impl/ImportacionServicioImpl.java"
    "src/main/java/informviva/gest/service/impl/ExportacionServicioImpl.java"
    "src/main/java/informviva/gest/service/impl/VentaServicioImpl.java"
    # ... agregar otros 28 archivos
)

for file in "${FILES[@]}"; do
    echo "Procesando: $file"

    # 1. Agregar 'final' a los campos con @Autowired
    sed -i 's/@Autowired\s*private /private final /g' "$file"

    # 2. Remover líneas vacías resultantes
    sed -i '/^@Autowired$/d' "$file"

    # 3. Agregar @RequiredArgsConstructor si no existe
    if ! grep -q "@RequiredArgsConstructor" "$file"; then
        sed -i 's/@Service/@Service\n@RequiredArgsConstructor/g' "$file"
    fi

    # 4. Agregar import de Lombok si no existe
    if ! grep -q "import lombok.RequiredArgsConstructor" "$file"; then
        sed -i '1i import lombok.RequiredArgsConstructor;' "$file"
    fi
done

echo "Refactorización completada. Verificar compilación."
```

### Checklist de Implementación

- [ ] **Día 11:** Refactorizar servicios principales (10 archivos)
  - [ ] `ClienteServicioImpl.java`
  - [ ] `ProductoServicioImpl.java`
  - [ ] `VentaServicioImpl.java`
  - [ ] `ImportacionServicioImpl.java`
  - [ ] `ExportacionServicioImpl.java`
  - [ ] `ReporteServicioImpl.java`
  - [ ] Otros servicios grandes

- [ ] **Día 12:** Refactorizar controladores (10 archivos)
  - [ ] `ClienteControlador.java`
  - [ ] `ProductoControlador.java`
  - [ ] `VentaControlador.java`
  - [ ] `UsuarioControlador.java`
  - [ ] Controladores REST

- [ ] **Día 13:** Refactorizar servicios restantes (11 archivos)
  - [ ] Servicios de utilidad
  - [ ] Servicios de validación
  - [ ] Servicios de configuración

- [ ] **Día 14:** Verificación y testing
  - [ ] Ejecutar compilación completa
  - [ ] Verificar que no haya `@Autowired` en campos
  - [ ] Ejecutar suite de pruebas
  - [ ] Verificar que no haya dependencias circulares

### Verificación con Comando

```bash
# Buscar todos los @Autowired que quedan
grep -r "@Autowired" src/main/java --include="*.java" | grep -v "// @Autowired"

# Contar cuántos quedan
grep -r "@Autowired" src/main/java --include="*.java" | wc -l
```

### Métricas de Éxito
- ✅ 0 usos de `@Autowired` en fields
- ✅ 100% de servicios usando constructor injection
- ✅ Todos los campos de dependencias son `final`
- ✅ Compilación exitosa sin errores

---

## TAREA 4: Descomponer Métodos Largos

### Problema Identificado
Métodos con más de 30 líneas que hacen múltiples cosas:
- VentaControlador: métodos de 34-51 líneas
- ImportacionServicioImpl: métodos de 62 líneas
- ExportacionServicioImpl: múltiples métodos largos

### Archivos Afectados
1. `controlador/VentaControlador.java:315-349` (34 líneas - obtenerVentasFiltradas)
2. `controlador/VentaControlador.java:473-523` (51 líneas - cargarDatosListado)
3. `service/impl/ImportacionServicioImpl.java:57-119` (62 líneas - importarClientes)

### Solución Propuesta

#### Ejemplo 1: VentaControlador.obtenerVentasFiltradas

**Antes (VentaControlador.java:315-349):**
```java
private List<Venta> obtenerVentasFiltradas(LocalDate fechaInicio, LocalDate fechaFin,
                                           Long clienteId, Long vendedorId,
                                           String estado, String metodoPago) {
    // ❌ 34 líneas haciendo múltiples cosas
    List<Venta> ventas;

    if (fechaInicio != null && fechaFin != null) {
        ventas = ventaRepositorio.findByFechaBetween(
            fechaInicio.atStartOfDay(),
            fechaFin.atTime(23, 59, 59)
        );
    } else {
        ventas = ventaRepositorio.findAll();
    }

    if (clienteId != null) {
        ventas = ventas.stream()
            .filter(v -> v.getCliente().getId().equals(clienteId))
            .collect(Collectors.toList());
    }

    if (vendedorId != null) {
        ventas = ventas.stream()
            .filter(v -> v.getVendedor() != null && v.getVendedor().getId().equals(vendedorId))
            .collect(Collectors.toList());
    }

    if (estado != null && !estado.isEmpty()) {
        ventas = ventas.stream()
            .filter(v -> v.getEstado().equals(estado))
            .collect(Collectors.toList());
    }

    return ventas;
}
```

**Después (VentaControlador.java):**
```java
// ✅ Mover lógica al servicio con criterios
private List<VentaDTO> obtenerVentasFiltradas(VentaCriteriosBusquedaDTO criterios) {
    return ventaServicio.buscarPorCriterios(criterios);
}
```

**Nuevo DTO de criterios: `dto/VentaCriteriosBusquedaDTO.java`**
```java
package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaCriteriosBusquedaDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long clienteId;
    private Long vendedorId;
    private String estado;
    private String metodoPago;

    public boolean tieneFiltroFechas() {
        return fechaInicio != null && fechaFin != null;
    }

    public boolean tieneFiltroCliente() {
        return clienteId != null;
    }

    public boolean tieneFiltroVendedor() {
        return vendedorId != null;
    }

    public boolean tieneFiltroEstado() {
        return estado != null && !estado.trim().isEmpty();
    }
}
```

**Agregar a VentaServicio:**
```java
public interface VentaServicio {
    // ... métodos existentes

    List<VentaDTO> buscarPorCriterios(VentaCriteriosBusquedaDTO criterios);
}
```

**Implementación en VentaServicioImpl:**
```java
@Override
public List<VentaDTO> buscarPorCriterios(VentaCriteriosBusquedaDTO criterios) {
    List<Venta> ventas = aplicarFiltros(criterios);
    return ventas.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
}

private List<Venta> aplicarFiltros(VentaCriteriosBusquedaDTO criterios) {
    List<Venta> ventas = obtenerVentasBase(criterios);

    ventas = filtrarPorCliente(ventas, criterios);
    ventas = filtrarPorVendedor(ventas, criterios);
    ventas = filtrarPorEstado(ventas, criterios);

    return ventas;
}

private List<Venta> obtenerVentasBase(VentaCriteriosBusquedaDTO criterios) {
    if (criterios.tieneFiltroFechas()) {
        return ventaRepositorio.findByFechaBetween(
            criterios.getFechaInicio().atStartOfDay(),
            criterios.getFechaFin().atTime(23, 59, 59)
        );
    }
    return ventaRepositorio.findAll();
}

private List<Venta> filtrarPorCliente(List<Venta> ventas, VentaCriteriosBusquedaDTO criterios) {
    if (!criterios.tieneFiltroCliente()) {
        return ventas;
    }
    return ventas.stream()
        .filter(v -> v.getCliente().getId().equals(criterios.getClienteId()))
        .collect(Collectors.toList());
}

private List<Venta> filtrarPorVendedor(List<Venta> ventas, VentaCriteriosBusquedaDTO criterios) {
    if (!criterios.tieneFiltroVendedor()) {
        return ventas;
    }
    return ventas.stream()
        .filter(v -> v.getVendedor() != null &&
                    v.getVendedor().getId().equals(criterios.getVendedorId()))
        .collect(Collectors.toList());
}

private List<Venta> filtrarPorEstado(List<Venta> ventas, VentaCriteriosBusquedaDTO criterios) {
    if (!criterios.tieneFiltroEstado()) {
        return ventas;
    }
    return ventas.stream()
        .filter(v -> v.getEstado().equals(criterios.getEstado()))
        .collect(Collectors.toList());
}
```

#### Ejemplo 2: ImportacionServicioImpl.importarClientes

**Antes (ImportacionServicioImpl.java:57-119):**
```java
public ResultadoImportacion importarClientes(MultipartFile archivo) {
    // ❌ 62 líneas - hace validación, lectura, mapeo, guardado, manejo de errores
    ResultadoImportacion resultado = new ResultadoImportacion();

    try {
        // Validar archivo
        if (archivo.isEmpty()) {
            resultado.agregarError("Archivo vacío");
            return resultado;
        }

        // Leer datos
        List<Map<String, Object>> datos;
        if (esExcel(archivo)) {
            datos = excelReader.leer(archivo);
        } else {
            datos = csvReader.leer(archivo);
        }

        // Procesar cada fila
        for (int i = 0; i < datos.size(); i++) {
            try {
                Map<String, Object> fila = datos.get(i);

                // Validar fila
                if (!validarFila(fila)) {
                    resultado.agregarAdvertencia("Fila " + i + " inválida");
                    continue;
                }

                // Mapear a entidad
                Cliente cliente = mapearCliente(fila, i + 2);

                if (cliente != null) {
                    if (clienteServicio.existePorRut(cliente.getRut())) {
                        resultado.agregarAdvertencia("Cliente ya existe: " + cliente.getRut());
                    } else {
                        clienteServicio.guardar(cliente);
                        resultado.incrementarExitosos();
                    }
                }
            } catch (Exception e) {
                resultado.agregarError("Error en fila " + i + ": " + e.getMessage());
            }
        }

    } catch (Exception e) {
        resultado.agregarError("Error general: " + e.getMessage());
    }

    return resultado;
}
```

**Después (ImportacionServicioImpl.java):**
```java
@Override
public ResultadoImportacion importarClientes(MultipartFile archivo) {
    // ✅ Método de orquestación - solo 10 líneas
    ResultadoImportacion resultado = new ResultadoImportacion();

    if (!validarArchivo(archivo, resultado)) {
        return resultado;
    }

    List<Map<String, Object>> datos = leerArchivo(archivo);
    procesarDatos(datos, resultado, this::procesarCliente);

    return resultado;
}

// Métodos privados especializados
private boolean validarArchivo(MultipartFile archivo, ResultadoImportacion resultado) {
    if (archivo.isEmpty()) {
        resultado.agregarError("Archivo vacío");
        return false;
    }
    return true;
}

private List<Map<String, Object>> leerArchivo(MultipartFile archivo) {
    if (esExcel(archivo)) {
        return excelReader.leer(archivo);
    }
    return csvReader.leer(archivo);
}

private void procesarDatos(List<Map<String, Object>> datos,
                          ResultadoImportacion resultado,
                          BiConsumer<Map<String, Object>, ResultadoImportacion> procesador) {
    for (int i = 0; i < datos.size(); i++) {
        try {
            Map<String, Object> fila = datos.get(i);
            procesador.accept(fila, resultado);
        } catch (Exception e) {
            resultado.agregarError("Error en fila " + i + ": " + e.getMessage());
        }
    }
}

private void procesarCliente(Map<String, Object> fila, ResultadoImportacion resultado) {
    Cliente cliente = mapearCliente(fila);

    if (clienteServicio.existePorRut(cliente.getRut())) {
        resultado.agregarAdvertencia("Cliente ya existe: " + cliente.getRut());
        return;
    }

    clienteServicio.guardar(cliente);
    resultado.incrementarExitosos();
}
```

### Checklist de Implementación

- [ ] **Día 15:** Refactorizar VentaControlador
  - [ ] Crear `VentaCriteriosBusquedaDTO`
  - [ ] Mover `obtenerVentasFiltradas` al servicio
  - [ ] Descomponer `cargarDatosListado`

- [ ] **Día 16:** Refactorizar ImportacionServicioImpl
  - [ ] Descomponer `importarClientes` (62 líneas → 10 líneas)
  - [ ] Descomponer `importarProductos` (similar)
  - [ ] Crear métodos privados especializados

- [ ] **Día 17:** Refactorizar ExportacionServicioImpl
  - [ ] Descomponer métodos de exportación largos
  - [ ] Aplicar patrón similar a importación

- [ ] **Día 18:** Verificación final
  - [ ] Verificar que todos los métodos públicos tengan máx 20 líneas
  - [ ] Verificar que métodos privados tengan máx 15 líneas
  - [ ] Ejecutar suite de pruebas
  - [ ] Generar reporte de complejidad ciclomática

### Herramientas de Medición

```bash
# Instalar herramienta de análisis de complejidad
# PMD, Checkstyle, o SonarLint

# Comando para encontrar métodos largos
find src/main/java -name "*.java" -exec wc -l {} \; | sort -rn | head -20
```

### Métricas de Éxito
- ✅ 0 métodos con más de 30 líneas
- ✅ Complejidad ciclomática < 10 por método
- ✅ Máximo 3 niveles de indentación por método
- ✅ Todos los métodos tienen responsabilidad única

---

## RESUMEN DE LA FASE 1

### Duración Total: 18 días (aproximadamente 2.5 semanas)

### Resultado Esperado
Al finalizar la Fase 1, el proyecto deberá tener:

✅ **Arquitectura consistente:**
- Interfaces bien definidas (DTO vs Entity)
- Servicios con responsabilidad única
- Controladores ligeros (solo coordinación)

✅ **Código mantenible:**
- Constructor injection en todos los servicios
- Métodos cortos y con un solo propósito
- Lógica de negocio en servicios, no en controladores

✅ **Base sólida para siguiente fase:**
- Fácil agregar nuevas funcionalidades
- Fácil escribir tests unitarios
- Fácil detectar problemas de acoplamiento

### Siguiente Paso
Con estas bases sólidas, en la **FASE 2** abordaremos:
- Consolidar validaciones
- Refactorizar repositorios sobrecargados
- Crear patrones genéricos para importación/exportación
- Mover lógica de DTOs a servicios

---

## Notas Importantes

1. **Commits frecuentes:** Hacer commit después de cada tarea completada
2. **Tests antes de merge:** Ejecutar suite completa de pruebas
3. **Code review:** Revisar cada cambio antes de pasar a la siguiente tarea
4. **Rollback plan:** Tener estrategia de rollback por si algo falla
5. **Documentación:** Actualizar documentación técnica con cambios arquitectónicos

---

**Última actualización:** 2025-11-07
**Autor:** Análisis automatizado Claude Code
**Versión:** 1.0
