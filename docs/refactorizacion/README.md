# Plan de Refactorización - Sistema de Gestión Empresarial (SGE)

## Descripción General

Este directorio contiene la documentación completa del análisis de código y el plan de refactorización para el proyecto SGE.

El análisis identificó **problemas graves de arquitectura, diseño y violaciones de principios SOLID** que requieren refactorización sistemática.

---

## Estructura de Documentación

### 📋 00_INDICE_PROBLEMAS.md
**Documento principal:** Lista completa de todos los problemas encontrados

**Contenido:**
- Resumen ejecutivo con estadísticas
- Violaciones SOLID identificadas
- Listado de 100+ archivos con problemas organizados por severidad
- Métricas objetivo
- Priorización de tareas

**Usa este documento para:**
- Ver panorama completo de problemas
- Identificar qué archivos necesitan refactorización
- Seguir progreso (marcar con ✅ archivos corregidos)

---

### 🔴 FASE_1_CRITICA.md
**Duración:** Semana 1-2 (18 días)
**Prioridad:** CRÍTICA - Debe completarse primero

**Tareas:**
1. **Separar Interfaces:** Métodos DTO vs Entity
   - Problema: Controladores usan métodos que no existen en interfaces
   - Archivos: ClienteServicio, ProductoServicio, VentaServicio
   - Impacto: El código actual tiene errores arquitectónicos graves

2. **Mover Lógica de Negocio:**
   - Problema: Controladores calculan estadísticas y hacen lógica de negocio
   - Crear: EstadisticasClienteServicio, UsuarioActualServicio
   - Impacto: Viola SRP, dificulta testing

3. **Constructor Injection:**
   - Problema: 119 usos de @Autowired field injection
   - Cambiar: A constructor injection con @RequiredArgsConstructor
   - Impacto: Dificulta testing, no es inmutable

4. **Descomponer Métodos Largos:**
   - Problema: Métodos de 30-62 líneas
   - Crear: Métodos especializados, DTOs de criterios
   - Impacto: Alta complejidad ciclomática

**Entregables:**
- Interfaces consistentes
- Controladores ligeros (solo orquestación)
- Constructor injection en todos los servicios
- Métodos < 20 líneas

---

### 🟠 FASE_2_ALTA.md
**Duración:** Semana 3-4 (18 días)
**Prioridad:** ALTA - Completar después de Fase 1

**Tareas:**
1. **Consolidar Validación:**
   - Problema: Validación duplicada en 3 lugares (Controlador, Servicio, DTO)
   - Crear: Validadores de negocio (ClienteNegocioValidador)
   - Estrategia: DTOs (sintáctica) + Servicios (negocio)

2. **Refactorizar Repositorios:**
   - Problema: ClienteRepositorio tiene 470 líneas con 60+ métodos
   - Crear: Specifications para búsquedas complejas
   - Reducir: A máximo 20 métodos esenciales

3. **Patrón Genérico Importación:**
   - Problema: Código duplicado al 90% entre importarClientes e importarProductos
   - Crear: Template Method pattern (ImportacionTemplate)
   - Resultado: 62 líneas → 1 línea por método

4. **Limpiar DTOs:**
   - Problema: DTOs con lógica de negocio (calcularTotales, formatearRut)
   - Mover: A servicios dedicados (VentaCalculoServicio, RutFormateador)
   - Resultado: DTOs como POJOs puros

**Entregables:**
- Validación en 2 capas (DTO + Servicio)
- Repositorios < 150 líneas
- Patrón reutilizable para importación
- DTOs sin lógica

---

### 🟡 FASE_3_MEDIA.md
**Duración:** Semana 5-6
**Prioridad:** MEDIA - Mejoras de calidad
**Estado:** Pendiente de crear

**Tareas previstas:**
- Try-catch excesivo → GlobalExceptionHandler
- Limpiar lógica de entidades
- Usar Enums en lugar de Strings
- Object Parameter Pattern para métodos con muchos parámetros

---

