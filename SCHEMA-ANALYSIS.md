# Análisis del Schema SQL - Sistema de Gestión Empresarial

## Resumen Ejecutivo

Se han analizado **18 archivos Java** del modelo de entidades JPA y se ha generado un schema SQL completo para MySQL 8.0.

- **Entidades JPA analizadas**: 16
- **Tablas principales generadas**: 16
- **Tablas auxiliares (@ElementCollection)**: 2
- **Total de tablas en el schema**: 18

---

## Archivos Analizados

### Entidades JPA (Con @Entity)
1. `Categoria.java` → Tabla: `categorias`
2. `Cliente.java` → Tabla: `clientes`
3. `ConfiguracionSistema.java` → Tabla: `configuracion_sistema`
4. `DetalleOrdenCompra.java` → Tabla: `detalles_orden_compra`
5. `ExportacionHistorial.java` → Tabla: `exportacion_historial`
6. `ImportacionHistorial.java` → Tabla: `importacion_historial`
7. `MovimientoInventario.java` → Tabla: `movimientos_inventario`
8. `OrdenCompra.java` → Tabla: `ordenes_compra`
9. `Producto.java` → Tabla: `productos`
10. `Proveedor.java` → Tabla: `proveedores`
11. `Rol.java` → Tabla: `roles` + `rol_permisos`
12. `RolVista.java` → Tabla: `rol_vistas`
13. `Usuario.java` → Tabla: `usuarios` + `usuario_roles`
14. `Venta.java` → Tabla: `ventas`
15. `VentaDetalle.java` → Tabla: `venta_detalle`

### Clases NO Persistidas (Sin @Entity)
- `EstadisticasNotificaciones.java` - Clase POJO para estadísticas
- `Notificacion.java` - Clase POJO (no persiste en BD)

### Enums
- `TipoNotificacion.java` - Enum (no genera tabla)
- `MovimientoInventario.TipoMovimiento` - Enum interno (VARCHAR en BD)
- `OrdenCompra.EstadoOrden` - Enum interno (VARCHAR en BD)
- `Venta.EstadoVenta` - Enum interno (VARCHAR en BD)
- `Venta.MetodoPago` - Enum interno (VARCHAR en BD)

---

## Estructura de Tablas

### 1. Tablas sin Dependencias (Entidades Base)

#### `categorias`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- nombre: VARCHAR(255) UNIQUE NOT NULL
- descripcion: VARCHAR(255)
- activa: TINYINT(1) DEFAULT 1
```

#### `configuracion_sistema`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- nombre_empresa: VARCHAR(100) NOT NULL
- smtp_host, smtp_port, smtp_usuario, smtp_password
- dias_inactividad_alerta: INT DEFAULT 30
- habilitar_notificaciones: TINYINT(1) DEFAULT 1
```

#### `exportacion_historial`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- tipo_exportacion: VARCHAR(255) NOT NULL
- formato: VARCHAR(255) NOT NULL
- usuario_solicitante: VARCHAR(255) NOT NULL
- fecha_solicitud: DATETIME NOT NULL
- estado: VARCHAR(30) DEFAULT 'Procesando'
```

#### `rol_vistas`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- rol_nombre: VARCHAR(255)
- usuario_id: BIGINT
- username: VARCHAR(255)
- fecha_vista: TIMESTAMP
```

#### `roles`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- nombre: VARCHAR(50) UNIQUE NOT NULL
- descripcion: VARCHAR(200)

Tabla auxiliar:
- rol_permisos (rol_id, permiso)
```

#### `usuarios`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- username: VARCHAR(255) UNIQUE NOT NULL
- password: VARCHAR(255) NOT NULL
- nombre: VARCHAR(255) NOT NULL
- apellido: VARCHAR(255) NOT NULL
- email: VARCHAR(255) UNIQUE NOT NULL
- activo: TINYINT(1) DEFAULT 1
- fecha_creacion: DATETIME
- ultimo_acceso: DATETIME

Tabla auxiliar:
- usuario_roles (usuario_id, rol)
```

