src/main/resources/templates/
├── fragments/                          # Fragmentos reutilizables
│   ├── head.html                      # <head> común
│   ├── navbar.html                    # Barra de navegación
│   ├── sidebar.html                   # Menú lateral
│   ├── footer.html                    # Pie de página
│   └── scripts.html                   # Scripts comunes
│
├── layouts/                           # Layouts base
│   ├── base.html                      # Layout principal
│   ├── admin.html                     # Layout para admin
│   └── simple.html                    # Layout simple (login, error)
│
├── error/                             # Páginas de error
│   ├── default.html                   # Error genérico
│   ├── 404.html                       # No encontrado
│   ├── 500.html                       # Error interno
│   └── access-denied.html             # Acceso denegado
│
├── auth/                              # Autenticación
│   └── login.html                     # Página de login
│
├── inicio.html                        # Página de inicio/dashboard principal
├── contacto.html                      # Página de contacto
├── dashboard.html                     # Dashboard específico (DashboardControladorVista)
│
├── admin/                             # Administración general
│   ├── configuracion.html             # Configuración del sistema
│   │
│   ├── usuarios/                      # Gestión de usuarios
│   │   ├── lista.html                 # Lista de usuarios (admin/usuarios)
│   │   ├── formulario.html            # Crear/editar usuario (admin/usuario-form)
│   │   ├── roles.html                 # Gestión de roles (admin/usuario-roles)
│   │   ├── password.html              # Cambio de contraseña (admin/usuario-password)
│   │   └── ultimas-vistas.html        # Últimas vistas de roles (admin/usuarios-ultimas-vistas)
│   │
│   ├── roles/                         # Gestión de roles y permisos
│   │   ├── lista.html                 # Lista de roles (admin/roles)
│   │   ├── formulario.html            # Crear/editar rol (admin/rol-form)
│   │   └── permisos.html              # Gestión de permisos (admin/rol-permisos)
│   │
│   └── respaldos/                     # 🆕 Sistema de respaldos
│       ├── lista.html                 # Lista de respaldos disponibles
│       ├── crear.html                 # Formulario para crear respaldo
│       ├── restaurar.html             # Formulario para restaurar
│       └── configuracion.html         # Configuración de respaldos automáticos
│
├── ventas/                            # Módulo de ventas
│   ├── lista.html                     # Lista de ventas (ventas/lista)
│   ├── nueva.html                     # Crear nueva venta (ventas/nueva)
│   ├── editar.html                    # Editar venta (ventas/editar)
│   └── detalle.html                   # Detalle de venta (ventas/detalle)
│
├── productos/                         # Módulo de productos
│   ├── lista-admin.html               # Lista admin de productos (productos/lista-admin)
│   ├── lista-vendedor.html            # Lista para vendedores (productos-vendedor)
│   ├── formulario.html                # Crear/editar producto (productos/formulario)
│   ├── detalle.html                   # Detalle de producto (productos/detalle)
│   └── bajo-stock.html                # Productos con bajo stock (productos/bajo-stock)
│
├── clientes/                          # Módulo de clientes
│   ├── lista.html                     # Lista general de clientes (clientes/lista)
│   ├── lista-admin.html               # Lista admin de clientes (clientes/lista-admin)
│   ├── formulario.html                # Crear/editar cliente (clientes/formulario)
│   └── detalle.html                   # Detalle de cliente (clientes/detalle)
│
├── reportes/                          # Módulo de reportes
│   ├── index.html                     # Panel principal de reportes (reportes)
│   ├── panel-ventas.html              # Panel de ventas (panel-ventas)
│   │
│   ├── clientes/                      # Reportes de clientes
│   │   ├── index.html                 # Reporte principal (reportes/clientes)
│   │   ├── dashboard.html             # Dashboard de clientes (reportes/clientes-dashboard)
│   │   └── inactivos.html             # Clientes inactivos (reportes/clientes-inactivos)
│   │
│   ├── productos/                     # 🆕 Reportes de productos
│   │   ├── index.html                 # Reporte principal de productos
│   │   ├── mas-vendidos.html          # Productos más vendidos
│   │   ├── bajo-stock.html            # Reporte de bajo stock
│   │   └── categoria.html             # Reportes por categoría
│   │
│   └── ventas/                        # 🆕 Reportes de ventas
│       ├── resumen.html               # Resumen general
│       ├── por-vendedor.html          # Ventas por vendedor
│       ├── por-periodo.html           # Ventas por período
│       └── comparativo.html           # Análisis comparativo
│
└── email/                             # 🆕 Templates para emails
├── respaldo-exitoso.html          # Email de respaldo exitoso
├── respaldo-fallido.html          # Email de respaldo fallido
├── bajo-stock-alerta.html         # Alerta de bajo stock
└── reporte-semanal.html           # Reporte semanal automático