# 🔧 GUÍA DE SOLUCIÓN DE ERRORES DE COMPILACIÓN

**Fecha**: 2025-11-07
**Estado**: Análisis sin Maven (problemas de red)
**Errores reportados**: 100+

---

## ✅ YA CORREGIDOS

### 1. ValidadorRutUtil
**Problema**: 4 archivos usaban `ValidadorRutUtil` que está en backup
**Solución**: ✅ Consolidado en `ValidadorRutClase` con método estático `validar()`
**Commit**: 16f3716

---

## 🔍 ANÁLISIS DE POSIBLES ERRORES RESTANTES

Dado que no podemos compilar con Maven por problemas de red, aquí está la guía para identificar y solucionar errores cuando puedas compilar localmente.

###  PROBLEMA POTENCIAL 1: Imports Faltantes en Nuevas Clases

**Archivos afectados**: Servicios y controladores nuevos

**Cómo identificar**:
```bash
mvn compile 2>&1 | grep "cannot find symbol"
```

**Solución típica**:
- Agregar imports faltantes
- Verificar que las clases existen en el package correcto

**Ejemplo**:
```java
// Si falta:
import informviva.gest.service.InventarioServicio;
import informviva.gest.repository.MovimientoInventarioRepositorio;
```

---

### 🔴 PROBLEMA POTENCIAL 2: BaseServiceImpl - Métodos Abstractos

**Archivos afectados**:
- `InventarioServicioImpl.java`
- `ProveedorServicioImpl.java`
- `OrdenCompraServicioImpl.java`

**Problema**: Si `BaseServiceImpl` tiene métodos abstractos, las implementaciones deben proveerlos.

**Cómo verificar**:
```bash
# Ver qué métodos tiene BaseServiceImpl
cat src/main/java/informviva/gest/service/impl/BaseServiceImpl.java | grep "abstract"
```

**Solución**: Implementar métodos faltantes como:
```java
@Override
protected String getNombreEntidad() {
    return "MovimientoInventario"; // o "Proveedor", "OrdenCompra"
}
```

---

### 🟡 PROBLEMA POTENCIAL 3: ModelMapper Bean

**Archivos afectados**: Todos los servicios que usan `ModelMapper`

**Problema**: Si `ModelMapper` no está configurado como Bean

**Cómo identificar**:
```bash
mvn compile 2>&1 | grep "ModelMapper"
```

**Solución**:

**Opción A**: Verificar que existe configuración de ModelMapper
```bash
grep -r "@Bean.*ModelMapper" src/main/java
```

**Opción B**: Si no existe, crear configuración:
```java
// En una clase @Configuration
@Bean
public ModelMapper modelMapper() {
    return new ModelMapper();
}
```

---

### 🟠 PROBLEMA POTENCIAL 4: Enums en DTOs

**Archivos afectados**:
- `OrdenCompraDTO.java` (usa `EstadoOrden`)
- `MovimientoInventarioDTO.java` (usa `TipoMovimiento`)

**Problema**: Imports de enums que están dentro de clases

**Cómo identificar**:
```bash
mvn compile 2>&1 | grep "EstadoOrden\|TipoMovimiento"
```

**Solución**: Verificar imports correctos:
```java
// Correcto:
import informviva.gest.model.OrdenCompra.EstadoOrden;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;

// Incorrecto (si los enums son inner classes):
import informviva.gest.model.EstadoOrden; // ❌
```

---

### 🟢 PROBLEMA POTENCIAL 5: UsuarioServicio

**Archivos afectados**: Controladores que obtienen usuario actual

**Problema**: Si `UsuarioServicio` no tiene método para obtener usuario autenticado

**Cómo identificar**:
```bash
mvn compile 2>&1 | grep "obtenerUsuarioActual\|getCurrentUser"
```

**Solución**: Verificar que `UsuarioServicio` tiene el método necesario

Si no existe, hay 2 opciones:

**Opción A**: Usar `SecurityContextHolder` directamente en controladores:
```java
private Usuario obtenerUsuarioActual() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    return usuarioRepositorio.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
}
```

**Opción B**: Agregar método a `UsuarioServicio`:
```java
public interface UsuarioServicio {
    Usuario obtenerUsuarioActual();
}
```

---

### 🔵 PROBLEMA POTENCIAL 6: Paginación - Page vs Pageable

**Archivos afectados**: Repositorios y servicios

**Problema**: Confusión entre `Page<T>` y `Pageable`

**Imports correctos**:
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
```

**Uso correcto**:
```java
// En repositorio
Page<Entidad> findByNombre(String nombre, Pageable pageable);

