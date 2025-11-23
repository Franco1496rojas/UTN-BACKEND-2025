-- ==========================================
-- MIGRACIÓN: Agregar campos de capacidad a depositos
-- ==========================================

-- Paso 1: Agregar columna capacidad_maxima con valor por defecto temporal
ALTER TABLE depositos 
ADD COLUMN IF NOT EXISTS capacidad_maxima INTEGER DEFAULT 100;

-- Paso 2: Agregar columna cantidad_ocupada con valor por defecto 0
ALTER TABLE depositos 
ADD COLUMN IF NOT EXISTS cantidad_ocupada INTEGER DEFAULT 0;

-- Paso 3: Actualizar registros existentes (si es necesario)
UPDATE depositos 
SET capacidad_maxima = 100, cantidad_ocupada = 0 
WHERE capacidad_maxima IS NULL OR cantidad_ocupada IS NULL;

-- Paso 4: Hacer las columnas NOT NULL (después de tener valores)
ALTER TABLE depositos 
ALTER COLUMN capacidad_maxima SET NOT NULL;

ALTER TABLE depositos 
ALTER COLUMN cantidad_ocupada SET NOT NULL;

-- Paso 5: Remover el default de capacidad_maxima (debe ser especificado al crear)
ALTER TABLE depositos 
ALTER COLUMN capacidad_maxima DROP DEFAULT;

-- Paso 6: Mantener el default de cantidad_ocupada en 0
-- (ya está configurado)

-- Verificación: Mostrar estructura de la tabla
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns
WHERE table_name = 'depositos'
ORDER BY ordinal_position;
