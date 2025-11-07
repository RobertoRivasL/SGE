# ÍNDICE COMPLETO DE PROBLEMAS - SGE

**Fecha de análisis:** 2025-11-07
**Versión:** 1.0
**Estado del proyecto:** Requiere refactorización crítica

---

## RESUMEN EJECUTIVO

### Estadísticas del Proyecto
- **Total archivos Java:** 217
- **Controladores:** 29 clases (6,319 líneas)
- **DTOs:** 90+ clases
- **Servicios:** 60+ clases
- **Repositorios:** 12 interfaces
- **Archivos con problemas:** 100+ archivos

### Problemas Identificados por Severidad

| Severidad | Cantidad | Impacto |
|-----------|----------|---------|
| **CRÍTICA** | 16 categorías | Afecta arquitectura completa |
| **ALTA** | 15 categorías | Afecta mantenibilidad y escalabilidad |
| **MEDIA** | 10 categorías | Afecta calidad del código |
| **BAJA** | 5 categorías | Code smells menores |

### Violaciones SOLID Detectadas

| Principio | Violaciones | Archivos Afectados |
|-----------|-------------|-------------------|
| **SRP** (Single Responsibility) | 15+ | Controladores, DTOs, Entidades |
| **DIP** (Dependency Inversion) | 10+ | Todas las interfaces de servicio |
| **OCP** (Open/Closed) | 5+ | Servicios de importación |
| **ISP** (Interface Segregation) | 3+ | Repositorios |
| **LSP** (Liskov Substitution) | 2+ | Herencias |

---

## LISTADO COMPLETO DE ARCHIVOS CON PROBLEMAS

### CATEGORÍA 1: PROBLEMAS CRÍTICOS (Prioridad 1)

#### 1.1 Inconsistencia entre Interfaces y Uso
**Severidad:** CRÍTICA
**Impacto:** El código no compila o tiene comportamiento indefinido

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/service/ClienteServicio.java`
  - Líneas: 45, 89, 97, 105, 151
  - Problema: Define `buscarPorIdDTO()` pero controladores llaman a `buscarPorId()`

- [ ] `src/main/java/informviva/gest/service/ProductoServicio.java`
  - Problema: Similar a ClienteServicio

- [ ] `src/main/java/informviva/gest/service/VentaServicio.java`
  - Problema: Similar a ClienteServicio

- [ ] `src/main/java/informviva/gest/controlador/ClienteControlador.java`
  - Líneas: 116, 281, 289, 427-433
  - Problema: Devuelve entidad en lugar de DTO

- [ ] `src/main/java/informviva/gest/controlador/api/ClienteRestControlador.java`
  - Línea: 96
  - Problema: Usa `clienteServicio.buscarPorId(id)` que no existe en interfaz

#### 1.2 Lógica de Negocio en Controladores
**Severidad:** CRÍTICA
**Impacto:** Viola SRP, dificulta testing y reutilización

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/ClienteControlador.java`
  - Líneas: 153-173 (mostrarDetalleCliente)
  - Problema: Calcula estadísticas (totalCompras, promedioCompra)

- [ ] `src/main/java/informviva/gest/controlador/VentaControlador.java`
  - Líneas: 153-173 (cálculo de estadísticas)
  - Líneas: 505-523 (cálculo de totales)
  - Líneas: 373-387 (acceso a SecurityContextHolder)
  - Problema: Múltiples responsabilidades

- [ ] `src/main/java/informviva/gest/controlador/ProductoControlador.java`
  - Problema: Similar a VentaControlador

#### 1.3 Métodos Extremadamente Largos
**Severidad:** CRÍTICA
**Impacto:** Alta complejidad ciclomática, difícil mantenimiento

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/VentaControlador.java`
  - Líneas: 315-349 (34 líneas - obtenerVentasFiltradas)
  - Líneas: 438-471 (34 líneas - cargarDatosFormulario)
  - Líneas: 473-523 (51 líneas - cargarDatosListado)

- [ ] `src/main/java/informviva/gest/service/impl/ImportacionServicioImpl.java`
  - Líneas: 57-119 (62 líneas - importarClientes)
  - Líneas: 122-150 (28 líneas - importarProductos)

- [ ] `src/main/java/informviva/gest/service/impl/ExportacionServicioImpl.java`
  - Líneas: 100-150+ (múltiples métodos largos)

#### 1.4 Mapeo Entity-DTO Inconsistente
**Severidad:** CRÍTICA
**Impacto:** Duplicación de código, propenso a errores

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/dto/VentaDTO.java`
  - Líneas: 25-26 (duplicación de campos `fecha` y `fechaVenta`)
  - Líneas: 174-186 (sincronización manual en setters)
  - Líneas: 119-139 (método calcularTotales - lógica de negocio en DTO)