#### `clientes`
```sql
Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- nombre: VARCHAR(50) NOT NULL
- apellido: VARCHAR(50) NOT NULL
- email: VARCHAR(255) UNIQUE NOT NULL
- rut: VARCHAR(255) NOT NULL
- fecha_registro: DATETIME NOT NULL
- total_compras: DECIMAL(19,2)
- numero_compras: INT DEFAULT 0
- activo: TINYINT(1) DEFAULT 1

Índices:
- uk_cliente_email (UNIQUE)
- idx_cliente_rut
- idx_cliente_nombre
- idx_cliente_activo
```

---

### 2. Tablas con Dependencias Simples

#### `productos`
```sql
Dependencias: categorias

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- codigo: VARCHAR(255) UNIQUE NOT NULL
- nombre: VARCHAR(255) NOT NULL
- precio: DOUBLE NOT NULL
- stock: INT DEFAULT 0
- stock_minimo: INT
- categoria_id (FK): BIGINT → categorias(id)

Foreign Keys:
- fk_producto_categoria → categorias(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE
```

#### `proveedores`
```sql
Dependencias: usuarios (auditoría)

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- rut: VARCHAR(12) UNIQUE NOT NULL
- nombre: VARCHAR(200) NOT NULL
- telefono, email, direccion, ciudad, region
- activo: TINYINT(1) DEFAULT 1
- usuario_creacion_id (FK): BIGINT → usuarios(id)
- usuario_actualizacion_id (FK): BIGINT → usuarios(id)

Foreign Keys:
- fk_proveedor_usuario_creacion → usuarios(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE
```

#### `importacion_historial`
```sql
Dependencias: usuarios

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- tipo_importacion: VARCHAR(255) NOT NULL
- nombre_archivo: VARCHAR(255) NOT NULL
- fecha_importacion: DATETIME NOT NULL
- usuario_id (FK): BIGINT → usuarios(id)
- total_registros, registros_exitosos, registros_con_error: INT
- exitoso: TINYINT(1)

Foreign Keys:
- fk_importacion_usuario → usuarios(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE
```

#### `movimientos_inventario`
```sql
Dependencias: productos, usuarios

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- producto_id (FK): BIGINT → productos(id)
- tipo: VARCHAR(30) NOT NULL (ENUM)
- cantidad: INT NOT NULL
- stock_anterior: INT NOT NULL
- stock_nuevo: INT NOT NULL
- usuario_id (FK): BIGINT → usuarios(id)
- fecha: DATETIME NOT NULL
- costo_unitario: DECIMAL(10,2)
- costo_total: DECIMAL(10,2)

Enum TipoMovimiento:
- COMPRA, DEVOLUCION_ENTRADA, VENTA, DEVOLUCION_SALIDA
- AJUSTE_POSITIVO, AJUSTE_NEGATIVO
- TRANSFERENCIA_ENTRADA, TRANSFERENCIA_SALIDA
- INVENTARIO_INICIAL

Índices:
- idx_producto, idx_tipo, idx_fecha, idx_usuario

Foreign Keys:
- fk_movimiento_producto → productos(id)
  ON DELETE CASCADE
- fk_movimiento_usuario → usuarios(id)
  ON DELETE CASCADE
```

---

### 3. Tablas con Dependencias Complejas

#### `ventas`
```sql
Dependencias: clientes, usuarios

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- fecha: DATETIME NOT NULL
- cliente_id (FK): BIGINT → clientes(id)
- vendedor_id (FK): BIGINT → usuarios(id)
- subtotal: DOUBLE
- impuesto: DOUBLE
- total: DOUBLE NOT NULL
- metodo_pago: VARCHAR(20) (ENUM)
- estado: VARCHAR(25) DEFAULT 'PENDIENTE' (ENUM)
- numero_factura: VARCHAR(50) UNIQUE
- descuento: DOUBLE DEFAULT 0.0

Enum EstadoVenta:
- PENDIENTE, EN_PROCESO, COMPLETADA, ANULADA, PARCIALMENTE_PAGADA

Enum MetodoPago:
- EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO
- TRANSFERENCIA, CHEQUE, CREDITO

Índices:
- idx_venta_fecha, idx_venta_cliente
- idx_venta_vendedor, idx_venta_estado

Foreign Keys:
- fk_venta_cliente → clientes(id)
  ON DELETE RESTRICT
- fk_venta_vendedor → usuarios(id)
  ON DELETE SET NULL
```

