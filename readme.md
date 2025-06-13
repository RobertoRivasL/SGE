## Cambios en la versión 2.1.0

### Rutas deprecadas

Las siguientes rutas han sido marcadas como deprecadas y serán eliminadas después del 01/12/2025:

- `/ventas` -> Usar `/ventas/lista` en su lugar
- Todas las rutas API bajo `/ventas/api/*` -> Usar `/api/ventas/*` en su lugar

### Razones del cambio

- Mejor separación de responsabilidades siguiendo principios REST
- Evitar ambigüedades de mapeo en controladores
    - Estructura más consistente para APIs

- Mejora en la legibilidad y mantenibilidad del código
- Facilitar la integración con otros servicios y sistemas
- Mejorar la experiencia del desarrollador al trabajar con la API
- Facilitar la evolución futura de la API
- Mejorar la documentación y la comprensión de la API
- Facilitar la implementación de nuevas características y funcionalidades
- Mejorar la seguridad y el control de acceso a la API
- Facilitar la implementación de pruebas automatizadas y la validación de la API
- Mejorar la gestión de errores y la respuesta de la API
- Facilitar la implementación de nuevas versiones de la API sin afectar a los clientes existentes
- Mejorar la interoperabilidad con otros sistemas y servicios

src/main/
├── java/informviva/gest/
│ ├── config/
│ │ ├── InicializadorUsuarios.java
│ │ └── LoggingFilter.java
│ ├── controlador/
│ │ ├── api/
│ │ │ ├── SistemaRestControlador.java
│ │ │ └── VentaRestControlador.java
│ │ ├── ClienteControlador.java
│ │ ├── DashboardControladorAPI.java
│ │ ├── DashboardControladorVista.java
│ │ ├── InicioControlador.java
│ │ ├── PanelControlador.java
│ │ ├── ProductoControlador.java
│ │ └── VentaControlador.java
│ ├── dto/
│ │ ├── MetricaDTO.java
│ │ ├── VentaDTO.java
│ │ └── ... (otros DTOs)
│ ├── exception/
│ │ ├── GlobalExceptionHandler.java
│ │ └── ... (otras excepciones)
│ ├── model/
│ │ ├── Cliente.java
│ │ ├── Producto.java
│ │ ├── Usuario.java
│ │ ├── Venta.java
│ │ └── ... (otros modelos)
│ ├── repository/
│ │ ├── ClienteRepositorio.java
│ │ ├── ProductoRepositorio.java
│ │ └── ... (otros repositorios)
│ ├── seguridad/
│ │ ├── ConfiguracionSeguridad.java
│ │ └── ... (otras clases de seguridad)
│ ├── service/
│ │ ├── impl/
│ │ │ ├── ClienteServicioImpl.java
│ │ │ ├── ProductoServicioImpl.java
│ │ │ └── ... (otras implementaciones)
│ │ ├── ClienteServicio.java
│ │ ├── ProductoServicio.java
│ │ ├── SistemaIntegradoServicio.java
│ │ └── ... (otros servicios)
│ └── util/
│ ├── MensajesConstantes.java
│ └── ... (otras clases de utilidad)
└── resources/
├── static/
│ ├── css/
│ │ ├── bootstrap.min.css
│ │ └── custom.css
│ ├── js/
│ │ ├── bootstrap.bundle.min.js
│ │ ├── chart.min.js
│ │ └── app.js
│ └── img/
└── templates/
├── admin/
│ ├── usuarios.html
│ └── configuracion.html
├── clientes/
│ ├── lista.html
│ └── formulario.html
├── layout/
│ └── base.html
├── panel/
│ ├── principal.html
│ ├── resumen.html
│ └── inventario-ventas.html
├── productos/
│ ├── lista.html
│ └── formulario.html
├── ventas/
│ ├── lista.html
│ ├── nueva.html
│ └── detalle.html
├── error/
│ └── 404.html
├── login.html
└── index.html