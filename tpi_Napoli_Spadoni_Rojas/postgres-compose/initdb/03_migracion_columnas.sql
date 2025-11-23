-- ==========================================
-- MIGRACIÓN: Agregar columnas faltantes
-- ==========================================

-- Actualizar tabla SOLICITUDES
-- Agregar columnas faltantes
ALTER TABLE solicitudes 
    ADD COLUMN IF NOT EXISTS costo_estimado NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS costo_real NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS tiempo_estimado_min INT,
    ADD COLUMN IF NOT EXISTS tiempo_real_min INT,
    ADD COLUMN IF NOT EXISTS observaciones TEXT;

-- Renombrar columna costo_total a costo_estimado si existe
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'solicitudes' AND column_name = 'costo_total'
    ) THEN
        -- Copiar datos de costo_total a costo_estimado si costo_estimado está vacío
        UPDATE solicitudes SET costo_estimado = costo_total WHERE costo_estimado IS NULL;
        -- Eliminar columna costo_total
        ALTER TABLE solicitudes DROP COLUMN IF EXISTS costo_total;
    END IF;
END $$;

-- Cambiar el valor por defecto del estado
ALTER TABLE solicitudes ALTER COLUMN estado_actual SET DEFAULT 'BORRADOR';

-- Actualizar tabla TRAMOS
-- Agregar columnas faltantes
ALTER TABLE tramos 
    ADD COLUMN IF NOT EXISTS transportista_id INT,
    ADD COLUMN IF NOT EXISTS fecha_hora_inicio_real TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_hora_fin_real TIMESTAMP,
    ADD COLUMN IF NOT EXISTS distancia_km_real NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS duracion_min_real INT,
    ADD COLUMN IF NOT EXISTS costo_real NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS estado VARCHAR(50) DEFAULT 'ESTIMADO',
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(50),
    ADD COLUMN IF NOT EXISTS orden INT;

-- Actualizar valores por defecto para columnas existentes
UPDATE tramos SET estado = 'ESTIMADO' WHERE estado IS NULL;
UPDATE tramos SET orden = 1 WHERE orden IS NULL;

-- ==========================================
-- FIN DE LA MIGRACIÓN
-- ==========================================
