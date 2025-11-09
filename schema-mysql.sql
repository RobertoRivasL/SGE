-- ============================================================================
-- SCHEMA SQL PARA SISTEMA DE GESTION EMPRESARIAL (SGE)
-- ============================================================================
-- Proyecto: InformViva - Sistema de Gestión Empresarial
-- Autor: Roberto Rivas
-- Versión: 2.0
-- Base de Datos: MySQL 8.0
-- Charset: UTF8MB4 (Soporte completo Unicode)
-- Engine: InnoDB (Transacciones ACID, Integridad Referencial)
--
-- DESCRIPCIÓN:
-- Este schema contiene todas las tablas necesarias para el Sistema de Gestión
-- Empresarial, incluyendo módulos de:
-- - Gestión de Usuarios y Roles
-- - Gestión de Clientes
-- - Gestión de Productos y Categorías
-- - Gestión de Inventario y Movimientos
-- - Gestión de Ventas
-- - Gestión de Compras y Proveedores
-- - Configuración del Sistema
-- - Historial de Importaciones/Exportaciones
-- ============================================================================

-- ============================================================================
-- CONFIGURACIÓN INICIAL
-- ============================================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================================================
-- CREACIÓN DE BASE DE DATOS
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `informviva_gest`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `informviva_gest`;

-- ============================================================================
-- ELIMINACIÓN DE TABLAS EXISTENTES (EN ORDEN INVERSO DE DEPENDENCIAS)
-- ============================================================================

DROP TABLE IF EXISTS `detalles_orden_compra`;
DROP TABLE IF EXISTS `ordenes_compra`;
DROP TABLE IF EXISTS `venta_detalle`;
DROP TABLE IF EXISTS `ventas`;
DROP TABLE IF EXISTS `movimientos_inventario`;
DROP TABLE IF EXISTS `importacion_historial`;
DROP TABLE IF EXISTS `productos`;
DROP TABLE IF EXISTS `proveedores`;
DROP TABLE IF EXISTS `clientes`;
DROP TABLE IF EXISTS `usuario_roles`;
DROP TABLE IF EXISTS `usuarios`;
DROP TABLE IF EXISTS `rol_permisos`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `categorias`;
DROP TABLE IF EXISTS `configuracion_sistema`;
DROP TABLE IF EXISTS `exportacion_historial`;
DROP TABLE IF EXISTS `rol_vistas`;

-- ============================================================================
-- CREACIÓN DE TABLAS (EN ORDEN DE DEPENDENCIAS)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: categorias
-- Descripción: Categorías de productos para clasificación
-- Dependencias: Ninguna
-- ----------------------------------------------------------------------------
CREATE TABLE `categorias` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(255) NOT NULL,
    `descripcion` VARCHAR(255) DEFAULT NULL,
    `activa` TINYINT(1) DEFAULT 1,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_categoria_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Categorías de productos';

-- ----------------------------------------------------------------------------
-- Tabla: configuracion_sistema
-- Descripción: Configuraciones globales del sistema (empresa, SMTP, etc.)
-- Dependencias: Ninguna
-- ----------------------------------------------------------------------------
CREATE TABLE `configuracion_sistema` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nombre_empresa` VARCHAR(100) NOT NULL,
    `direccion_empresa` VARCHAR(200) DEFAULT NULL,
    `telefono_empresa` VARCHAR(50) DEFAULT NULL,
    `email_contacto` VARCHAR(100) DEFAULT NULL,
    `logo_url` VARCHAR(200) DEFAULT NULL,
    `color_primario` VARCHAR(50) DEFAULT '#0d6efd',
    `smtp_host` VARCHAR(255) DEFAULT NULL,
    `smtp_port` INT DEFAULT NULL,
    `smtp_usuario` VARCHAR(255) DEFAULT NULL,
    `smtp_password` VARCHAR(255) DEFAULT NULL,
    `smtp_ssl_habilitado` TINYINT(1) DEFAULT 1,
    `dias_inactividad_alerta` INT DEFAULT 30,
    `habilitar_notificaciones` TINYINT(1) DEFAULT 1,
    `ultima_actualizacion` DATETIME DEFAULT NULL,
    `usuario_actualizacion` VARCHAR(255) DEFAULT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Configuración general del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: exportacion_historial
-- Descripción: Historial de todas las exportaciones realizadas
-- Dependencias: Ninguna
-- ----------------------------------------------------------------------------
CREATE TABLE `exportacion_historial` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tipo_exportacion` VARCHAR(255) NOT NULL,
    `formato` VARCHAR(255) NOT NULL,
    `usuario_solicitante` VARCHAR(255) NOT NULL,
    `fecha_solicitud` DATETIME NOT NULL,
    `fecha_inicio_datos` DATE DEFAULT NULL,
    `fecha_fin_datos` DATE DEFAULT NULL,
    `tamano_archivo` BIGINT DEFAULT NULL,
    `estado` VARCHAR(30) NOT NULL DEFAULT 'Procesando',
    `mensaje_error` TEXT DEFAULT NULL,
    `numero_registros` INT DEFAULT NULL,
    `tiempo_procesamiento` BIGINT DEFAULT NULL,
    `ruta_archivo` VARCHAR(500) DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_tipo_exportacion` (`tipo_exportacion`),
    KEY `idx_fecha_solicitud` (`fecha_solicitud`),
    KEY `idx_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de exportaciones del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: rol_vistas