#### `venta_detalle`
```sql
Dependencias: ventas, productos

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- venta_id (FK): BIGINT → ventas(id)
- producto_id (FK): BIGINT → productos(id)
- cantidad: INT NOT NULL
- precio_unitario: DOUBLE NOT NULL

Foreign Keys:
- fk_venta_detalle_venta → ventas(id)
  ON DELETE CASCADE
- fk_venta_detalle_producto → productos(id)
  ON DELETE RESTRICT
```

#### `ordenes_compra`
```sql
Dependencias: proveedores, usuarios (múltiples)

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- numero_orden: VARCHAR(50) UNIQUE NOT NULL
- proveedor_id (FK): BIGINT → proveedores(id)
- fecha_orden: DATE NOT NULL
- estado: VARCHAR(30) DEFAULT 'PENDIENTE' (ENUM)
- usuario_comprador_id (FK): BIGINT → usuarios(id)
- usuario_aprobador_id (FK): BIGINT → usuarios(id)
- usuario_receptor_id (FK): BIGINT → usuarios(id)
- subtotal: DECIMAL(12,2) DEFAULT 0.00
- porcentaje_impuesto: DECIMAL(5,2) DEFAULT 19.00
- total: DECIMAL(12,2) DEFAULT 0.00

Enum EstadoOrden:
- BORRADOR, PENDIENTE, ENVIADA, CONFIRMADA, EN_TRANSITO
- RECIBIDA_PARCIAL, RECIBIDA_COMPLETA, COMPLETADA, CANCELADA

Índices:
- uk_orden_numero (UNIQUE)
- idx_proveedor, idx_estado, idx_fecha_orden
- idx_usuario_comprador

Foreign Keys:
- fk_orden_proveedor → proveedores(id)
  ON DELETE RESTRICT
- fk_orden_usuario_comprador → usuarios(id)
  ON DELETE RESTRICT
- fk_orden_usuario_aprobador → usuarios(id)
  ON DELETE SET NULL
- fk_orden_usuario_receptor → usuarios(id)
  ON DELETE SET NULL
```

#### `detalles_orden_compra`
```sql
Dependencias: ordenes_compra, productos

Columnas principales:
- id (PK): BIGINT AUTO_INCREMENT
- orden_compra_id (FK): BIGINT → ordenes_compra(id)
- producto_id (FK): BIGINT → productos(id)
- cantidad: INT NOT NULL
- precio_unitario: DECIMAL(10,2) NOT NULL
- porcentaje_descuento: DECIMAL(5,2) DEFAULT 0.00
- monto_descuento: DECIMAL(10,2) DEFAULT 0.00
- subtotal: DECIMAL(12,2) NOT NULL
- cantidad_recibida: INT DEFAULT 0

Índices:
- idx_orden_compra, idx_producto_detalle

Foreign Keys:
- fk_detalle_orden_compra → ordenes_compra(id)
  ON DELETE CASCADE
- fk_detalle_producto → productos(id)
  ON DELETE RESTRICT
```

---

## Mapeo de Tipos de Datos

### JPA → MySQL