// En servicio
public Page<DTO> buscar(String nombre, Pageable pageable) {
    return repository.findByNombre(nombre, pageable)
        .map(this::convertirADTO);
}
```

---

### 🟣 PROBLEMA POTENCIAL 7: ResponseEntity<byte[]>

**Archivos afectados**: Controladores con exportación Excel

**Problema**: Generación de archivos Excel

**Dependencias necesarias** en `pom.xml`:
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

**Importar correc tamente**:
```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
```

---

## 📋 CHECKLIST PASO A PASO

Cuando puedas compilar localmente, sigue estos pasos:

### Paso 1: Compilar y capturar errores
```bash
mvn clean compile 2>&1 | tee errores.log
```

### Paso 2: Clasificar errores
```bash
# Errores de imports
grep "cannot find symbol" errores.log > errores_imports.txt

# Errores de tipos
grep "incompatible types" errores.log > errores_tipos.txt

# Errores de métodos
grep "cannot find symbol.*method" errores.log > errores_metodos.txt
```

### Paso 3: Solucionar por prioridad

**PRIORIDAD ALTA**:
1. ✅ Errores en entidades (`model/`)
2. ✅ Errores en repositorios (`repository/`)
3. ⏳ Errores en servicios (`service/`)

**PRIORIDAD MEDIA**:
4. ⏳ Errores en DTOs
5. ⏳ Errores en controladores

**PRIORIDAD BAJA**:
6. ⏳ Warnings

### Paso 4: Compilar incrementalmente
```bash
# Solo entidades
mvn compile -pl :tu-modulo -am

# Solo servicios
mvn compile -Dmaven.test.skip=true
```

---

## 🛠️ HERRAMIENTAS ÚTILES

### Buscar clases faltantes
```bash
# Buscar todas las clases que faltan
grep -r "cannot find symbol: class" errores.log | \
    sed 's/.*class \([A-Z][a-zA-Z]*\).*/\1/' | \
    sort -u > clases_faltantes.txt
```

### Buscar imports duplicados
```bash
# Buscar imports duplicados
find src -name "*.java" -exec grep -l "import.*;" {} \; | \
    xargs -I {} sh -c 'echo "=== {} ===" && grep "^import " {} | sort | uniq -d'
```

### Buscar @Autowired vs Constructor Injection
```bash
# Archivos con @Autowired en campos (malo)
grep -r "@Autowired" src --include="*.java" | grep "private"

# Debería ser inyección por constructor (bueno)
```

---

## 🎯 ERRORES MÁS COMUNES Y SOLUCIONES RÁPIDAS

### Error: "package org.springframework.boot does not exist"
**Causa**: Maven no descargó dependencias
**Solución**:
```bash
mvn dependency:purge-local-repository
mvn clean install
```

### Error: "incompatible types: X cannot be converted to Y"
**Causa**: Problema de conversión DTO ↔ Entidad
**Solución**: Usar ModelMapper o métodos convertir
```java
return modelMapper.map(entidad, DTO.class);
```

### Error: "method X in class Y cannot be applied"
**Causa**: Firma de método incorrecta
**Solución**: Verificar parámetros del método

### Error: "@Transactional cannot be resolved"
**Causa**: Import incorrecto
**Solución**:
```java
// Correcto:
import org.springframework.transaction.annotation.Transactional;

// Incorrecto:
import jakarta.transaction.Transactional;
```

---

## 📊 MÉTRICAS ESPERADAS

Después de solucionar errores:

```bash
mvn compile 2>&1 | tail -20
```

Deberías ver:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX s
[INFO] Finished at: YYYY-MM-DD...
```

---

## 🚨 SI NADA FUNCIONA

### Plan B: Compilar módulo por módulo

1. **Comentar** nuevas clases temporalmente
2. Compilar código existente
3. **Descomentar** una clase a la vez
4. Compilar de nuevo
5. Repetir hasta identificar el problema exacto

### Plan C: Rollback selectivo

Si un módulo específico falla:
```bash
# Ver archivos del último commit
git diff HEAD~1 --name-only

# Revertir archivo específico
git checkout HEAD~1 -- path/to/problematic/file.java
```

---

## 📞 SIGUIENTES PASOS

1. ✅ Ejecutar Maven localmente
2. ⏳ Capturar errores en archivo
3. ⏳ Solucionar por prioridad
4. ⏳ Compilar incrementalmente
5. ⏳ Ejecutar tests cuando compile

---

## 💡 NOTAS IMPORTANTES

- ✅ `ValidadorRutUtil` ya está corregido (commit 16f3716)
- ⏳ Otros errores necesitan Maven para identificarse
- 🔒 **NO eliminar código sin entender el error**
- 📝 **Documentar** cada cambio que hagas
- 🧪 **Probar** después de cada fix

---

**Siguiente paso recomendado**: Ejecutar `mvn clean compile` localmente y compartir los errores específicos para soluciones targeted.
