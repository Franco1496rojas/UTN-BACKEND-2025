-- ==========================================
-- TPI LOGÍSTICA - SCHEMA BASE DE DATOS
-- ==========================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===============================
-- CLIENTES Y CONTENEDORES
-- ===============================

CREATE TABLE IF NOT EXISTS provincias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ciudades (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10),
    provincia_id INT REFERENCES provincias(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS clientes (
    id SERIAL PRIMARY KEY,
    dni BIGINT NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    telefono VARCHAR(50),
    domicilio VARCHAR(200),
    keycloak_id VARCHAR(120),
    ciudad_id INT REFERENCES ciudades(id)
);

CREATE TABLE IF NOT EXISTS contenedores (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    peso NUMERIC(10,2) NOT NULL,
    volumen NUMERIC(10,2) NOT NULL,
    cliente_id INT REFERENCES clientes(id) ON DELETE CASCADE
);

-- ===============================
-- FLOTA Y TRANSPORTE
-- ===============================

CREATE TABLE IF NOT EXISTS transportistas (
    id SERIAL PRIMARY KEY,
    dni BIGINT NOT NULL UNIQUE,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    email VARCHAR(150),
    telefono VARCHAR(50),
    domicilio VARCHAR(200),
    keycloak_id VARCHAR(120),
    ciudad VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS administradores (
    id SERIAL PRIMARY KEY,
    dni BIGINT NOT NULL UNIQUE,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    email VARCHAR(150),
    telefono VARCHAR(50),
    domicilio VARCHAR(200),
    keycloak_id VARCHAR(120),
    ciudad VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS camiones (
    id SERIAL PRIMARY KEY,
    dominio VARCHAR(20) NOT NULL UNIQUE,
    capacidad_peso NUMERIC(10,2),
    capacidad_volumen NUMERIC(10,2),
    disponibilidad BOOLEAN DEFAULT TRUE,
    costo_km_base NUMERIC(10,2),
    consumo_litro_km NUMERIC(10,3),
    transportista_id INT REFERENCES transportistas(id)
);

CREATE TABLE IF NOT EXISTS depositos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    latitud NUMERIC(10,6),
    longitud NUMERIC(10,6),
    costo_estadia_diaria NUMERIC(10,2),
    estado BOOLEAN DEFAULT TRUE,
    capacidad_maxima INTEGER NOT NULL,
    cantidad_ocupada INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tarifas_rango_volumen_peso (
    id SERIAL PRIMARY KEY,
    volumen_min NUMERIC(10,2),
    volumen_max NUMERIC(10,2),
    peso_min NUMERIC(10,2),
    peso_max NUMERIC(10,2),
    costo_km_base NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS parametros_tarifa (
    id SERIAL PRIMARY KEY,
    precio_litro_combustible NUMERIC(10,2),
    cargo_fijo_tramo NUMERIC(10,2)
);

-- ===============================
-- OPERACIONES LOGÍSTICAS
-- ===============================

CREATE TABLE IF NOT EXISTS solicitudes (
    id SERIAL PRIMARY KEY,
    cliente_id INT NOT NULL,
    contenedor_id INT NOT NULL,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    origen VARCHAR(200) NOT NULL,
    destino VARCHAR(200) NOT NULL,
    distancia_km NUMERIC(10,2),
    costo_estimado NUMERIC(12,2),
    costo_real NUMERIC(12,2),
    tiempo_estimado_min INT,
    tiempo_real_min INT,
    estado_actual VARCHAR(50) DEFAULT 'BORRADOR',
    observaciones TEXT,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (contenedor_id) REFERENCES contenedores(id)
);

CREATE TABLE IF NOT EXISTS rutas (
    id SERIAL PRIMARY KEY,
    solicitud_id INT NOT NULL,
    fecha_inicio TIMESTAMP,
    fecha_fin_estimada TIMESTAMP,
    distancia_total_km NUMERIC(10,2),
    costo_total NUMERIC(12,2),
    FOREIGN KEY (solicitud_id) REFERENCES solicitudes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tramos (
    id SERIAL PRIMARY KEY,
    ruta_id INT NOT NULL,
    origen VARCHAR(200),
    destino VARCHAR(200),
    distancia_km NUMERIC(10,2),
    costo NUMERIC(12,2),
    camion_id INT,
    transportista_id INT,
    deposito_origen_id INT,
    deposito_destino_id INT,
    fecha_inicio TIMESTAMP,
    fecha_fin_estimada TIMESTAMP,
    fecha_hora_inicio_real TIMESTAMP,
    fecha_hora_fin_real TIMESTAMP,
    distancia_km_real NUMERIC(10,2),
    duracion_min_real INT,
    costo_real NUMERIC(12,2),
    costo_estadia NUMERIC(12,2),
    dias_estadia INT,
    estado VARCHAR(50) DEFAULT 'ESTIMADO',
    tipo VARCHAR(50),
    orden INT,
    FOREIGN KEY (ruta_id) REFERENCES rutas(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cambios_estado_solicitud (
    id SERIAL PRIMARY KEY,
    solicitud_id INT NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50),
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones VARCHAR(250),
    FOREIGN KEY (solicitud_id) REFERENCES solicitudes(id) ON DELETE CASCADE
);

-- ===============================
-- ÍNDICES Y CONSTRAINTS ADICIONALES
-- ===============================
CREATE INDEX IF NOT EXISTS idx_cliente_ciudad ON clientes(ciudad_id);
CREATE INDEX IF NOT EXISTS idx_contenedor_cliente ON contenedores(cliente_id);
CREATE INDEX IF NOT EXISTS idx_tramo_ruta ON tramos(ruta_id);
CREATE INDEX IF NOT EXISTS idx_ruta_solicitud ON rutas(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON solicitudes(cliente_id);
-- ===============================
-- FIN DEL SCRIPT
-- ===============================