| Tipo JPA           | Tipo MySQL           | Notas                                |
|--------------------|----------------------|--------------------------------------|
| Long               | BIGINT               | Para IDs y números grandes           |
| Integer            | INT                  | Para cantidades y contadores         |
| String (default)   | VARCHAR(255)         | Longitud por defecto                 |
| String (@Column)   | VARCHAR(length)      | Según especificación                 |
| String (TEXT)      | TEXT                 | Para textos largos                   |
| LocalDateTime      | DATETIME             | Fecha y hora                         |
| LocalDate          | DATE                 | Solo fecha                           |
| Date (java.util)   | TIMESTAMP            | Compatibilidad legacy                |
| Boolean/boolean    | TINYINT(1)           | 0=false, 1=true                      |
| Double             | DOUBLE               | Números decimales (legacy)           |
| BigDecimal         | DECIMAL(p,s)         | Precisión monetaria (recomendado)    |
| Enum (@Enumerated) | VARCHAR              | Nombre del enum                      |

### Precisión de Decimales

```sql
Precios y montos pequeños: DECIMAL(10,2)   -- hasta 99,999,999.99
Totales y montos grandes:  DECIMAL(12,2)   -- hasta 9,999,999,999.99
Porcentajes:               DECIMAL(5,2)    -- hasta 999.99%
Cliente.totalCompras:      DECIMAL(19,2)   -- gran precisión
```

---

## Relaciones entre Tablas

### One-to-Many / Many-to-One

```
categorias (1) ←→ (N) productos
usuarios (1) ←→ (N) ventas (vendedor)
clientes (1) ←→ (N) ventas
ventas (1) ←→ (N) venta_detalle
productos (1) ←→ (N) venta_detalle
usuarios (1) ←→ (N) movimientos_inventario
productos (1) ←→ (N) movimientos_inventario
proveedores (1) ←→ (N) ordenes_compra
usuarios (1) ←→ (N) ordenes_compra (comprador/aprobador/receptor)
ordenes_compra (1) ←→ (N) detalles_orden_compra
productos (1) ←→ (N) detalles_orden_compra
usuarios (1) ←→ (N) importacion_historial
usuarios (1) ←→ (N) proveedores (creador/actualizador)
```

### Many-to-Many (mediante @ElementCollection)

```
usuarios (N) ←→ (N) roles
  → Tabla intermedia: usuario_roles

roles (N) ←→ (N) permisos
  → Tabla intermedia: rol_permisos
```

---

## Políticas de Integridad Referencial

### ON DELETE CASCADE
Elimina registros dependientes automáticamente:
```sql
- venta_detalle → ventas
- detalles_orden_compra → ordenes_compra
- movimientos_inventario → productos
- importacion_historial → usuarios
- usuario_roles → usuarios
- rol_permisos → roles
```

### ON DELETE RESTRICT
Previene eliminación si existen referencias:
```sql
- ventas → clientes
- venta_detalle → productos
- ordenes_compra → proveedores
- ordenes_compra → usuarios (comprador)
- detalles_orden_compra → productos
```

### ON DELETE SET NULL
Establece NULL al eliminar (para relaciones opcionales):
```sql
- productos → categorias
- ventas → usuarios (vendedor)
- ordenes_compra → usuarios (aprobador/receptor)
- proveedores → usuarios (creador/actualizador)
```

### ON UPDATE CASCADE
Actualiza referencias automáticamente:
```sql
- Todas las foreign keys usan ON UPDATE CASCADE
```

---

## Índices del Schema

### Índices UNIQUE (Unicidad)
```sql
categorias:           nombre
productos:            codigo
clientes:             email
usuarios:             username, email
ventas:               numero_factura
ordenes_compra:       numero_orden
proveedores:          rut
roles:                nombre
```

### Índices de Búsqueda/Performance
```sql
clientes:
  - idx_cliente_rut
  - idx_cliente_nombre (nombre, apellido)
  - idx_cliente_activo

productos:
  - idx_producto_nombre
  - idx_producto_activo

ventas:
  - idx_venta_fecha
  - idx_venta_cliente
  - idx_venta_vendedor
  - idx_venta_estado

movimientos_inventario:
  - idx_producto
  - idx_tipo
  - idx_fecha
  - idx_usuario

ordenes_compra:
  - idx_numero_orden
  - idx_proveedor
  - idx_estado
  - idx_fecha_orden
  - idx_usuario_comprador

detalles_orden_compra:
  - idx_orden_compra
  - idx_producto_detalle

proveedores:
  - idx_rut_proveedor
  - idx_nombre_proveedor
  - idx_activo_proveedor
```