- [ ] `src/main/java/informviva/gest/controlador/api/ClienteRestControlador.java`
  - Línea: 78 (conversión manual `this::convertirADTO`)

- [ ] `src/main/java/informviva/gest/service/impl/ClienteImportacionServicioImpl.java`
  - Problema: Construcción manual de entidades sin mapper

---

### CATEGORÍA 2: PROBLEMAS DE ALTA PRIORIDAD

#### 2.1 Inyección de Dependencias Incorrecta (Field Injection)
**Severidad:** ALTA
**Impacto:** Dificulta testing, no es inmutable, oculta dependencias

**Estadísticas:**
- **119 usos de @Autowired** en field injection
- **31 archivos** afectados

**Archivos principales:**
- [ ] `src/main/java/informviva/gest/service/impl/ImportacionServicioImpl.java`
  - Líneas: 44-54 (7 dependencias con @Autowired)

- [ ] `src/main/java/informviva/gest/service/impl/ExportacionServicioImpl.java`
  - (6 dependencias con @Autowired)

- [ ] `src/main/java/informviva/gest/service/impl/VentaServicioImpl.java`
  - Líneas: 39-42 (4 dependencias)

- [ ] `src/main/java/informviva/gest/service/impl/ClienteServicioImpl.java`
  - Problema: Field injection

- [ ] **+27 archivos más** con mismo problema

#### 2.2 Repositorio Sobrecargado
**Severidad:** ALTA
**Impacto:** Viola SRP, difícil mantenimiento

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/repository/ClienteRepositorio.java`
  - **470 líneas** con **60+ métodos**
  - Líneas: 74-87 (Query duplicada)
  - Línea: 378-388 (Nombre de método ridículamente largo)
  - Línea: 433 (Dos métodos en una línea - error de formato)
  - Líneas: 286-305 (Proyección complicada innecesaria)
  - Líneas: 356-375 (Métodos que Spring resuelve automáticamente)

- [ ] `src/main/java/informviva/gest/repository/ProductoRepositorio.java`
  - Problema: Similar a ClienteRepositorio

- [ ] `src/main/java/informviva/gest/repository/VentaRepositorio.java`
  - Problema: Similar a ClienteRepositorio

#### 2.3 Acoplamiento Excesivo Entre Módulos
**Severidad:** ALTA
**Impacto:** Dificulta cambios, dependencias circulares

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/service/impl/VentaServicioImpl.java`
  - Líneas: 39-42
  - Problema: Depende de 4 repositorios directamente
  ```java
  private final VentaRepositorio ventaRepositorio;
  private final ClienteRepositorio clienteRepositorio;
  private final ProductoRepositorio productoRepositorio;
  private final RepositorioUsuario repositorioUsuario;
  ```

- [ ] `src/main/java/informviva/gest/controlador/api/ClienteRestControlador.java`
  - Líneas: 45-46
  - Problema: El controlador de clientes conoce sobre ventas
  ```java
  private final ClienteServicio clienteServicio;
  private final VentaServicio ventaServicio;
  ```

#### 2.4 Validación Duplicada en Múltiples Lugares
**Severidad:** ALTA
**Impacto:** Código duplicado, inconsistencias

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/ClienteControlador.java`
  - Líneas: 201-221
  - Problema: Validación manual con `resultado.rejectValue()`

- [ ] `src/main/java/informviva/gest/service/impl/ClienteServicioImpl.java`
  - Líneas: 344-370 (método validarClienteDTO)
  - Problema: Validación en servicio

- [ ] `src/main/java/informviva/gest/dto/ClienteDTO.java`
  - Problema: Anotaciones de validación (tercera capa de validación)

**Resultado:** La misma validación se repite en 3 lugares diferentes

#### 2.5 Código Duplicado en Importación (Patrón Copy-Paste)
**Severidad:** ALTA
**Impacto:** Mantenimiento duplicado, propenso a errores

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/service/impl/ImportacionServicioImpl.java`
  - Líneas: 57-119 (importarClientes - 62 líneas)
  - Líneas: 122-150 (importarProductos - código casi idéntico)
  - Problema: Código duplicado al 90%, debería usar patrón Template Method

---

### CATEGORÍA 3: PROBLEMAS DE MEDIA PRIORIDAD

