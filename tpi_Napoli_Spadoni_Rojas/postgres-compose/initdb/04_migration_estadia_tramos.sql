-- Migración: Agregar columnas de estadía a la tabla tramos
-- Fecha: 2025-11-16
-- Descripción: Agrega campos para calcular estadías en depósitos

ALTER TABLE tramos 
ADD COLUMN IF NOT EXISTS dias_estadia INTEGER,
ADD COLUMN IF NOT EXISTS costo_estadia DOUBLE PRECISION;

-- Comentarios para las columnas
COMMENT ON COLUMN tramos.dias_estadia IS 'Días de estadía en depósito (redondeado hacia arriba)';
COMMENT ON COLUMN tramos.costo_estadia IS 'Costo total de estadía en depósito';