---

## Valores por Defecto

```sql
categorias.activa                         = 1 (true)
configuracion_sistema.color_primario      = '#0d6efd'
configuracion_sistema.smtp_ssl_habilitado = 1 (true)
configuracion_sistema.dias_inactividad... = 30
configuracion_sistema.habilitar_notific.. = 1 (true)
exportacion_historial.estado              = 'Procesando'
clientes.activo                           = 1 (true)
clientes.numero_compras                   = 0
productos.stock                           = 0
productos.activo                          = 1 (true)
proveedores.activo                        = 1 (true)
usuarios.activo                           = 1 (true)
ventas.estado                             = 'PENDIENTE'
ventas.descuento                          = 0.0
ordenes_compra.estado                     = 'PENDIENTE'
ordenes_compra.subtotal                   = 0.00
ordenes_compra.porcentaje_impuesto        = 19.00
ordenes_compra.monto_impuesto             = 0.00
ordenes_compra.descuento                  = 0.00
ordenes_compra.total                      = 0.00
detalles_orden_compra.porcentaje_descuento= 0.00
detalles_orden_compra.monto_descuento     = 0.00
detalles_orden_compra.cantidad_recibida   = 0
```

---

## Configuración del Schema

### Charset y Collation
```sql
CHARACTER SET: utf8mb4
COLLATION:     utf8mb4_unicode_ci
```

**Beneficios:**
- Soporte completo para caracteres Unicode (incluyendo emojis)
- Comparaciones case-insensitive
- Compatibilidad internacional

### Storage Engine
```sql
ENGINE: InnoDB
```

**Beneficios:**
- Soporte de transacciones ACID
- Integridad referencial (foreign keys)
- Bloqueo a nivel de fila
- Recuperación ante fallos

---

## Enumeraciones

### MovimientoInventario.TipoMovimiento
```
COMPRA                 - Entrada por compra a proveedor
DEVOLUCION_ENTRADA     - Entrada por devolución de cliente
VENTA                  - Salida por venta a cliente
DEVOLUCION_SALIDA      - Salida por devolución a proveedor
AJUSTE_POSITIVO        - Ajuste positivo (corrección, inventario encontrado)
AJUSTE_NEGATIVO        - Ajuste negativo (merma, robo, daño)
TRANSFERENCIA_ENTRADA  - Transferencia entre bodegas (entrada)
TRANSFERENCIA_SALIDA   - Transferencia entre bodegas (salida)
INVENTARIO_INICIAL     - Entrada inicial al crear producto
```

### OrdenCompra.EstadoOrden
```
BORRADOR           - Orden creada pero no aprobada
PENDIENTE          - Orden aprobada pendiente de envío
ENVIADA            - Orden enviada al proveedor
CONFIRMADA         - Orden confirmada por el proveedor
EN_TRANSITO        - Mercancía en tránsito
RECIBIDA_PARCIAL   - Mercancía recibida parcialmente
RECIBIDA_COMPLETA  - Mercancía recibida completamente
COMPLETADA         - Orden completada y cerrada
CANCELADA          - Orden cancelada
```

### Venta.EstadoVenta
```
PENDIENTE              - Venta pendiente
EN_PROCESO             - Venta en proceso
COMPLETADA             - Venta completada
ANULADA                - Venta anulada
PARCIALMENTE_PAGADA    - Venta parcialmente pagada
```

### Venta.MetodoPago
```
EFECTIVO           - Pago en efectivo
TARJETA_CREDITO    - Pago con tarjeta de crédito
TARJETA_DEBITO     - Pago con tarjeta de débito
TRANSFERENCIA      - Transferencia bancaria
CHEQUE             - Pago con cheque
CREDITO            - Pago a crédito
```