#### 3.1 Métodos con Múltiples Responsabilidades
**Severidad:** MEDIA
**Impacto:** Viola SRP a nivel de método

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/model/Cliente.java`
  - Líneas: 147-157 (registrarCompra)
  - Problema: Hace 4 cosas: valida, suma, incrementa, actualiza fecha

  - Líneas: 139-145 (actualizarCiudad)
  - Problema: Actualiza dato + actualiza fecha de modificación

- [ ] `src/main/java/informviva/gest/model/Venta.java`
  - Problema: Métodos con lógica de negocio en entidad

- [ ] `src/main/java/informviva/gest/model/Producto.java`
  - Problema: Validaciones en setters

#### 3.2 DTOs con Excesivas Responsabilidades
**Severidad:** MEDIA
**Impacto:** DTOs con lógica que debería estar en servicios

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/dto/VentaDTO.java`
  - Líneas: 119-139 (calcularTotales - lógica de cálculo)
  - Líneas: 147-162 (agregarProducto, eliminarProducto)
  - Líneas: 201-217 (obtenerErroresValidacion)
  - Problema: DTO con lógica de negocio

- [ ] `src/main/java/informviva/gest/dto/ClienteDTO.java`
  - Líneas: 132-147 (getNombreCompleto, getIniciales)
  - Líneas: 173-185 (tieneContactoCompleto, tieneDireccion)
  - Líneas: 254-276 (formatearRut - método estático)
  - Problema: Métodos de utilidad en DTO

- [ ] `src/main/java/informviva/gest/dto/ProductoDTO.java`
  - Problema: Métodos de cálculo en DTO

#### 3.3 Try-Catch Excesivo en Controladores
**Severidad:** MEDIA
**Impacto:** Código verboso, debería usar GlobalExceptionHandler

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/ClienteControlador.java`
  - Líneas: 67-91 (listarClientes)
  - Problema: Try-catch para excepciones que GlobalExceptionHandler debería manejar

- [ ] `src/main/java/informviva/gest/controlador/VentaControlador.java`
  - Problema: Múltiples try-catch innecesarios

- [ ] **+6 controladores más** con mismo problema

#### 3.4 Métodos con Demasiados Parámetros
**Severidad:** MEDIA
**Impacto:** Code smell, debería usar objeto de criterios

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/VentaControlador.java`
  - Líneas: 315-317 (obtenerVentasFiltradas)
  ```java
  private List<Venta> obtenerVentasFiltradas(LocalDate fechaInicio, LocalDate fechaFin,
                                             Long clienteId, Long vendedorId,
                                             String estado, String metodoPago)
  ```
  - Problema: 6 parámetros, debería usar `VentaCriteriosBusquedaDTO`

- [ ] `src/main/java/informviva/gest/service/ReporteServicio.java`
  - Líneas: 62-63 (obtenerEstimaciones)
  - Problema: 4 parámetros primitivos