## Cómo Usar Esta Documentación

### Para Desarrolladores

#### 1. Antes de Empezar la Refactorización
```bash
# 1. Leer el índice completo
cat docs/refactorizacion/00_INDICE_PROBLEMAS.md

# 2. Leer la fase actual
cat docs/refactorizacion/FASE_1_CRITICA.md

# 3. Crear branch de trabajo
git checkout -b refactor/fase-1
```

#### 2. Durante la Refactorización
- Seguir las tareas en orden (son interdependientes)
- Marcar checkboxes en los archivos .md al completar
- Hacer commit después de cada tarea completada
- Ejecutar tests después de cada cambio

#### 3. Al Completar una Fase
```bash
# 1. Verificar todas las métricas de éxito
# 2. Ejecutar suite completa de tests
mvn clean test

# 3. Verificar compilación
mvn clean compile

# 4. Actualizar índice (marcar archivos con ✅)
# 5. Hacer merge a main
git checkout main
git merge refactor/fase-1

# 6. Tag de versión
git tag -a v1.1-fase1-completa -m "Completada Fase 1 de refactorización"
```

### Para Project Managers

#### Seguimiento de Progreso
- Revisar checkboxes en cada documento de fase
- Verificar métricas de éxito al final de cada fase
- Actualizar 00_INDICE_PROBLEMAS.md con ✅ en archivos corregidos

#### Estimación de Tiempo
- **Fase 1:** 18 días (crítica)
- **Fase 2:** 18 días (alta)
- **Fase 3:** 12 días (media)
- **Total:** ~10 semanas (2.5 meses)

---

## Herramientas Recomendadas

### Análisis de Código
```bash
# Buscar @Autowired restantes
grep -r "@Autowired" src/main/java --include="*.java"

# Encontrar métodos largos
# (usar herramientas como SonarLint, PMD, Checkstyle)
```

### Testing
```bash
# Ejecutar tests
mvn test

# Cobertura de código
mvn jacoco:report

# Verificar compilación
mvn clean compile
```

---

## Principios de Refactorización

### ✅ Hacer
- Seguir el orden de las fases (son interdependientes)
- Hacer commits frecuentes (después de cada tarea)
- Ejecutar tests después de cada cambio
- Actualizar documentación al completar tareas
- Pedir code review antes de merge

### ❌ No Hacer
- Saltarse fases (las tareas tienen dependencias)
- Hacer cambios sin tests
- Hacer cambios masivos sin commits intermedios
- Modificar múltiples categorías de problemas a la vez
- Ignorar las métricas de éxito

---

## Contacto y Soporte

Si encuentras problemas durante la refactorización:

1. **Revisar la documentación** de la fase actual
2. **Verificar el índice** de problemas
3. **Consultar con el equipo** si hay dudas arquitectónicas
4. **Documentar** cualquier problema nuevo encontrado

---

## Historial de Cambios

| Fecha | Versión | Cambios |
|-------|---------|---------|
| 2025-11-07 | 1.0 | Análisis inicial y creación de documentación |
| | | - 00_INDICE_PROBLEMAS.md creado |
| | | - FASE_1_CRITICA.md completado |
| | | - FASE_2_ALTA.md completado |

---

## Próximos Pasos

### Inmediato (Hoy)
1. ✅ Revisar documentación completa
2. ✅ Entender problemas identificados
3. [ ] Decidir cuándo empezar Fase 1
4. [ ] Asignar recursos al proyecto de refactorización

### Corto Plazo (Esta Semana)
1. [ ] Crear branch `refactor/fase-1`
2. [ ] Empezar Tarea 1: Separar interfaces
3. [ ] Setup de herramientas de análisis (SonarLint, etc.)

### Mediano Plazo (Este Mes)
1. [ ] Completar Fase 1
2. [ ] Iniciar Fase 2

---

**Última actualización:** 2025-11-07
**Autor:** Análisis automatizado Claude Code
**Estado:** Documentación completa - Listo para comenzar refactorización