---

## Archivos Generados

### `/home/user/SGE/schema-mysql.sql`
Schema SQL completo listo para ejecutar en MySQL 8.0.

**Contenido:**
- Configuración inicial (SET statements)
- DROP TABLE IF EXISTS (orden inverso)
- CREATE TABLE statements (orden de dependencias)
- Restauración de configuración
- Comentarios extensivos

**Uso:**
```bash
mysql -u root -p nombre_base_datos < schema-mysql.sql
```

---

## Estadísticas del Schema

```
Total de tablas:                    18
  - Tablas principales:             16
  - Tablas auxiliares:               2

Total de foreign keys:              22
Total de índices únicos:            10
Total de índices de búsqueda:       30+
Total de enums:                      4

Líneas de código SQL:             ~800
```

---

## Consideraciones Especiales

### 1. Campos Calculados (@Transient)
Los siguientes métodos en las entidades NO se persisten en la BD:
- `Venta.getEstadoAsString()`
- `Venta.getMetodoPagoAsString()`
- `Venta.getCantidad()`
- `Venta.getProducto()`
- `Producto.getNombreFormateado()`
- `Producto.isDisponible()`
- Todos los métodos marcados con `@Transient`

### 2. Callbacks JPA
Las siguientes operaciones se ejecutan automáticamente:
- `@PrePersist`: Antes de insertar (establece fechas de creación)
- `@PreUpdate`: Antes de actualizar (actualiza fechas de modificación)
- `ConfiguracionSistema.onUpdate()`: Actualiza ultima_actualizacion
- `OrdenCompra.establecerDatosCreacion()`: Genera número de orden
- `DetalleOrdenCompra.calcularSubtotal()`: Calcula subtotales

### 3. Validaciones JPA (NO aplicadas en BD)
Las siguientes validaciones son de aplicación, no de BD:
- `@NotBlank`: Validación de string no vacío
- `@NotNull`: Validación de no nulo
- `@Email`: Validación de formato email
- `@Positive`: Validación de número positivo
- `@PositiveOrZero`: Validación de número no negativo
- `@Pattern`: Validación de expresión regular
- `@Size`: Validación de longitud

**IMPORTANTE:** Estas validaciones se ejecutan en Java, no en MySQL.

### 4. Relaciones Lazy Loading
Todas las relaciones `@ManyToOne` y `@OneToMany` usan `FetchType.LAZY`:
- Los datos relacionados NO se cargan automáticamente
- Se requiere configuración de JPA/Hibernate para manejar esto
- Puede causar excepciones `LazyInitializationException` si no se maneja correctamente

---

## Recomendaciones

### Para Producción

1. **Backup Regular**
   ```bash
   mysqldump -u root -p nombre_bd > backup_$(date +%Y%m%d).sql
   ```

2. **Monitoreo de Índices**
   ```sql
   SHOW INDEX FROM tabla_name;
   EXPLAIN SELECT ... FROM tabla_name WHERE ...;
   ```

3. **Optimización**
   ```sql
   ANALYZE TABLE tabla_name;
   OPTIMIZE TABLE tabla_name;
   ```

4. **Seguridad**
   - Usar contraseñas seguras para usuarios de BD
   - Cifrar conexiones SSL/TLS
   - Implementar auditoría de cambios

### Para Desarrollo

1. **Versionado del Schema**
   - Usar herramientas de migración (Flyway, Liquibase)
   - Mantener historial de cambios

2. **Testing**
   - Usar bases de datos separadas para dev/test/prod
   - Implementar tests de integración

3. **Documentación**
   - Mantener este documento actualizado
   - Documentar cambios en el schema

---

## Contacto

**Autor:** Roberto Rivas
**Versión:** 2.0
**Proyecto:** InformViva - Sistema de Gestión Empresarial
**Fecha:** 2025-11-09
