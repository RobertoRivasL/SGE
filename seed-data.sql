-- ============================================================================
-- SEED DATA - DATOS DE PRUEBA
-- ============================================================================
-- Proyecto: InformViva - Sistema de Gestión Empresarial
-- Propósito: Datos de prueba para desarrollo y testing
-- IMPORTANTE: Este script es SOLO para ambientes de desarrollo/testing
-- NO ejecutar en producción sin revisión previa
-- ============================================================================
-- Autor: Roberto Rivas
-- Fecha: 2025-11-09
-- Versión: 1.0
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;

-- ============================================================================
-- 1. CONFIGURACIÓN DEL SISTEMA
-- ============================================================================
-- Eliminar configuración existente para regenerar
TRUNCATE TABLE configuracion_sistema;

INSERT INTO configuracion_sistema (
    id, nombre_empresa, direccion_empresa, telefono_empresa, email_contacto,
    logo_url, color_primario, smtp_host, smtp_port, smtp_usuario, smtp_password,
    smtp_ssl_habilitado, dias_inactividad_alerta, habilitar_notificaciones,
    ultima_actualizacion, usuario_actualizacion
) VALUES (
    1,
    'InformViva Gestión SpA',
    'Av. Libertador Bernardo O''Higgins 1234, Santiago Centro',
    '+56 2 2345 6789',
    'contacto@informviva.cl',
    '/images/logo-informviva.png',
    '#0d6efd',
    'smtp.gmail.com',
    587,
    'noreply@informviva.cl',
    'smtp_password_encrypted',
    1,
    30,
    1,
    '2024-11-01 10:00:00',
    'admin'
);

-- ============================================================================
-- 2. CATEGORÍAS (8 categorías)
-- ============================================================================
TRUNCATE TABLE categorias;

INSERT INTO categorias (id, nombre, descripcion, activa) VALUES
(1, 'Electrónica', 'Productos electrónicos y dispositivos', 1),
(2, 'Computación', 'Equipos de computación y accesorios', 1),
(3, 'Oficina', 'Artículos y suministros de oficina', 1),
(4, 'Hogar', 'Productos para el hogar', 1),
(5, 'Deportes', 'Artículos deportivos y fitness', 1),
(6, 'Vestuario', 'Ropa y accesorios de vestir', 1),
(7, 'Alimentos', 'Productos alimenticios', 1),
(8, 'Bebidas', 'Bebidas y líquidos', 1);

-- ============================================================================
-- 3. ROLES Y PERMISOS
-- ============================================================================
TRUNCATE TABLE rol_permisos;
TRUNCATE TABLE roles;

INSERT INTO roles (id, nombre, descripcion) VALUES
(1, 'ADMIN', 'Administrador con acceso completo al sistema'),
(2, 'VENDEDOR', 'Vendedor con acceso limitado a ventas'),
(3, 'BODEGUERO', 'Encargado de bodega con acceso a inventario'),
(4, 'USUARIO', 'Usuario con permisos básicos de lectura');

-- Permisos para ADMIN
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(1, 'CREAR'),
(1, 'LEER'),
(1, 'ACTUALIZAR'),
(1, 'ELIMINAR'),
(1, 'EXPORTAR'),
(1, 'IMPORTAR');

-- Permisos para VENDEDOR
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(2, 'LEER'),
(2, 'CREAR');

-- Permisos para BODEGUERO
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(3, 'LEER'),
(3, 'CREAR'),
(3, 'ACTUALIZAR');

-- Permisos para USUARIO
INSERT INTO rol_permisos (rol_id, permiso) VALUES
(4, 'LEER');

-- ============================================================================
-- 4. USUARIOS (6 usuarios)
-- ============================================================================
-- Nota: Las contraseñas están hasheadas con BCrypt
-- Password original para todos: admin123 / vendedor123 / bodega123 / usuario123
TRUNCATE TABLE usuario_roles;
TRUNCATE TABLE usuarios;

INSERT INTO usuarios (id, username, password, nombre, apellido, email, activo, fecha_creacion, ultimo_acceso) VALUES
(1, 'admin@informviva.cl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Roberto', 'Rivas', 'admin@informviva.cl', 1, '2023-01-15 09:00:00', '2024-11-08 16:30:00'),
(2, 'vendedor@informviva.cl', '$2a$10$x5AZcJ6P.V6wlxIQN9ip4uwQrjrqYppVBgDEzyrHrpcGR6qUqN6S6', 'Carolina', 'Muñoz', 'vendedor@informviva.cl', 1, '2023-03-20 10:30:00', '2024-11-08 14:20:00'),
(3, 'bodega@informviva.cl', '$2a$10$E0s0JbA8.dZcWf0ygKz6pu1L6z6YQkpXc8j4C5s5M5M5M5M5M5M5M', 'Luis', 'González', 'bodega@informviva.cl', 1, '2023-05-10 11:00:00', '2024-11-08 09:15:00'),
(4, 'usuario@informviva.cl', '$2a$10$mE.qmcV7Xxty6z5Rxkvyxf22W0pyfJqmq2wF6l7b7M0sHIx8E7ssu', 'María', 'Silva', 'usuario@informviva.cl', 1, '2023-07-25 14:00:00', '2024-11-07 17:45:00'),
(5, 'roberto.rivas@informviva.cl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Roberto', 'Rivas', 'roberto.rivas@informviva.cl', 1, '2023-01-10 08:00:00', '2024-11-09 10:00:00'),
(6, 'patricia.lopez@informviva.cl', '$2a$10$x5AZcJ6P.V6wlxIQN9ip4uwQrjrqYppVBgDEzyrHrpcGR6qUqN6S6', 'Patricia', 'López', 'patricia.lopez@informviva.cl', 0, '2022-11-05 16:00:00', '2024-06-15 12:00:00');

-- Asignar roles a usuarios
INSERT INTO usuario_roles (usuario_id, rol) VALUES
(1, 'ADMIN'),
(2, 'VENDEDOR'),
(3, 'BODEGUERO'),
(4, 'USUARIO'),
(5, 'ADMIN'),
(6, 'VENDEDOR');

-- ============================================================================
-- 5. CLIENTES (15 clientes chilenos)
-- ============================================================================
TRUNCATE TABLE clientes;

