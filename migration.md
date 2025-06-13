# migration.md

# Guía de Migración - APIs de Ventas

## Cambios en rutas API

### Antes

``` javascript
// JavaScript de cliente antiguo
fetch('/ventas/api/productos')
  .then(response => response.json())
  .then(data => console.log(data));
```

## Cambios en rutas de vistas

### Antes

Enlaces a:

``` html
<a href="/ventas">Ver ventas</a>
```

### Después

Enlaces a:

``` html
<a href="/ventas/lista">Ver ventas</a>
```

## Calendario de eliminación

- 01/06/2025: Revisión de logs para identificar clientes que aún usan rutas antiguas
- 01/09/2025: Envío de avisos a equipos que aún usan rutas antiguas
- 01/12/2025: Eliminación definitiva de las rutas antiguas

## Instrucciones de depreciación

Estas instrucciones de depreciación ayudan a:

1. Informar claramente a otros desarrolladores sobre los cambios
2. Establecer fechas concretas para la eliminación
3. Registrar el uso de rutas obsoletas para seguimiento
4. Documentar la razón y el proceso de migración

Lo más importante es incluir las anotaciones `@Deprecated` junto con los comentarios JavaDoc explicativos en los métodos
de redirección y en la clase de compatibilidad.