#### 3.5 Enumeraciones y Constantes Dispersas
**Severidad:** MEDIA
**Impacto:** Valores hardcoded, no usa Enums

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/util/VentaConstantes.java`
  - Líneas: 560-561 (obtenerEstadosVenta - lista hardcoded)

- [ ] `src/main/java/informviva/gest/controlador/ClienteControlador.java`
  - Líneas: 422-424 (obtenerCategoriasDisponibles - lista hardcoded)

- [ ] `src/main/java/informviva/gest/controlador/VentaControlador.java`
  - Líneas: 560-566 (estados hardcoded)

- [ ] `src/main/java/informviva/gest/model/Venta.java`
  - Problema: EstadoVenta debería ser Enum, no String

#### 3.6 Métodos API REST Inconsistentes
**Severidad:** MEDIA
**Impacto:** API inconsistente, mezcla DTOs y entidades

**Archivos afectados:**
- [ ] `src/main/java/informviva/gest/controlador/api/ClienteRestControlador.java`
  - Línea: 76 (Retorna entidad en lugar de DTO)
  - Línea: 96 (`Cliente cliente = clienteServicio.buscarPorId(id)`)
  - Líneas: 322-348 (validarRut - expone lógica de validación)
  - Líneas: 354-379 (validarEmail - misma información duplicada)

---

### CATEGORÍA 4: PROBLEMAS DE BAJA PRIORIDAD

#### 4.1 System.out.println en Lugar de Logging
**Severidad:** BAJA
**Impacto:** No hay logs estructurados

**Estadísticas:** 5 ocurrencias encontradas

**Archivos afectados:**
- [ ] Servicios de exportación
- [ ] Servicios de importación
- [ ] Servicios de procesamiento

**Solución:** Usar SLF4J Logger

#### 4.2 Naming Inconsistente
**Severidad:** BAJA
**Impacto:** Confusión en nomenclatura

**Archivos afectados:**
- [ ] Repositorios (métodos muy largos)
- [ ] Servicios (buscarPorId vs obtenerPorId)

---

## RESUMEN POR TIPO DE ARCHIVO

### Controladores (29 archivos - 6 con problemas graves)
1. ✅ ClienteControlador.java - CRÍTICO
2. ✅ VentaControlador.java - CRÍTICO
3. ✅ ProductoControlador.java - ALTA
4. ✅ ClienteRestControlador.java - ALTA
5. ✅ VentaRestControlador.java - MEDIA
6. ✅ ExportacionControlador.java - MEDIA
7. ImportacionControlador.java - MEDIA
8. +22 controladores con problemas menores

### Servicios (60+ archivos - 10 con problemas graves)
1. ✅ ClienteServicio.java (interfaz) - CRÍTICO
2. ✅ ClienteServicioImpl.java - CRÍTICO
3. ✅ VentaServicio.java (interfaz) - CRÍTICO
4. ✅ VentaServicioImpl.java - CRÍTICO
5. ✅ ProductoServicio.java - ALTA
6. ✅ ProductoServicioImpl.java - ALTA
7. ✅ ImportacionServicioImpl.java - ALTA
8. ✅ ExportacionServicioImpl.java - ALTA
9. ✅ ReporteServicioImpl.java - MEDIA
10. BaseServiceImpl.java - MEDIA
11. +50 servicios con problemas menores

### DTOs (90+ archivos - 3 con problemas graves)
1. ✅ VentaDTO.java - CRÍTICO
2. ✅ ClienteDTO.java - ALTA
3. ✅ ProductoDTO.java - MEDIA
4. +87 DTOs con estructura correcta

### Repositorios (12 archivos - 3 con problemas graves)
1. ✅ ClienteRepositorio.java - CRÍTICO (470 líneas)
2. ✅ ProductoRepositorio.java - ALTA
3. ✅ VentaRepositorio.java - ALTA
4. +9 repositorios con problemas menores

### Entidades (16 archivos - 3 con problemas)
1. ✅ Cliente.java - MEDIA (lógica de negocio)
2. ✅ Venta.java - MEDIA (enums como String)
3. ✅ Producto.java - MEDIA (validaciones en setters)
4. +13 entidades correctas

---

## PLAN DE REFACTORIZACIÓN

### Documentos de Planificación
1. **FASE_1_CRITICA.md** - Problemas críticos (Semana 1-2)
   - Tarea 1: Separar interfaces (DTO vs Entity)
   - Tarea 2: Mover lógica de controladores
   - Tarea 3: Constructor injection
   - Tarea 4: Descomponer métodos largos

2. **FASE_2_ALTA.md** - Problemas de alta prioridad (Semana 3-4)
   - Tarea 1: Consolidar validación
   - Tarea 2: Refactorizar repositorios
   - Tarea 3: Patrón genérico importación
   - Tarea 4: Limpiar lógica de DTOs

3. **FASE_3_MEDIA.md** - Problemas de media prioridad (Semana 5-6)
   - (Pendiente de crear)

---

## MÉTRICAS OBJETIVO

### Estado Actual vs Objetivo

| Métrica | Actual | Objetivo | Gap |
|---------|--------|----------|-----|
| Métodos > 30 líneas | 12+ | 0 | -12 |
| Field injection | 119 | 0 | -119 |
| Queries duplicadas | 10+ | 0 | -10 |
| Líneas en repositorio más grande | 470 | <150 | -320 |
| Validaciones duplicadas | 3 capas | 2 capas | -1 |
| DTOs con lógica | 3+ | 0 | -3 |
| Controladores con lógica negocio | 6+ | 0 | -6 |

---

## PRIORIZACIÓN RECOMENDADA

### Semana 1-2: CRÍTICA
- Arreglar interfaces inconsistentes
- Mover lógica de controladores
- Constructor injection
- Descomponer métodos largos

### Semana 3-4: ALTA
- Consolidar validaciones
- Refactorizar ClienteRepositorio
- Patrón Template para importación
- Limpiar DTOs

### Semana 5-6: MEDIA
- GlobalExceptionHandler
- Enums en lugar de Strings
- Limpiar entidades
- Object Parameter Pattern

---

**NOTA IMPORTANTE:** Este índice debe actualizarse a medida que se resuelven los problemas.
Marcar con ✅ los archivos que ya fueron refactorizados.

---

**Última actualización:** 2025-11-07
**Autor:** Análisis automatizado Claude Code
**Próxima revisión:** Después de completar FASE 1