INSERT INTO clientes (
    id, nombre, apellido, email, telefono, direccion, rut,
    fecha_registro, categoria, ciudad, activo, total_compras,
    numero_compras, fecha_ultima_compra, usuario_creacion
) VALUES
(1, 'Juan', 'Pérez', 'juan.perez@gmail.com', '+569 9876 5432', 'Los Leones 234, Providencia', '12345678-9', '2023-03-15 10:30:00', 'VIP', 'Santiago', 1, 1250000.00, 8, '2024-10-15 14:20:00', 'admin@informviva.cl'),
(2, 'María', 'González', 'maria.gonzalez@hotmail.com', '+569 8765 4321', 'Av. Apoquindo 4567, Las Condes', '23456789-0', '2023-06-20 11:15:00', 'Regular', 'Santiago', 1, 850000.00, 5, '2024-09-28 16:45:00', 'vendedor@informviva.cl'),
(3, 'Carlos', 'Rodríguez', 'carlos.rodriguez@outlook.com', '+569 7654 3210', 'Gran Avenida 8901, San Miguel', '34567890-1', '2023-08-10 09:00:00', 'Regular', 'Santiago', 1, 420000.00, 3, '2024-11-02 10:10:00', 'vendedor@informviva.cl'),
(4, 'Ana', 'Martínez', 'ana.martinez@yahoo.com', '+569 6543 2109', 'Av. Vicuña Mackenna 1234, Ñuñoa', '45678901-2', '2024-01-05 14:30:00', 'Nuevo', 'Santiago', 1, 180000.00, 2, '2024-10-20 11:30:00', 'vendedor@informviva.cl'),
(5, 'Pedro', 'Fernández', 'pedro.fernandez@gmail.com', '+569 5432 1098', 'Av. España 567, Valparaíso', '56789012-3', '2023-05-18 12:45:00', 'VIP', 'Valparaíso', 1, 2100000.00, 12, '2024-11-05 15:00:00', 'admin@informviva.cl'),
(6, 'Sofía', 'López', 'sofia.lopez@gmail.com', '+569 4321 0987', 'Calle Larga 890, Concepción', '67890123-4', '2023-09-22 10:20:00', 'Regular', 'Concepción', 1, 560000.00, 4, '2024-10-12 09:50:00', 'vendedor@informviva.cl'),
(7, 'Diego', 'Ramírez', 'diego.ramirez@hotmail.com', '+569 3210 9876', 'Av. Libertad 345, Viña del Mar', '78901234-5', '2024-02-14 16:00:00', 'Nuevo', 'Viña del Mar', 1, 95000.00, 1, '2024-08-30 13:20:00', 'vendedor@informviva.cl'),
(8, 'Valentina', 'Torres', 'valentina.torres@outlook.com', '+569 2109 8765', 'Paseo Bulnes 678, Santiago Centro', '89012345-6', '2023-07-30 11:30:00', 'Regular', 'Santiago', 1, 720000.00, 6, '2024-11-01 12:15:00', 'vendedor@informviva.cl'),
(9, 'Matías', 'Flores', 'matias.flores@gmail.com', '+569 1098 7654', 'Av. Irarrázaval 2345, Ñuñoa', '90123456-7', '2023-11-12 15:15:00', 'Regular', 'Santiago', 1, 340000.00, 3, '2024-09-18 10:40:00', 'vendedor@informviva.cl'),
(10, 'Camila', 'Morales', 'camila.morales@yahoo.com', '+569 0987 6543', 'Av. Santa María 4567, Providencia', '01234567-8', '2024-03-08 09:45:00', 'Nuevo', 'Santiago', 1, 225000.00, 2, '2024-10-25 14:55:00', 'vendedor@informviva.cl'),
(11, 'Benjamín', 'Castro', 'benjamin.castro@gmail.com', '+569 8876 5544', 'Los Carrera 123, Temuco', '11234567-9', '2023-04-25 13:00:00', 'Regular', 'Temuco', 1, 480000.00, 4, '2024-10-08 11:25:00', 'vendedor@informviva.cl'),
(12, 'Isidora', 'Vargas', 'isidora.vargas@hotmail.com', '+569 7765 4433', 'Av. Alemania 789, Puerto Montt', '22345678-0', '2023-12-05 10:50:00', 'Regular', 'Puerto Montt', 1, 615000.00, 5, '2024-11-03 16:30:00', 'vendedor@informviva.cl'),
(13, 'Martín', 'Reyes', 'martin.reyes@outlook.com', '+569 6654 3322', 'Calle Baquedano 456, Antofagasta', '33456789-1', '2024-01-20 14:20:00', 'Nuevo', 'Antofagasta', 1, 150000.00, 1, '2024-09-05 09:10:00', 'vendedor@informviva.cl'),
(14, 'Florencia', 'Pizarro', 'florencia.pizarro@gmail.com', '+569 5543 2211', 'Av. Brasil 2345, Valparaíso', '44567890-2', '2023-10-15 12:10:00', 'VIP', 'Valparaíso', 1, 1850000.00, 10, '2024-11-06 13:40:00', 'admin@informviva.cl'),
(15, 'Joaquín', 'Soto', 'joaquin.soto@yahoo.com', '+569 4432 1100', 'Av. Colón 678, La Serena', '55678901-3', '2022-08-30 09:30:00', 'Regular', 'La Serena', 0, 290000.00, 2, '2023-12-10 15:20:00', 'vendedor@informviva.cl');

-- ============================================================================
-- 6. PROVEEDORES (8 proveedores chilenos)
-- ============================================================================
TRUNCATE TABLE proveedores;

