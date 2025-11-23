-- =======================================================
-- TPI LOGÍSTICA - DATOS INICIALES COMPLETOS
-- =======================================================

-- =======================
-- PROVINCIAS Y CIUDADES
-- =======================
INSERT INTO provincias (nombre)
VALUES ('Córdoba'), ('Buenos Aires'), ('Santa Fe'), ('Mendoza'), ('Salta')
ON CONFLICT DO NOTHING;

INSERT INTO ciudades (nombre, codigo_postal, provincia_id)
VALUES 
('Córdoba Capital', '5000', 1),
('Villa María', '5900', 1),
('La Plata', '1900', 2),
('Mar del Plata', '7600', 2),
('Rosario', '2000', 3),
('Santa Fe Capital', '3000', 3),
('Mendoza Capital', '5500', 4),
('Salta Capital', '4400', 5)
ON CONFLICT DO NOTHING;

-- =======================
-- CLIENTES
-- =======================
INSERT INTO clientes (dni, nombre, apellido, email, telefono, domicilio, keycloak_id, ciudad_id)
VALUES 
(40111222, 'Lucas', 'Pérez', 'lucas.perez@example.com', '3513332222', 'Av. Colón 1000', 'key-lucas', 1),
(39222333, 'Martina', 'Gómez', 'martina.gomez@example.com', '1122334455', 'Diag. 74 Nº 450', 'key-martina', 3),
(38888111, 'Roberto', 'Alonso', 'roberto.alonso@example.com', '3415557788', 'San Martín 850', 'key-roberto', 5),
(37777999, 'Carla', 'Funes', 'carla.funes@example.com', '3819998888', 'Av. Belgrano 100', 'key-carla', 8)
ON CONFLICT DO NOTHING;

-- =======================
-- CONTENEDORES
-- =======================
INSERT INTO contenedores (codigo, peso, volumen, cliente_id)
VALUES
('CONT-A1', 1000.0, 2.5, 1),
('CONT-A2', 800.0, 1.8, 1),
('CONT-B1', 750.0, 1.5, 2),
('CONT-C1', 1200.0, 3.0, 3),
('CONT-C2', 950.0, 2.2, 3),
('CONT-D1', 1100.0, 2.7, 4)
ON CONFLICT DO NOTHING;

-- =======================
-- TRANSPORTISTAS Y ADMINISTRADORES
-- =======================
INSERT INTO transportistas (dni, nombre, apellido, email, telefono, domicilio, keycloak_id, ciudad)
VALUES 
(40888222, 'Juan', 'Pérez', 'juan.perez@flota.com', '3513334444', 'Av. Los Transportes 101', 'key-juan', 'Córdoba'),
(39999111, 'Sofía', 'Ruiz', 'sofia.ruiz@flota.com', '1122233344', 'Ruta 8 Km 12', 'key-sofia', 'Rosario'),
(37777111, 'Andrés', 'Torres', 'andres.torres@flota.com', '2615552222', 'Bv. San Martín 122', 'key-andres', 'Mendoza')
ON CONFLICT DO NOTHING;

INSERT INTO administradores (dni, nombre, apellido, email, telefono, domicilio, keycloak_id, ciudad)
VALUES 
(32111222, 'Carlos', 'López', 'carlos.lopez@admin.com', '3511112222', 'Av. Central 500', 'key-carlos', 'Córdoba'),
(31222999, 'Laura', 'Martínez', 'laura.martinez@admin.com', '1122445566', 'Calle Mitre 123', 'key-laura', 'Buenos Aires')
ON CONFLICT DO NOTHING;

-- =======================
-- CAMIONES
-- =======================
INSERT INTO camiones (dominio, capacidad_peso, capacidad_volumen, disponibilidad, costo_km_base, consumo_litro_km, transportista_id)
VALUES
('AA123BB', 12000.0, 25.0, TRUE, 80.0, 0.3, 1),
('AB987CD', 9000.0, 20.0, TRUE, 70.0, 0.25, 1),
('AC555EE', 15000.0, 30.0, TRUE, 100.0, 0.35, 2),
('AD666FF', 10000.0, 22.0, FALSE, 90.0, 0.28, 3)
ON CONFLICT DO NOTHING;

-- =======================
-- DEPÓSITOS
-- =======================
INSERT INTO depositos (nombre, direccion, latitud, longitud, costo_estadia_diaria, estado, capacidad_maxima, cantidad_ocupada)
VALUES
('Depósito Córdoba', 'Av. Circunvalación 3500', -31.4201, -64.1888, 2500.0, TRUE, 100, 0),
('Depósito Rosario', 'Bv. Oroño 2500', -32.9442, -60.6505, 2300.0, TRUE, 80, 0),
('Depósito Buenos Aires', 'Panamericana Km 45', -34.6037, -58.3816, 2700.0, TRUE, 120, 0),
('Depósito Mendoza', 'Ruta 40 Km 3200', -32.8897, -68.8458, 2400.0, TRUE, 90, 0),
('Depósito Salta', 'Av. Bolivia 2000', -24.7829, -65.4232, 2200.0, TRUE, 70, 0)
ON CONFLICT DO NOTHING;

-- =======================
-- PARÁMETROS Y TARIFAS
-- =======================
INSERT INTO parametros_tarifa (precio_litro_combustible, cargo_fijo_tramo)
VALUES (1300.00, 2500.00)
ON CONFLICT DO NOTHING;

