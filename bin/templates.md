04/06/2025
# 📁 Estructura Completa de Templates - InformViva

## 🗂️ Directorio Principal: `src/main/resources/templates/`

```
templates/
├── 📄 inicio.html                          # Página principal/dashboard
├── 📄 login.html                           # Página de login
├── 📄 contacto.html                        # Página de contacto
├── 📄 dashboard.html                       # Dashboard principal
├── 📄 panel-ventas.html                    # Panel de ventas
├── 📄 productos.html                       # Vista de productos (general)
├── 📄 productos-vendedor.html              # Vista productos para vendedores
├── 📄 reportes.html                        # Página de reportes
│
├── 📁 admin/                               # 🔧 ADMINISTRACIÓN
│   ├── 📄 usuarios.html                    # Lista de usuarios
│   ├── 📄 usuario-form.html                # Formulario crear/editar usuario
│   ├── 📄 usuario-roles.html               # Gestión de roles de usuario
│   ├── 📄 usuario-password.html            # Cambio de contraseña
│   ├── 📄 usuarios-ultimas-vistas.html     # Últimas vistas de roles
│   ├── 📄 roles.html                       # Lista de roles
│   ├── 📄 rol-form.html                    # Formulario crear/editar rol
│   ├── 📄 rol-permisos.html                # Gestión de permisos
│   ├── 📄 configuracion.html               # Configuración del sistema
│   └── 📁 usuarios.export.pdf/
│       └── 📄 usuarios_pdf.html            # Template para PDF de usuarios
│
├── 📁 categorias/                          # 🆕 CATEGORÍAS (NUEVO)
│   ├── 📄 lista.html                       # Lista paginada de categorías
│   ├── 📄 formulario.html                  # Crear/editar categoría
│   └── 📄 detalle.html                     # Vista detalle de categoría
│
├── 📁 clientes/                            # 👥 CLIENTES
│   ├── 📄 lista.html                       # Lista general de clientes
│   ├── 📄 lista-clientes.html              # Lista alternativa
│   ├── 📄 lista-admin.html                 # Vista administrativa
│   ├── 📄 formulario.html                  # Crear/editar cliente
│   ├── 📄 detalle.html                     # Vista detalle de cliente
│   └── 📄 clientes-desde-inicio.html       # Vista desde inicio
│
├── 📁 productos/                           # 📦 PRODUCTOS
│   ├── 📄 lista-admin.html                 # Lista administrativa
│   ├── 📄 formulario.html                  # Crear/editar producto
│   ├── 📄 detalle.html                     # Vista detalle de producto
│   └── 📄 bajo-stock.html                  # Productos con bajo stock
│
├── 📁 ventas/                              # 💰 VENTAS
│   ├── 📄 lista.html                       # Lista de ventas
│   ├── 📄 nueva.html                       # Crear nueva venta
│   ├── 📄 editar.html                      # Editar venta
│   └── 📄 detalle.html                     # Vista detalle de venta
│
├── 📁 reportes/                            # 📊 REPORTES
│   ├── 📄 clientes.html                    # Reportes de clientes
│   ├── 📄 clientes-dashboard.html          # Dashboard de clientes
│   └── 📄 clientes-inactivos.html          # Clientes inactivos
│
├── 📁 error/                               # ❌ PÁGINAS DE ERROR
│   ├── 📄 default.html                     # Error genérico
│   ├── 📄 404.html                         # Página no encontrada
│   └── 📄 500.html                         # Error interno del servidor
│
└── 📁 fragments/                           # 🧩 FRAGMENTOS REUTILIZABLES
    ├── 📄 navbar.html                      # Barra de navegación
    ├── 📄 sidebar.html                     # Barra lateral
    ├── 📄 footer.html                      # Pie de página
    ├── 📄 head.html                        # Meta tags y CSS
    └── 📄 scripts.html                     # Scripts JavaScript
```

## 📊 Estadísticas de la Estructura

| Módulo | Vistas | Estado | Funcionalidad |
|--------|--------|--------|---------------|
| **🏠 Core** | 6 | ✅ Completo | Dashboard, login, inicio |
| **🔧 Admin** | 9 | ✅ Completo | Usuarios, roles, config |
| **🆕 Categorías** | 3 | 🆕 **NUEVO** | **CRUD completo** |
| **👥 Clientes** | 6 | ✅ Completo | Gestión de clientes |
| **📦 Productos** | 4 | ✅ Completo | Gestión de productos |
| **💰 Ventas** | 4 | ✅ Completo | Proceso de ventas |
| **📊 Reportes** | 3 | ✅ Completo | Análisis y reportes |
| **❌ Errores** | 3 | ✅ Completo | Manejo de errores |
| **🧩 Fragmentos** | 5 | ⚠️ Parcial | Reutilizables |