-- Descripción: Vistas de roles por usuario
-- Dependencias: Ninguna (tabla de auditoría/vista)
-- ----------------------------------------------------------------------------
CREATE TABLE `rol_vistas` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rol_nombre` VARCHAR(255) DEFAULT NULL,
    `usuario_id` BIGINT DEFAULT NULL,
    `username` VARCHAR(255) DEFAULT NULL,
    `fecha_vista` TIMESTAMP NULL DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_usuario_id` (`usuario_id`),
    KEY `idx_rol_nombre` (`rol_nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de visualización de roles';

-- ----------------------------------------------------------------------------
-- Tabla: roles
-- Descripción: Roles del sistema para control de acceso
-- Dependencias: Ninguna
-- ----------------------------------------------------------------------------
CREATE TABLE `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL,
    `descripcion` VARCHAR(200) DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rol_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Roles de usuario del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: rol_permisos
-- Descripción: Permisos asociados a cada rol (relación muchos-a-muchos)
-- Dependencias: roles
-- ----------------------------------------------------------------------------
CREATE TABLE `rol_permisos` (
    `rol_id` BIGINT NOT NULL,
    `permiso` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`rol_id`, `permiso`),
    CONSTRAINT `fk_rol_permisos_rol`
        FOREIGN KEY (`rol_id`)
        REFERENCES `roles` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Permisos asociados a roles';

-- ----------------------------------------------------------------------------
-- Tabla: usuarios
-- Descripción: Usuarios del sistema (empleados, vendedores, administradores)
-- Dependencias: Ninguna (auto-referencial para auditoría)
-- ----------------------------------------------------------------------------
CREATE TABLE `usuarios` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nombre` VARCHAR(255) NOT NULL,
    `apellido` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `activo` TINYINT(1) DEFAULT 1,
    `fecha_creacion` DATETIME DEFAULT NULL,
    `ultimo_acceso` DATETIME DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_usuario_username` (`username`),
    UNIQUE KEY `uk_usuario_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Usuarios del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: usuario_roles
-- Descripción: Roles asignados a usuarios (relación muchos-a-muchos)
-- Dependencias: usuarios
-- ----------------------------------------------------------------------------
CREATE TABLE `usuario_roles` (
    `usuario_id` BIGINT NOT NULL,
    `rol` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`usuario_id`, `rol`),
    CONSTRAINT `fk_usuario_roles_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Roles asignados a usuarios';

-- ----------------------------------------------------------------------------
-- Tabla: clientes
-- Descripción: Clientes del sistema
-- Dependencias: Ninguna
-- ----------------------------------------------------------------------------
CREATE TABLE `clientes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL,
    `apellido` VARCHAR(50) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `telefono` VARCHAR(15) DEFAULT NULL,
    `direccion` VARCHAR(100) DEFAULT NULL,
    `rut` VARCHAR(255) NOT NULL,
    `fecha_registro` DATETIME NOT NULL,
    `categoria` VARCHAR(30) DEFAULT NULL,
    `ciudad` VARCHAR(50) DEFAULT NULL,
    `fecha_modificacion` DATETIME DEFAULT NULL,
    `fecha_ultima_compra` DATETIME DEFAULT NULL,
    `fecha_nacimiento` DATE DEFAULT NULL,
    `activo` TINYINT(1) DEFAULT 1,
    `total_compras` DECIMAL(19,2) DEFAULT NULL,
    `numero_compras` INT DEFAULT 0,
    `usuario_creacion` VARCHAR(255) DEFAULT NULL,
    `usuario_modificacion` VARCHAR(255) DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cliente_email` (`email`),
    KEY `idx_cliente_rut` (`rut`),
    KEY `idx_cliente_nombre` (`nombre`, `apellido`),
    KEY `idx_cliente_activo` (`activo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Clientes del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: productos
-- Descripción: Productos disponibles para la venta
-- Dependencias: categorias
-- ----------------------------------------------------------------------------
CREATE TABLE `productos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `codigo` VARCHAR(255) NOT NULL,
    `nombre` VARCHAR(255) NOT NULL,
    `descripcion` VARCHAR(255) DEFAULT NULL,
    `activo` TINYINT(1) DEFAULT 1,
    `precio` DOUBLE NOT NULL,
    `stock` INT DEFAULT 0,
    `stock_minimo` INT DEFAULT NULL,
    `marca` VARCHAR(255) DEFAULT NULL,
    `modelo` VARCHAR(255) DEFAULT NULL,
    `fecha_creacion` DATETIME DEFAULT NULL,
    `fecha_actualizacion` DATETIME DEFAULT NULL,
    `categoria_id` BIGINT DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_producto_codigo` (`codigo`),
    KEY `idx_producto_nombre` (`nombre`),
    KEY `idx_producto_activo` (`activo`),
    KEY `fk_producto_categoria` (`categoria_id`),
    CONSTRAINT `fk_producto_categoria`
        FOREIGN KEY (`categoria_id`)
        REFERENCES `categorias` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Productos del catálogo';

-- ----------------------------------------------------------------------------
-- Tabla: proveedores
-- Descripción: Proveedores para el módulo de compras
-- Dependencias: usuarios (para auditoría)
-- ----------------------------------------------------------------------------
CREATE TABLE `proveedores` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rut` VARCHAR(12) NOT NULL,
    `nombre` VARCHAR(200) NOT NULL,
    `nombre_fantasia` VARCHAR(200) DEFAULT NULL,
    `giro` VARCHAR(200) DEFAULT NULL,
    `direccion` VARCHAR(500) DEFAULT NULL,
    `ciudad` VARCHAR(100) DEFAULT NULL,
    `region` VARCHAR(100) DEFAULT NULL,
    `pais` VARCHAR(100) DEFAULT NULL,
    `codigo_postal` VARCHAR(20) DEFAULT NULL,
    `telefono` VARCHAR(20) DEFAULT NULL,
    `telefono_alternativo` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(150) DEFAULT NULL,
    `sitio_web` VARCHAR(200) DEFAULT NULL,
    `contacto_nombre` VARCHAR(150) DEFAULT NULL,
    `contacto_cargo` VARCHAR(100) DEFAULT NULL,
    `contacto_telefono` VARCHAR(20) DEFAULT NULL,
    `contacto_email` VARCHAR(150) DEFAULT NULL,
    `condiciones_pago` VARCHAR(100) DEFAULT NULL,
    `dias_credito` INT DEFAULT NULL,
    `observaciones` VARCHAR(1000) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL,
    `fecha_actualizacion` DATETIME DEFAULT NULL,
    `usuario_creacion_id` BIGINT DEFAULT NULL,
    `usuario_actualizacion_id` BIGINT DEFAULT NULL,
    `calificacion` INT DEFAULT NULL,
    `categoria` VARCHAR(100) DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_proveedor_rut` (`rut`),
    KEY `idx_rut_proveedor` (`rut`),
    KEY `idx_nombre_proveedor` (`nombre`),
    KEY `idx_activo_proveedor` (`activo`),
    KEY `fk_proveedor_usuario_creacion` (`usuario_creacion_id`),
    KEY `fk_proveedor_usuario_actualizacion` (`usuario_actualizacion_id`),
    CONSTRAINT `fk_proveedor_usuario_creacion`
        FOREIGN KEY (`usuario_creacion_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT `fk_proveedor_usuario_actualizacion`
        FOREIGN KEY (`usuario_actualizacion_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Proveedores del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: importacion_historial
-- Descripción: Historial de todas las importaciones realizadas
-- Dependencias: usuarios
-- ----------------------------------------------------------------------------
CREATE TABLE `importacion_historial` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tipo_importacion` VARCHAR(255) NOT NULL,
    `nombre_archivo` VARCHAR(255) NOT NULL,
    `fecha_importacion` DATETIME NOT NULL,
    `usuario_id` BIGINT NOT NULL,
    `total_registros` INT DEFAULT NULL,
    `registros_exitosos` INT DEFAULT NULL,
    `registros_con_error` INT DEFAULT NULL,
    `tiempo_procesamiento_ms` BIGINT DEFAULT NULL,
    `exitoso` TINYINT(1) DEFAULT NULL,
    `resumen` TEXT DEFAULT NULL,
    `errores` TEXT DEFAULT NULL,
    `advertencias` TEXT DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_fecha_importacion` (`fecha_importacion`),
    KEY `idx_tipo_importacion` (`tipo_importacion`),
    KEY `fk_importacion_usuario` (`usuario_id`),
    CONSTRAINT `fk_importacion_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de importaciones del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: movimientos_inventario
-- Descripción: Trazabilidad de movimientos de inventario (entradas/salidas)
-- Dependencias: productos, usuarios
-- Valores del enum TipoMovimiento:
--   - COMPRA, DEVOLUCION_ENTRADA, VENTA, DEVOLUCION_SALIDA,
--   - AJUSTE_POSITIVO, AJUSTE_NEGATIVO, TRANSFERENCIA_ENTRADA,
--   - TRANSFERENCIA_SALIDA, INVENTARIO_INICIAL
-- ----------------------------------------------------------------------------
CREATE TABLE `movimientos_inventario` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `producto_id` BIGINT NOT NULL,
    `tipo` VARCHAR(30) NOT NULL,
    `cantidad` INT NOT NULL,
    `stock_anterior` INT NOT NULL,
    `stock_nuevo` INT NOT NULL,
    `motivo` VARCHAR(500) DEFAULT NULL,
    `referencia_externa` VARCHAR(50) DEFAULT NULL,
    `usuario_id` BIGINT NOT NULL,
    `fecha` DATETIME NOT NULL,
    `observaciones` VARCHAR(1000) DEFAULT NULL,
    `costo_unitario` DECIMAL(10,2) DEFAULT NULL,
    `costo_total` DECIMAL(10,2) DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_producto` (`producto_id`),
    KEY `idx_tipo` (`tipo`),
    KEY `idx_fecha` (`fecha`),
    KEY `idx_usuario` (`usuario_id`),
    CONSTRAINT `fk_movimiento_producto`
        FOREIGN KEY (`producto_id`)
        REFERENCES `productos` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_movimiento_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Movimientos de inventario del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: ventas
-- Descripción: Ventas realizadas a clientes
-- Dependencias: clientes, usuarios
-- Valores del enum EstadoVenta:
--   - PENDIENTE, EN_PROCESO, COMPLETADA, ANULADA, PARCIALMENTE_PAGADA
-- Valores del enum MetodoPago:
--   - EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, CHEQUE, CREDITO
-- ----------------------------------------------------------------------------
CREATE TABLE `ventas` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `fecha` DATETIME NOT NULL,
    `cliente_id` BIGINT NOT NULL,
    `vendedor_id` BIGINT DEFAULT NULL,
    `subtotal` DOUBLE DEFAULT NULL,
    `impuesto` DOUBLE DEFAULT NULL,
    `total` DOUBLE NOT NULL,
    `metodo_pago` VARCHAR(20) DEFAULT NULL,
    `estado` VARCHAR(25) DEFAULT 'PENDIENTE',
    `observaciones` TEXT DEFAULT NULL,
    `numero_factura` VARCHAR(50) DEFAULT NULL,
    `descuento` DOUBLE DEFAULT 0.0,
    `fecha_creacion` DATETIME DEFAULT NULL,
    `fecha_actualizacion` DATETIME DEFAULT NULL,
    `usuario_creacion` VARCHAR(100) DEFAULT NULL,
    `usuario_actualizacion` VARCHAR(100) DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_venta_numero_factura` (`numero_factura`),
    KEY `idx_venta_fecha` (`fecha`),
    KEY `idx_venta_cliente` (`cliente_id`),
    KEY `idx_venta_vendedor` (`vendedor_id`),
    KEY `idx_venta_estado` (`estado`),
    CONSTRAINT `fk_venta_cliente`
        FOREIGN KEY (`cliente_id`)
        REFERENCES `clientes` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT `fk_venta_vendedor`
        FOREIGN KEY (`vendedor_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ventas del sistema';

-- ----------------------------------------------------------------------------
-- Tabla: venta_detalle
-- Descripción: Detalles/líneas de cada venta (productos vendidos)
-- Dependencias: ventas, productos
-- ----------------------------------------------------------------------------
CREATE TABLE `venta_detalle` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `venta_id` BIGINT NOT NULL,
    `producto_id` BIGINT NOT NULL,
    `cantidad` INT NOT NULL,
    `precio_unitario` DOUBLE NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_venta_detalle_venta` (`venta_id`),
    KEY `idx_venta_detalle_producto` (`producto_id`),
    CONSTRAINT `fk_venta_detalle_venta`
        FOREIGN KEY (`venta_id`)
        REFERENCES `ventas` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_venta_detalle_producto`
        FOREIGN KEY (`producto_id`)
        REFERENCES `productos` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Detalles de las ventas';

-- ----------------------------------------------------------------------------
-- Tabla: ordenes_compra
-- Descripción: Órdenes de compra a proveedores
-- Dependencias: proveedores, usuarios
-- Valores del enum EstadoOrden:
--   - BORRADOR, PENDIENTE, ENVIADA, CONFIRMADA, EN_TRANSITO,
--   - RECIBIDA_PARCIAL, RECIBIDA_COMPLETA, COMPLETADA, CANCELADA
-- ----------------------------------------------------------------------------
CREATE TABLE `ordenes_compra` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `numero_orden` VARCHAR(50) NOT NULL,
    `proveedor_id` BIGINT NOT NULL,
    `fecha_orden` DATE NOT NULL,
    `fecha_entrega_estimada` DATE DEFAULT NULL,
    `fecha_entrega_real` DATE DEFAULT NULL,
    `estado` VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    `usuario_comprador_id` BIGINT NOT NULL,
    `usuario_aprobador_id` BIGINT DEFAULT NULL,
    `fecha_aprobacion` DATETIME DEFAULT NULL,
    `usuario_receptor_id` BIGINT DEFAULT NULL,
    `fecha_recepcion` DATETIME DEFAULT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `porcentaje_impuesto` DECIMAL(5,2) DEFAULT 19.00,
    `monto_impuesto` DECIMAL(12,2) DEFAULT 0.00,
    `descuento` DECIMAL(12,2) DEFAULT 0.00,
    `total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `observaciones` VARCHAR(1000) DEFAULT NULL,
    `condiciones_pago` VARCHAR(200) DEFAULT NULL,
    `metodo_pago` VARCHAR(50) DEFAULT NULL,
    `direccion_entrega` VARCHAR(500) DEFAULT NULL,
    `referencia_proveedor` VARCHAR(100) DEFAULT NULL,
    `motivo_cancelacion` VARCHAR(500) DEFAULT NULL,
    `fecha_cancelacion` DATETIME DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL,
    `fecha_actualizacion` DATETIME DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orden_numero` (`numero_orden`),
    KEY `idx_numero_orden` (`numero_orden`),
    KEY `idx_proveedor` (`proveedor_id`),
    KEY `idx_estado` (`estado`),
    KEY `idx_fecha_orden` (`fecha_orden`),
    KEY `idx_usuario_comprador` (`usuario_comprador_id`),
    KEY `fk_orden_usuario_aprobador` (`usuario_aprobador_id`),
    KEY `fk_orden_usuario_receptor` (`usuario_receptor_id`),
    CONSTRAINT `fk_orden_proveedor`
        FOREIGN KEY (`proveedor_id`)
        REFERENCES `proveedores` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT `fk_orden_usuario_comprador`
        FOREIGN KEY (`usuario_comprador_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT `fk_orden_usuario_aprobador`
        FOREIGN KEY (`usuario_aprobador_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT `fk_orden_usuario_receptor`
        FOREIGN KEY (`usuario_receptor_id`)
        REFERENCES `usuarios` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Órdenes de compra a proveedores';

-- ----------------------------------------------------------------------------
-- Tabla: detalles_orden_compra
-- Descripción: Detalles/líneas de cada orden de compra
-- Dependencias: ordenes_compra, productos
-- ----------------------------------------------------------------------------
CREATE TABLE `detalles_orden_compra` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `orden_compra_id` BIGINT NOT NULL,
    `producto_id` BIGINT NOT NULL,
    `cantidad` INT NOT NULL,
    `precio_unitario` DECIMAL(10,2) NOT NULL,
    `porcentaje_descuento` DECIMAL(5,2) DEFAULT 0.00,
    `monto_descuento` DECIMAL(10,2) DEFAULT 0.00,
    `subtotal` DECIMAL(12,2) NOT NULL,
    `cantidad_recibida` INT NOT NULL DEFAULT 0,
    `observaciones` VARCHAR(500) DEFAULT NULL,
    `codigo_proveedor` VARCHAR(100) DEFAULT NULL,
    `nombre_proveedor` VARCHAR(200) DEFAULT NULL,
    `numero_linea` INT DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_orden_compra` (`orden_compra_id`),
    KEY `idx_producto_detalle` (`producto_id`),
    CONSTRAINT `fk_detalle_orden_compra`
        FOREIGN KEY (`orden_compra_id`)
        REFERENCES `ordenes_compra` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_detalle_producto`
        FOREIGN KEY (`producto_id`)
        REFERENCES `productos` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Detalles de las órdenes de compra';

-- ============================================================================
-- RESTAURACIÓN DE CONFIGURACIÓN
-- ============================================================================

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ============================================================================
-- FIN DEL SCHEMA
-- ============================================================================
-- NOTAS IMPORTANTES:
--
-- 1. ENTIDADES NO INCLUIDAS:
--    - EstadisticasNotificaciones: No es una entidad JPA (@Entity)
--    - Notificacion: No es una entidad JPA (@Entity)
--    - TipoNotificacion: Es un enum, no genera tabla
--
-- 2. ENUMS:
--    Los siguientes enums se almacenan como VARCHAR:
--    - MovimientoInventario.TipoMovimiento (max 30 chars)
--    - OrdenCompra.EstadoOrden (max 30 chars)
--    - Venta.EstadoVenta (max 25 chars)
--    - Venta.MetodoPago (max 20 chars)
--
-- 3. PRECISION DE DECIMALES:
--    - Precios y montos: DECIMAL(10,2) o DECIMAL(12,2)
--    - Porcentajes: DECIMAL(5,2)
--    - Campos heredados con DOUBLE se mantienen por compatibilidad
--
-- 4. FECHAS:
--    - LocalDateTime -> DATETIME
--    - LocalDate -> DATE
--    - Date (java.util.Date) -> TIMESTAMP
--
-- 5. BOOLEAN:
--    - boolean/Boolean -> TINYINT(1)
--    - Valores: 0 (false), 1 (true)
--
-- 6. CHARSET Y COLLATION:
--    - utf8mb4: Soporte completo para caracteres Unicode (incluyendo emojis)
--    - utf8mb4_unicode_ci: Comparación case-insensitive con soporte Unicode
--
-- 7. INDICES:
--    - Se incluyen todos los índices declarados en @Index
--    - Índices automáticos en foreign keys
--    - Índices en columnas UNIQUE
--
-- 8. INTEGRIDAD REFERENCIAL:
--    - ON DELETE CASCADE: Eliminación en cascada (detalles de ventas/órdenes)
--    - ON DELETE RESTRICT: Previene eliminación si hay referencias
--    - ON DELETE SET NULL: Establece NULL al eliminar (relaciones opcionales)
--    - ON UPDATE CASCADE: Actualización en cascada (todos los casos)
--
-- ============================================================================