INSERT INTO tarifas_rango_volumen_peso (volumen_min, volumen_max, peso_min, peso_max, costo_km_base)
VALUES 
(0, 10, 0, 5000, 70),
(10.01, 20, 5000.01, 10000, 90),
(20.01, 40, 10000.01, 20000, 110)
ON CONFLICT DO NOTHING;

-- =======================
-- SOLICITUDES
-- =======================
INSERT INTO solicitudes (cliente_id, contenedor_id, fecha_solicitud, origen, destino, distancia_km, costo_estimado, tiempo_estimado_min, estado_actual, observaciones)
VALUES
(1, 1, NOW() - INTERVAL '4 days', 'Córdoba Capital', 'Rosario', 400.0, 38500.0, 343, 'EN_TRANSITO', 'Transporte de prueba'),
(2, 3, NOW() - INTERVAL '3 days', 'La Plata', 'Córdoba Capital', 700.0, 69000.0, 600, 'ASIGNADA', 'Carga urgente'),
(3, 4, NOW() - INTERVAL '1 days', 'Rosario', 'Mendoza', 750.0, 74000.0, 643, 'EN_TRANSITO', 'Material frágil'),
(4, 6, NOW(), 'Salta Capital', 'Córdoba Capital', 900.0, 88000.0, 771, 'PENDIENTE', 'Carga estándar')
ON CONFLICT DO NOTHING;

-- =======================
-- RUTAS
-- =======================
INSERT INTO rutas (solicitud_id, fecha_inicio, fecha_fin_estimada, distancia_total_km, costo_total)
VALUES 
(1, NOW() - INTERVAL '3 days', NOW() + INTERVAL '5 hours', 400.0, 38500.0),
(2, NOW() - INTERVAL '2 days', NOW() + INTERVAL '1 days', 700.0, 69000.0),
(3, NOW() - INTERVAL '1 days', NOW() + INTERVAL '2 days', 750.0, 74000.0),
(4, NOW(), NOW() + INTERVAL '1 days', 900.0, 88000.0)
ON CONFLICT DO NOTHING;

-- =======================
-- TRAMOS
-- =======================
INSERT INTO tramos (ruta_id, origen, destino, distancia_km, costo, camion_id, transportista_id, deposito_origen_id, deposito_destino_id, fecha_inicio, fecha_fin_estimada, estado, tipo, orden)
VALUES
(1, 'Córdoba Capital', 'Villa María', 150.0, 13500.0, 1, 1, 1, 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', 'FINALIZADO', 'DEPOSITO_DEPOSITO', 1),
(1, 'Villa María', 'Rosario', 250.0, 25000.0, 2, 1, 2, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 days', 'FINALIZADO', 'DEPOSITO_DESTINO', 2),

(2, 'La Plata', 'Villa María', 500.0, 52000.0, 3, 2, 3, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 days', 'INICIADO', 'ORIGEN_DEPOSITO', 1),
(2, 'Villa María', 'Córdoba Capital', 200.0, 17000.0, 1, 1, 1, 1, NOW() - INTERVAL '1 days', NOW(), 'ASIGNADO', 'DEPOSITO_DESTINO', 2),

(3, 'Rosario', 'San Luis', 400.0, 39000.0, 2, 1, 2, 4, NOW() - INTERVAL '1 days', NOW() + INTERVAL '6 hours', 'INICIADO', 'ORIGEN_DEPOSITO', 1),
(3, 'San Luis', 'Mendoza Capital', 350.0, 35000.0, 3, 2, 4, 4, NOW(), NOW() + INTERVAL '1 days', 'ESTIMADO', 'DEPOSITO_DESTINO', 2),

(4, 'Salta Capital', 'Córdoba Capital', 900.0, 88000.0, 4, 3, 5, 1, NOW(), NOW() + INTERVAL '1 days', 'ESTIMADO', 'ORIGEN_DESTINO', 1)
ON CONFLICT DO NOTHING;

-- =======================
-- HISTORIAL DE ESTADOS
-- =======================
INSERT INTO cambios_estado_solicitud (solicitud_id, estado_anterior, estado_nuevo, fecha_cambio, observaciones)
VALUES
(1, 'PENDIENTE', 'ASIGNADA', NOW() - INTERVAL '4 days', 'Solicitud asignada a camión AA123BB'),
(1, 'ASIGNADA', 'EN_TRANSITO', NOW() - INTERVAL '3 days', 'Carga recogida y en tránsito'),
(1, 'EN_TRANSITO', 'COMPLETADA', NOW() - INTERVAL '1 days', 'Contenedor entregado correctamente'),

(2, 'PENDIENTE', 'ASIGNADA', NOW() - INTERVAL '3 days', 'Asignado a camión AB987CD'),
(2, 'ASIGNADA', 'EN_TRANSITO', NOW() - INTERVAL '2 days', 'En ruta hacia Córdoba'),

(3, 'PENDIENTE', 'ASIGNADA', NOW() - INTERVAL '1 days', 'Camión AC555EE asignado'),
(3, 'ASIGNADA', 'EN_TRANSITO', NOW() - INTERVAL '12 hours', 'Saliendo de Rosario'),

(4, 'PENDIENTE', 'ASIGNADA', NOW(), 'Preparando camión AD666FF para salida')
ON CONFLICT DO NOTHING;

-- =======================================================
-- FIN DEL SCRIPT DE SEED COMPLETO
-- =======================================================