## 🆕 Nuevas Adiciones - Módulo Categorías

### **Vistas Agregadas:**
```
📁 categorias/
├── 📄 lista.html          # Lista paginada con filtros y búsqueda
├── 📄 formulario.html     # Formulario crear/editar con validaciones
└── 📄 detalle.html        # Vista completa con estadísticas
```

### **Características Implementadas:**
- ✅ **CRUD Completo** - Crear, leer, actualizar, eliminar
- ✅ **Seguridad por Roles** - ADMIN, PRODUCTOS, GERENTE, VENTAS
- ✅ **Paginación Avanzada** - Filtros, búsqueda, ordenamiento
- ✅ **Responsive Design** - Bootstrap 5 + Font Awesome
- ✅ **Validaciones Frontend** - Tiempo real + UX mejorada
- ✅ **Integración Lista** - Compatible con tu arquitectura

## 🔗 Rutas de Navegación

### **Principales:**
```
/inicio                    → inicio.html
/login                     → login.html
/dashboard                 → dashboard.html
```

### **Administración:**
```
/admin/usuarios            → admin/usuarios.html
/admin/roles               → admin/roles.html
/admin/configuracion       → admin/configuracion.html
```

### **🆕 Categorías (NUEVO):**
```
/categorias                → categorias/lista.html
/categorias/nueva          → categorias/formulario.html
/categorias/editar/{id}    → categorias/formulario.html
/categorias/detalle/{id}   → categorias/detalle.html
```

### **Módulos Operativos:**
```
/clientes                  → clientes/lista.html
/productos/admin           → productos/lista-admin.html
/ventas/lista              → ventas/lista.html
/reportes/clientes         → reportes/clientes.html
```

## 🎯 Integraciones Recomendadas

### **1. Navegación Principal**
Agregar en `fragments/navbar.html`:
```html
<li class="nav-item dropdown">
    <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
        <i class="fas fa-cube"></i> Inventario
    </a>
    <ul class="dropdown-menu">
        <li><a class="dropdown-item" href="/categorias">
            <i class="fas fa-tags"></i> Categorías
        </a></li>
        <li><a class="dropdown-item" href="/productos/admin">
            <i class="fas fa-box"></i> Productos
        </a></li>
    </ul>
</li>
```

### **2. Sidebar**
Agregar en `fragments/sidebar.html`:
```html
<div class="nav-section">
    <h6 class="sidebar-heading">Inventario</h6>
    <ul class="nav flex-column">
        <li class="nav-item">
            <a class="nav-link" href="/categorias">
                <i class="fas fa-tags"></i> Categorías
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/productos/admin">
                <i class="fas fa-box"></i> Productos
            </a>
        </li>
    </ul>
</div>
```

### **3. Dashboard Principal**
Agregar widget en `inicio.html`:
```html
<div class="col-md-3">
    <div class="card text-white bg-info">
        <div class="card-body">
            <div class="d-flex justify-content-between">
                <div>
                    <h6 class="card-title">Categorías</h6>
                    <h3 th:text="${totalCategorias ?: 0}">0</h3>
                </div>
                <div class="align-self-center">
                    <i class="fas fa-tags fa-2x"></i>
                </div>
            </div>
        </div>
    </div>
</div>
```

## 📱 Compatibilidad y Estándares

### **Framework CSS:**
- ✅ **Bootstrap 5.3.0** - Componentes modernos
- ✅ **Font Awesome 6.0.0** - Iconografía consistente
- ✅ **Responsive Grid** - Mobile-first design

### **JavaScript:**
- ✅ **ES6+ Nativo** - Sin dependencias adicionales
- ✅ **Bootstrap JS** - Componentes interactivos
- ✅ **Validaciones Frontend** - UX mejorada

### **Accesibilidad:**
- ✅ **Semantic HTML** - Estructura correcta
- ✅ **ARIA Labels** - Screen readers
- ✅ **Keyboard Navigation** - Accesible por teclado

## 🚀 Próximos Pasos

1. **✅ Categorías** - **COMPLETADO**
2. **🔄 Integrar** fragmentos existentes
3. **📊 Estadísticas** adicionales en dashboard
4. **🔗 Conectar** productos con categorías
5. **📱 Testing** responsive en dispositivos
6. **🎨 Personalización** de estilos (opcional)

## 💡 Recomendaciones

### **Organización:**
- Mantener consistencia en nombres de archivos
- Usar fragmentos para elementos repetitivos
- Documentar cada vista con comentarios

### **Performance:**
- Lazy loading para imágenes
- Minificación de CSS/JS en producción
- Cache de fragmentos estáticos

### **Mantenimiento:**
- Versionado de templates
- Testing de vistas en diferentes navegadores
- Backup de templates personalizados

¡Tu estructura de templates está muy bien organizada y ahora es aún más completa! 🎉