INSERT INTO proveedores (
    id, rut, nombre, nombre_fantasia, giro, direccion, ciudad, region, pais,
    telefono, email, contacto_nombre, contacto_cargo, contacto_telefono, contacto_email,
    condiciones_pago, dias_credito, activo, fecha_creacion, categoria, calificacion,
    usuario_creacion_id
) VALUES
(1, '76123456-7', 'Comercial Electrónica Total Ltda.', 'Electrónica Total', 'Venta de productos electrónicos', 'Av. Matta 1234, Santiago Centro', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2555 1234', 'ventas@electronicatotal.cl', 'Jorge Morales', 'Gerente Comercial', '+56 9 8765 4321', 'jorge.morales@electronicatotal.cl', '30 días neto', 30, 1, '2022-05-10 10:00:00', 'Electrónica', 5, 1),
(2, '76234567-8', 'Distribuidora Computech SpA', 'Computech', 'Distribución de equipos computacionales', 'Av. Vicuña Mackenna 5678, La Florida', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2666 5678', 'contacto@computech.cl', 'Andrea Vega', 'Jefa de Ventas', '+56 9 7654 3210', 'andrea.vega@computech.cl', '60 días fecha factura', 60, 1, '2022-03-15 11:30:00', 'Computación', 4, 1),
(3, '76345678-9', 'Suministros de Oficina MegaOffice SA', 'MegaOffice', 'Venta de artículos de oficina', 'Av. Apoquindo 2345, Las Condes', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2777 8901', 'ventas@megaoffice.cl', 'Roberto Pinto', 'Ejecutivo Comercial', '+56 9 6543 2109', 'roberto.pinto@megaoffice.cl', '30 días neto', 30, 1, '2022-07-20 09:15:00', 'Oficina', 5, 1),
(4, '76456789-0', 'Importadora del Hogar y Deco SA', 'Hogar Deco', 'Importación y venta de artículos para el hogar', 'Av. Irarrázaval 3456, Ñuñoa', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2888 2345', 'ventas@hogardeco.cl', 'Claudia Rojas', 'Gerente General', '+56 9 5432 1098', 'claudia.rojas@hogardeco.cl', '90 días fecha factura', 90, 1, '2022-09-05 14:45:00', 'Hogar', 4, 1),
(5, '76567890-1', 'Sport Fitness Chile Ltda.', 'Sport Fitness', 'Equipamiento deportivo y fitness', 'Av. Los Leones 567, Providencia', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2999 3456', 'contacto@sportfitness.cl', 'Fernando Lagos', 'Director Comercial', '+56 9 4321 0987', 'fernando.lagos@sportfitness.cl', 'Al contado', 0, 1, '2023-01-12 10:20:00', 'Deportes', 5, 1),
(6, '76678901-2', 'Textil y Vestuario Nacional SpA', 'TexVest', 'Fabricación y distribución de vestuario', 'Av. Santa Rosa 6789, La Cisterna', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2111 4567', 'ventas@texvest.cl', 'Mónica Herrera', 'Jefa de Ventas', '+56 9 3210 9876', 'monica.herrera@texvest.cl', '45 días neto', 45, 1, '2022-11-18 13:00:00', 'Vestuario', 3, 1),
(7, '76789012-3', 'Alimentos y Distribución Sur Ltda.', 'AliSur', 'Distribución de productos alimenticios', 'Av. General Velásquez 890, Concepción', 'Concepción', 'Región del Biobío', 'Chile', '+56 41 2222 5678', 'ventas@alisur.cl', 'Carlos Muñoz', 'Gerente de Zona', '+56 9 2109 8765', 'carlos.munoz@alisur.cl', '60 días fecha factura', 60, 1, '2023-02-28 11:45:00', 'Alimentos', 4, 1),
(8, '76890123-4', 'Bebidas y Licores Premium SA', 'Premium Drinks', 'Importación y distribución de bebidas', 'Av. Providencia 1234, Providencia', 'Santiago', 'Región Metropolitana', 'Chile', '+56 2 2333 6789', 'ventas@premiumdrinks.cl', 'Daniela Torres', 'Ejecutiva de Cuentas', '+56 9 1098 7654', 'daniela.torres@premiumdrinks.cl', '30 días neto', 30, 1, '2022-12-08 15:30:00', 'Bebidas', 5, 1);

-- ============================================================================
-- 7. PRODUCTOS (30 productos)
-- ============================================================================
TRUNCATE TABLE productos;

INSERT INTO productos (
    id, codigo, nombre, descripcion, precio, stock, stock_minimo,
    categoria_id, marca, modelo, activo, fecha_creacion
) VALUES
-- Electrónica (5 productos)
(1, 'PROD-0001', 'Smart TV LED 55 pulgadas', 'Televisor LED Full HD 55" con Smart TV', 389990.00, 15, 5, 1, 'Samsung', 'UN55T6500', 1, '2024-01-15 10:00:00'),
(2, 'PROD-0002', 'Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido', 79990.00, 42, 10, 1, 'Sony', 'WH-1000XM4', 1, '2024-01-20 11:30:00'),
(3, 'PROD-0003', 'Parlante Portátil Bluetooth', 'Parlante resistente al agua con 12h de batería', 49990.00, 28, 8, 1, 'JBL', 'Flip 5', 1, '2024-02-05 09:15:00'),
(4, 'PROD-0004', 'Microondas Digital 25L', 'Microondas con panel digital y grill', 89990.00, 8, 3, 1, 'LG', 'MS2535GIS', 1, '2024-02-10 14:20:00'),
(5, 'PROD-0005', 'Cámara de Seguridad WiFi', 'Cámara IP con visión nocturna y app móvil', 59990.00, 0, 5, 1, 'TP-Link', 'Tapo C200', 0, '2024-02-15 10:45:00'),

-- Computación (7 productos)
(6, 'PROD-0006', 'Notebook Core i5 8GB RAM', 'Laptop 14" Intel Core i5 8GB RAM 256GB SSD', 549990.00, 12, 5, 2, 'HP', 'Pavilion 14', 1, '2024-01-10 08:30:00'),
(7, 'PROD-0007', 'Mouse Inalámbrico', 'Mouse ergonómico con sensor óptico', 12990.00, 65, 15, 2, 'Logitech', 'M185', 1, '2024-01-12 09:00:00'),
(8, 'PROD-0008', 'Teclado Mecánico RGB', 'Teclado mecánico gaming con iluminación RGB', 89990.00, 18, 8, 2, 'Razer', 'BlackWidow', 1, '2024-01-25 11:00:00'),
(9, 'PROD-0009', 'Monitor LED 24 pulgadas', 'Monitor Full HD 24" IPS 75Hz', 159990.00, 10, 5, 2, 'LG', '24MK430H', 1, '2024-02-01 10:30:00'),
(10, 'PROD-0010', 'Disco Duro Externo 1TB', 'HDD externo portátil USB 3.0', 54990.00, 35, 10, 2, 'Western Digital', 'Elements', 1, '2024-02-08 13:45:00'),
(11, 'PROD-0011', 'Pendrive 64GB USB 3.0', 'Memoria USB 3.0 de alta velocidad', 9990.00, 120, 30, 2, 'Kingston', 'DataTraveler', 1, '2024-02-12 15:00:00'),
(12, 'PROD-0012', 'Webcam Full HD 1080p', 'Cámara web con micrófono integrado', 45990.00, 22, 8, 2, 'Logitech', 'C920', 1, '2024-02-20 09:30:00'),

-- Oficina (6 productos)
(13, 'PROD-0013', 'Resma Papel Carta 500 hojas', 'Papel bond blanco 75g/m² tamaño carta', 3990.00, 85, 20, 3, 'Chamex', 'A4', 1, '2024-01-05 08:00:00'),
(14, 'PROD-0014', 'Archivador Palanca Oficio', 'Archivador de palanca tamaño oficio', 2990.00, 54, 15, 3, 'Rhein', 'Classic', 1, '2024-01-08 09:15:00'),
(15, 'PROD-0015', 'Calculadora Científica', 'Calculadora científica 252 funciones', 19990.00, 30, 10, 3, 'Casio', 'FX-991', 1, '2024-01-15 10:45:00'),
(16, 'PROD-0016', 'Silla de Oficina Ergonómica', 'Silla giratoria con respaldo alto', 129990.00, 15, 5, 3, 'Spine', 'Executive', 1, '2024-01-22 11:30:00'),
(17, 'PROD-0017', 'Escritorio de Melamina', 'Escritorio 120x60cm con cajonera', 89990.00, 8, 3, 3, 'TuHome', 'Office Pro', 1, '2024-02-03 14:00:00'),
(18, 'PROD-0018', 'Pizarra Acrílica 60x90cm', 'Pizarra blanca con marco de aluminio', 24990.00, 12, 5, 3, 'Studmark', 'Pro Board', 1, '2024-02-15 10:00:00'),

-- Hogar (4 productos)
(19, 'PROD-0019', 'Aspiradora 1600W', 'Aspiradora de arrastre con bolsa', 69990.00, 10, 3, 4, 'Electrolux', 'Flex', 1, '2024-01-18 09:45:00'),
(20, 'PROD-0020', 'Juego de Sábanas 2 Plazas', 'Sábanas 100% algodón 144 hilos', 29990.00, 25, 8, 4, 'Cannon', 'Premium', 1, '2024-01-28 11:00:00'),
(21, 'PROD-0021', 'Set de Ollas Acero Inoxidable', 'Set 6 piezas con tapas de vidrio', 79990.00, 12, 5, 4, 'Oster', 'Cuisine', 1, '2024-02-10 13:30:00'),
(22, 'PROD-0022', 'Lámpara de Pie LED', 'Lámpara de pie moderna con regulador', 49990.00, 7, 3, 4, 'Philips', 'Hue', 1, '2024-02-18 15:15:00'),

-- Deportes (4 productos)
(23, 'PROD-0023', 'Pelota de Fútbol N°5', 'Pelota oficial tamaño 5 costura manual', 19990.00, 40, 10, 5, 'Nike', 'Pitch', 1, '2024-01-12 08:45:00'),
(24, 'PROD-0024', 'Colchoneta Yoga Premium', 'Mat antideslizante 6mm grosor', 24990.00, 35, 10, 5, 'Adidas', 'Yoga Mat', 1, '2024-01-20 10:15:00'),
(25, 'PROD-0025', 'Pesas Mancuernas 5kg', 'Par de mancuernas con recubrimiento', 34990.00, 18, 8, 5, 'Rebook', 'Hand Weights', 1, '2024-02-05 11:45:00'),
(26, 'PROD-0026', 'Cuerda para Saltar', 'Cuerda con contador digital', 12990.00, 50, 15, 5, 'Under Armour', 'Speed Rope', 1, '2024-02-12 09:00:00'),

-- Vestuario (3 productos)
(27, 'PROD-0027', 'Polera Deportiva Hombre', 'Polera Dry-Fit manga corta', 19990.00, 45, 15, 6, 'Nike', 'Dri-FIT', 1, '2024-01-25 10:30:00'),
(28, 'PROD-0028', 'Zapatillas Running', 'Zapatillas para correr amortiguación', 89990.00, 22, 10, 6, 'Adidas', 'UltraBoost', 1, '2024-02-08 14:00:00'),
(29, 'PROD-0029', 'Chaqueta Impermeable', 'Chaqueta cortaviento con capucha', 59990.00, 15, 6, 6, 'The North Face', 'Resolve', 1, '2024-02-15 11:30:00'),

-- Alimentos y Bebidas (1 producto)
(30, 'PROD-0030', 'Café Premium Molido 500g', 'Café grano selecto molido medio', 8990.00, 60, 20, 7, 'Juan Valdez', 'Colombian', 1, '2024-02-20 08:15:00');

-- ============================================================================
-- 8. VENTAS (15 ventas)
-- ============================================================================
TRUNCATE TABLE ventas;

INSERT INTO ventas (
    id, fecha, cliente_id, vendedor_id, subtotal, impuesto, total,
    metodo_pago, estado, numero_factura, descuento, observaciones,
    fecha_creacion, usuario_creacion
) VALUES
-- Noviembre 2024
(1, '2024-11-01 10:30:00', 8, 2, 143277.31, 27222.69, 170500.00, 'EFECTIVO', 'COMPLETADA', 'V-2024-0001', 0.00, 'Venta al contado', '2024-11-01 10:30:00', 'vendedor@informviva.cl'),
(2, '2024-11-02 14:15:00', 3, 2, 117647.06, 22352.94, 140000.00, 'TARJETA_CREDITO', 'COMPLETADA', 'V-2024-0002', 0.00, 'Pago con tarjeta Visa', '2024-11-02 14:15:00', 'vendedor@informviva.cl'),
(3, '2024-11-03 11:20:00', 12, 2, 92436.97, 17563.03, 110000.00, 'TRANSFERENCIA', 'COMPLETADA', 'V-2024-0003', 0.00, 'Transferencia Banco Chile', '2024-11-03 11:20:00', 'vendedor@informviva.cl'),

-- Octubre 2024
(4, '2024-10-05 09:45:00', 1, 2, 546218.49, 103781.51, 650000.00, 'TRANSFERENCIA', 'COMPLETADA', 'V-2024-0004', 0.00, 'Compra corporativa', '2024-10-05 09:45:00', 'vendedor@informviva.cl'),
(5, '2024-10-12 15:30:00', 6, 6, 193277.31, 36722.69, 230000.00, 'TARJETA_CREDITO', 'COMPLETADA', 'V-2024-0005', 0.00, NULL, '2024-10-12 15:30:00', 'patricia.lopez@informviva.cl'),
(6, '2024-10-15 10:00:00', 1, 2, 134453.78, 25546.22, 160000.00, 'EFECTIVO', 'COMPLETADA', 'V-2024-0006', 0.00, NULL, '2024-10-15 10:00:00', 'vendedor@informviva.cl'),
(7, '2024-10-20 13:45:00', 4, 2, 84033.61, 15966.39, 100000.00, 'TRANSFERENCIA', 'COMPLETADA', 'V-2024-0007', 0.00, NULL, '2024-10-20 13:45:00', 'vendedor@informviva.cl'),
(8, '2024-10-25 16:00:00', 10, 2, 92436.97, 17563.03, 110000.00, 'TARJETA_CREDITO', 'COMPLETADA', 'V-2024-0008', 0.00, 'Cliente nuevo', '2024-10-25 16:00:00', 'vendedor@informviva.cl'),

-- Septiembre 2024
(9, '2024-09-05 11:30:00', 13, 2, 126050.42, 23949.58, 150000.00, 'EFECTIVO', 'COMPLETADA', 'V-2024-0009', 0.00, NULL, '2024-09-05 11:30:00', 'vendedor@informviva.cl'),
(10, '2024-09-18 14:20:00', 9, 6, 142857.14, 27142.86, 170000.00, 'TRANSFERENCIA', 'COMPLETADA', 'V-2024-0010', 0.00, NULL, '2024-09-18 14:20:00', 'patricia.lopez@informviva.cl'),
(11, '2024-09-28 10:15:00', 2, 2, 294117.65, 55882.35, 350000.00, 'TARJETA_CREDITO', 'COMPLETADA', 'V-2024-0011', 0.00, 'Pago en 3 cuotas', '2024-09-28 10:15:00', 'vendedor@informviva.cl'),

-- Agosto 2024
(12, '2024-08-10 15:45:00', 11, 2, 201680.67, 38319.33, 240000.00, 'TRANSFERENCIA', 'COMPLETADA', 'V-2024-0012', 0.00, NULL, '2024-08-10 15:45:00', 'vendedor@informviva.cl'),
(13, '2024-08-30 12:00:00', 7, 2, 79831.93, 15168.07, 95000.00, 'EFECTIVO', 'COMPLETADA', 'V-2024-0013', 0.00, NULL, '2024-08-30 12:00:00', 'vendedor@informviva.cl'),

-- Ventas pendientes/anuladas
(14, '2024-11-07 09:00:00', 5, 2, 420168.07, 79831.93, 500000.00, 'CREDITO', 'PENDIENTE', 'V-2024-0014', 0.00, 'Pendiente de pago 30 días', '2024-11-07 09:00:00', 'vendedor@informviva.cl'),
(15, '2024-11-08 16:30:00', 14, 2, 251260.50, 47739.50, 299000.00, 'TRANSFERENCIA', 'ANULADA', 'V-2024-0015', 0.00, 'Venta anulada por cliente', '2024-11-08 16:30:00', 'vendedor@informviva.cl');

-- ============================================================================
-- 9. DETALLES DE VENTA
-- ============================================================================
TRUNCATE TABLE venta_detalle;

INSERT INTO venta_detalle (id, venta_id, producto_id, cantidad, precio_unitario) VALUES
-- Venta 1 (V-2024-0001) - Total: 170500
(1, 1, 6, 1, 549990.00),   -- Notebook
(2, 1, 7, 2, 12990.00),    -- Mouse x2
(3, 1, 13, 3, 3990.00),    -- Resma papel x3

-- Venta 2 (V-2024-0002) - Total: 140000
(4, 2, 9, 1, 159990.00),   -- Monitor
(5, 2, 14, 5, 2990.00),    -- Archivador x5
(6, 2, 11, 2, 9990.00),    -- Pendrive x2

-- Venta 3 (V-2024-0003) - Total: 110000
(7, 3, 21, 1, 79990.00),   -- Set ollas
(8, 3, 20, 1, 29990.00),   -- Sábanas

-- Venta 4 (V-2024-0004) - Total: 650000
(9, 4, 1, 1, 389990.00),   -- Smart TV
(10, 4, 8, 2, 89990.00),   -- Teclado RGB x2
(11, 4, 12, 2, 45990.00),  -- Webcam x2

-- Venta 5 (V-2024-0005) - Total: 230000
(12, 5, 16, 1, 129990.00), -- Silla oficina
(13, 5, 18, 2, 24990.00),  -- Pizarra x2
(14, 5, 15, 2, 19990.00),  -- Calculadora x2

-- Venta 6 (V-2024-0006) - Total: 160000
(15, 6, 10, 2, 54990.00),  -- Disco duro x2
(16, 6, 2, 1, 79990.00),   -- Auriculares

-- Venta 7 (V-2024-0007) - Total: 100000
(17, 7, 3, 2, 49990.00),   -- Parlante x2

-- Venta 8 (V-2024-0008) - Total: 110000
(18, 8, 28, 1, 89990.00),  -- Zapatillas
(19, 8, 27, 1, 19990.00),  -- Polera

-- Venta 9 (V-2024-0009) - Total: 150000
(20, 9, 4, 1, 89990.00),   -- Microondas
(21, 9, 29, 1, 59990.00),  -- Chaqueta

-- Venta 10 (V-2024-0010) - Total: 170000
(22, 10, 17, 1, 89990.00), -- Escritorio
(23, 10, 2, 1, 79990.00),  -- Auriculares

-- Venta 11 (V-2024-0011) - Total: 350000
(24, 11, 6, 1, 549990.00), -- Notebook

-- Venta 12 (V-2024-0012) - Total: 240000
(25, 12, 19, 2, 69990.00), -- Aspiradora x2
(26, 12, 21, 1, 79990.00), -- Set ollas

-- Venta 13 (V-2024-0013) - Total: 95000
(27, 13, 24, 2, 24990.00), -- Colchoneta yoga x2
(28, 13, 25, 1, 34990.00), -- Pesas

-- Venta 14 (V-2024-0014) - Total: 500000 (PENDIENTE)
(29, 14, 1, 1, 389990.00), -- Smart TV
(30, 14, 9, 1, 159990.00), -- Monitor

-- Venta 15 (V-2024-0015) - Total: 299000 (ANULADA)
(31, 15, 6, 1, 549990.00); -- Notebook

-- ============================================================================
-- 10. ÓRDENES DE COMPRA (8 órdenes)
-- ============================================================================
TRUNCATE TABLE ordenes_compra;

INSERT INTO ordenes_compra (
    id, numero_orden, proveedor_id, fecha_orden, fecha_entrega_estimada, fecha_entrega_real,
    estado, usuario_comprador_id, usuario_aprobador_id, usuario_receptor_id,
    fecha_aprobacion, fecha_recepcion, subtotal, porcentaje_impuesto, monto_impuesto,
    descuento, total, observaciones, condiciones_pago, metodo_pago,
    fecha_creacion
) VALUES
-- Órdenes completadas
(1, 'OC-2024-0001', 1, '2024-10-01', '2024-10-15', '2024-10-14', 'RECIBIDA_COMPLETA', 3, 1, 3, '2024-10-01 14:00:00', '2024-10-14 10:30:00', 2499950.00, 19.00, 474990.50, 0.00, 2974940.50, 'Pedido urgente', '30 días neto', 'TRANSFERENCIA', '2024-10-01 09:00:00'),
(2, 'OC-2024-0002', 2, '2024-10-05', '2024-10-20', '2024-10-18', 'RECIBIDA_COMPLETA', 3, 1, 3, '2024-10-05 11:00:00', '2024-10-18 15:20:00', 3299940.00, 19.00, 626988.60, 100000.00, 3826928.60, NULL, '60 días fecha factura', 'TRANSFERENCIA', '2024-10-05 08:30:00'),
(3, 'OC-2024-0003', 3, '2024-10-10', '2024-10-25', '2024-10-23', 'RECIBIDA_COMPLETA', 3, 1, 3, '2024-10-10 10:00:00', '2024-10-23 11:45:00', 458720.00, 19.00, 87156.80, 0.00, 545876.80, 'Insumos mensuales oficina', '30 días neto', 'TRANSFERENCIA', '2024-10-10 09:15:00'),
(4, 'OC-2024-0004', 5, '2024-10-15', '2024-11-01', '2024-10-30', 'RECIBIDA_COMPLETA', 3, 1, 3, '2024-10-15 13:00:00', '2024-10-30 09:00:00', 379960.00, 19.00, 72192.40, 0.00, 452152.40, NULL, 'Al contado', 'TRANSFERENCIA', '2024-10-15 10:00:00'),

-- Órdenes en tránsito/enviadas
(5, 'OC-2024-0005', 4, '2024-11-01', '2024-11-20', NULL, 'EN_TRANSITO', 3, 1, NULL, '2024-11-01 15:00:00', NULL, 549980.00, 19.00, 104496.20, 0.00, 654476.20, 'Mercadería en camino', '90 días fecha factura', 'TRANSFERENCIA', '2024-11-01 11:00:00'),
(6, 'OC-2024-0006', 8, '2024-11-03', '2024-11-18', NULL, 'ENVIADA', 3, 1, NULL, '2024-11-03 12:00:00', NULL, 299700.00, 19.00, 56943.00, 0.00, 356643.00, NULL, '30 días neto', 'TRANSFERENCIA', '2024-11-03 09:30:00'),

-- Órdenes pendientes
(7, 'OC-2024-0007', 6, '2024-11-06', '2024-11-25', NULL, 'PENDIENTE', 3, 1, NULL, '2024-11-06 16:00:00', NULL, 899730.00, 19.00, 170948.70, 50000.00, 1020678.70, 'Pedido temporada', '45 días neto', 'TRANSFERENCIA', '2024-11-06 14:00:00'),
(8, 'OC-2024-0008', 7, '2024-11-08', '2024-11-22', NULL, 'PENDIENTE', 3, NULL, NULL, NULL, NULL, 269700.00, 19.00, 51243.00, 0.00, 320943.00, 'Pendiente aprobación', '60 días fecha factura', 'TRANSFERENCIA', '2024-11-08 10:00:00');

-- ============================================================================
-- 11. DETALLES DE ORDEN DE COMPRA
-- ============================================================================
TRUNCATE TABLE detalles_orden_compra;

INSERT INTO detalles_orden_compra (
    id, orden_compra_id, producto_id, cantidad, precio_unitario,
    porcentaje_descuento, monto_descuento, subtotal, cantidad_recibida, numero_linea
) VALUES
-- OC-2024-0001 (Electrónica Total)
(1, 1, 1, 5, 320000.00, 0.00, 0.00, 1600000.00, 5, 1),
(2, 1, 2, 10, 65000.00, 0.00, 0.00, 650000.00, 10, 2),
(3, 1, 3, 5, 35000.00, 0.00, 0.00, 175000.00, 5, 3),
(4, 1, 4, 3, 75000.00, 10.00, 22500.00, 202500.00, 3, 4),

-- OC-2024-0002 (Computech)
(5, 2, 6, 6, 480000.00, 5.00, 144000.00, 2736000.00, 6, 1),
(6, 2, 9, 4, 140000.00, 0.00, 0.00, 560000.00, 4, 2),
(7, 2, 12, 2, 38000.00, 0.00, 0.00, 76000.00, 2, 3),

-- OC-2024-0003 (MegaOffice)
(8, 3, 13, 100, 2800.00, 0.00, 0.00, 280000.00, 100, 1),
(9, 3, 14, 50, 2100.00, 0.00, 0.00, 105000.00, 50, 2),
(10, 3, 15, 3, 16500.00, 0.00, 0.00, 49500.00, 3, 3),
(11, 3, 18, 2, 18000.00, 0.00, 0.00, 36000.00, 2, 4),

-- OC-2024-0004 (Sport Fitness)
(12, 4, 23, 20, 14000.00, 0.00, 0.00, 280000.00, 20, 1),
(13, 4, 24, 15, 18000.00, 0.00, 0.00, 270000.00, 15, 2),
(14, 4, 25, 4, 28000.00, 0.00, 0.00, 112000.00, 4, 3),
(15, 4, 26, 10, 9500.00, 0.00, 0.00, 95000.00, 10, 4),

-- OC-2024-0005 (Hogar Deco) - EN TRÁNSITO
(16, 5, 19, 8, 58000.00, 0.00, 0.00, 464000.00, 0, 1),
(17, 5, 20, 10, 22000.00, 0.00, 0.00, 220000.00, 0, 2),
(18, 5, 21, 5, 68000.00, 0.00, 0.00, 340000.00, 0, 3),

-- OC-2024-0006 (Premium Drinks) - ENVIADA
(19, 6, 30, 50, 5990.00, 0.00, 0.00, 299500.00, 0, 1),

-- OC-2024-0007 (TexVest) - PENDIENTE
(20, 7, 27, 50, 14000.00, 0.00, 0.00, 700000.00, 0, 1),
(21, 7, 28, 15, 72000.00, 0.00, 0.00, 1080000.00, 0, 2),
(22, 7, 29, 10, 48000.00, 0.00, 0.00, 480000.00, 0, 3),

-- OC-2024-0008 (AliSur) - PENDIENTE SIN APROBAR
(23, 8, 30, 30, 5990.00, 0.00, 0.00, 179700.00, 0, 1);

-- ============================================================================
-- 12. MOVIMIENTOS DE INVENTARIO (20 movimientos)
-- ============================================================================
TRUNCATE TABLE movimientos_inventario;

INSERT INTO movimientos_inventario (
    id, producto_id, tipo, cantidad, stock_anterior, stock_nuevo,
    motivo, referencia_externa, usuario_id, fecha, observaciones,
    costo_unitario, costo_total
) VALUES
-- Movimientos de COMPRA (Octubre 2024)
(1, 1, 'COMPRA', 5, 10, 15, 'Recepción OC-2024-0001', 'OC-2024-0001', 3, '2024-10-14 10:30:00', 'Mercadería recibida completa', 320000.00, 1600000.00),
(2, 2, 'COMPRA', 10, 32, 42, 'Recepción OC-2024-0001', 'OC-2024-0001', 3, '2024-10-14 10:35:00', NULL, 65000.00, 650000.00),
(3, 3, 'COMPRA', 5, 23, 28, 'Recepción OC-2024-0001', 'OC-2024-0001', 3, '2024-10-14 10:40:00', NULL, 35000.00, 175000.00),
(4, 6, 'COMPRA', 6, 6, 12, 'Recepción OC-2024-0002', 'OC-2024-0002', 3, '2024-10-18 15:20:00', NULL, 480000.00, 2880000.00),
(5, 13, 'COMPRA', 100, 5, 105, 'Recepción OC-2024-0003', 'OC-2024-0003', 3, '2024-10-23 11:45:00', 'Stock papel mensual', 2800.00, 280000.00),

-- Movimientos de VENTA (Octubre-Noviembre 2024)
(6, 6, 'VENTA', 1, 12, 11, 'Venta V-2024-0004', 'V-2024-0004', 2, '2024-10-05 09:45:00', NULL, 480000.00, 480000.00),
(7, 1, 'VENTA', 1, 15, 14, 'Venta V-2024-0004', 'V-2024-0004', 2, '2024-10-05 09:45:00', NULL, 320000.00, 320000.00),
(8, 9, 'VENTA', 1, 11, 10, 'Venta V-2024-0002', 'V-2024-0002', 2, '2024-11-02 14:15:00', NULL, 140000.00, 140000.00),
(9, 6, 'VENTA', 1, 11, 10, 'Venta V-2024-0011', 'V-2024-0011', 2, '2024-09-28 10:15:00', NULL, 480000.00, 480000.00),

-- Movimientos de AJUSTE
(10, 13, 'AJUSTE_NEGATIVO', 20, 105, 85, 'Ajuste por inventario físico', 'INV-2024-10', 3, '2024-10-31 17:00:00', 'Diferencia inventario mensual', 2800.00, 56000.00),
(11, 7, 'AJUSTE_POSITIVO', 5, 60, 65, 'Productos encontrados en bodega', 'INV-2024-10', 3, '2024-10-31 17:15:00', NULL, 0.00, 0.00),
(12, 11, 'AJUSTE_NEGATIVO', 8, 128, 120, 'Productos dañados', 'MERMA-2024-10', 3, '2024-10-25 16:30:00', 'Pendrives defectuosos', 8000.00, 64000.00),

-- Más movimientos de VENTA recientes
(13, 21, 'VENTA', 1, 13, 12, 'Venta V-2024-0003', 'V-2024-0003', 2, '2024-11-03 11:20:00', NULL, 68000.00, 68000.00),
(14, 16, 'VENTA', 1, 16, 15, 'Venta V-2024-0005', 'V-2024-0005', 6, '2024-10-12 15:30:00', NULL, 110000.00, 110000.00),
(15, 2, 'VENTA', 1, 42, 41, 'Venta V-2024-0006', 'V-2024-0006', 2, '2024-10-15 10:00:00', NULL, 65000.00, 65000.00),

-- Movimientos de COMPRA recientes
(16, 23, 'COMPRA', 20, 20, 40, 'Recepción OC-2024-0004', 'OC-2024-0004', 3, '2024-10-30 09:00:00', NULL, 14000.00, 280000.00),
(17, 24, 'COMPRA', 15, 20, 35, 'Recepción OC-2024-0004', 'OC-2024-0004', 3, '2024-10-30 09:10:00', NULL, 18000.00, 270000.00),

-- Movimientos adicionales
(18, 10, 'VENTA', 2, 37, 35, 'Venta V-2024-0006', 'V-2024-0006', 2, '2024-10-15 10:00:00', NULL, 48000.00, 96000.00),
(19, 28, 'VENTA', 1, 23, 22, 'Venta V-2024-0008', 'V-2024-0008', 2, '2024-10-25 16:00:00', NULL, 72000.00, 72000.00),
(20, 19, 'COMPRA', 10, 0, 10, 'Recepción inicial', 'INICIAL', 3, '2024-10-01 08:00:00', 'Stock inicial', 58000.00, 580000.00);

-- ============================================================================
-- 13. HISTORIAL DE IMPORTACIÓN (5 registros)
-- ============================================================================
TRUNCATE TABLE importacion_historial;

INSERT INTO importacion_historial (
    id, tipo_importacion, nombre_archivo, fecha_importacion, usuario_id,
    total_registros, registros_exitosos, registros_con_error,
    tiempo_procesamiento_ms, exitoso, resumen, errores, advertencias
) VALUES
(1, 'CLIENTES', 'clientes_importacion_2024_01.xlsx', '2024-01-15 10:30:00', 1, 50, 48, 2, 2340, 1, 'Importación exitosa. 48 clientes creados, 2 con errores de validación.', '[{"fila":23,"error":"RUT inválido"},{"fila":45,"error":"Email duplicado"}]', NULL),
(2, 'PRODUCTOS', 'productos_catalogo_2024_02.csv', '2024-02-20 14:15:00', 1, 120, 115, 5, 4560, 1, 'Importación de catálogo completa. 115 productos importados correctamente.', '[{"fila":34,"error":"Categoría no existe"},{"fila":67,"error":"SKU duplicado"},{"fila":89,"error":"Precio inválido"},{"fila":102,"error":"Stock negativo"},{"fila":118,"error":"Campos requeridos faltantes"}]', '[{"fila":45,"advertencia":"Stock bajo"}]'),
(3, 'CLIENTES', 'clientes_actualizacion_marzo.xlsx', '2024-03-10 09:00:00', 1, 30, 30, 0, 1890, 1, 'Actualización exitosa de datos de clientes existentes.', NULL, '[{"fila":12,"advertencia":"Cliente inactivo actualizado"}]'),
(4, 'PRODUCTOS', 'productos_fallido.csv', '2024-04-05 11:20:00', 1, 80, 35, 45, 3240, 0, 'Importación con múltiples errores. Archivo con formato incorrecto.', '[{"error":"Formato de archivo no compatible"},{"error":"Columnas faltantes: precio, stock"}]', NULL),
(5, 'CLIENTES', 'clientes_octubre_2024.xlsx', '2024-10-15 16:45:00', 5, 25, 25, 0, 1560, 1, 'Importación completada exitosamente. 25 clientes nuevos agregados.', NULL, NULL);

-- ============================================================================
-- 14. HISTORIAL DE EXPORTACIÓN (5 registros)
-- ============================================================================
TRUNCATE TABLE exportacion_historial;

INSERT INTO exportacion_historial (
    id, tipo_exportacion, formato, usuario_solicitante, fecha_solicitud,
    fecha_inicio_datos, fecha_fin_datos, tamano_archivo, estado,
    mensaje_error, numero_registros, tiempo_procesamiento, ruta_archivo
) VALUES
(1, 'Ventas', 'Excel', 'admin@informviva.cl', '2024-10-01 10:00:00', '2024-09-01', '2024-09-30', 245678, 'Completado', NULL, 45, 2340, '/exports/ventas_septiembre_2024.xlsx'),
(2, 'Clientes', 'PDF', 'vendedor@informviva.cl', '2024-10-15 14:30:00', NULL, NULL, 189234, 'Completado', NULL, 120, 1890, '/exports/clientes_listado_2024.pdf'),
(3, 'Productos', 'CSV', 'bodega@informviva.cl', '2024-10-20 09:15:00', NULL, NULL, 156890, 'Completado', NULL, 250, 1560, '/exports/inventario_productos.csv'),
(4, 'Reportes', 'PDF', 'roberto.rivas@informviva.cl', '2024-11-01 11:00:00', '2024-10-01', '2024-10-31', 0, 'Error', 'Error al generar gráficos de reporte', 0, 890, NULL),
(5, 'Ventas', 'Excel', 'admin@informviva.cl', '2024-11-08 15:30:00', '2024-10-01', '2024-10-31', 312456, 'Completado', NULL, 52, 2670, '/exports/ventas_octubre_2024.xlsx');

-- ============================================================================
-- FINALIZACIÓN
-- ============================================================================

SET FOREIGN_KEY_CHECKS=1;
COMMIT;

-- ============================================================================
-- RESUMEN DE DATOS INSERTADOS
-- ============================================================================
-- Configuración Sistema: 1 registro
-- Categorías: 8 registros
-- Roles: 4 registros
-- Permisos: 15 registros (distribuidos entre roles)
-- Usuarios: 6 registros
-- Clientes: 15 registros
-- Proveedores: 8 registros
-- Productos: 30 registros
-- Ventas: 15 registros
-- Detalles de Venta: 31 registros
-- Órdenes de Compra: 8 registros
-- Detalles de Orden de Compra: 23 registros
-- Movimientos de Inventario: 20 registros
-- Importación Historial: 5 registros
-- Exportación Historial: 5 registros
-- ============================================================================
-- TOTAL: 194 registros insertados
-- ============================================================================

-- ============================================================================
-- NOTAS IMPORTANTES:
-- ============================================================================
-- 1. Las contraseñas están hasheadas con BCrypt (algoritmo $2a$10)
--    - Password para admin123: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--    - Password para vendedor123: $2a$10$x5AZcJ6P.V6wlxIQN9ip4uwQrjrqYppVBgDEzyrHrpcGR6qUqN6S6
--    - Estas son contraseñas de PRUEBA, cambiar en producción
--
-- 2. RUTs chilenos incluidos son ejemplos genéricos (12345678-9, etc.)
--    - En producción usar RUTs reales y validados
--
-- 3. Cálculos de ventas:
--    - IVA: 19% del subtotal
--    - Total: Subtotal + IVA - Descuento
--
-- 4. Stock de productos actualizado según movimientos:
--    - Considerar que algunos productos tienen stock 0 (ej: PROD-0005)
--    - Productos inactivos marcados con activo=0
--
-- 5. Fechas:
--    - Todas las fechas son del período 2022-2024
--    - Formato MySQL: 'YYYY-MM-DD HH:MM:SS'
--
-- 6. Este script está diseñado para MySQL/MariaDB
--    - Ajustar si se usa otro motor de base de datos
--
-- 7. Ejecutar este script SOLO en ambientes de desarrollo/testing
--    - NO ejecutar en producción sin revisión y autorización
-- ============================================================================
