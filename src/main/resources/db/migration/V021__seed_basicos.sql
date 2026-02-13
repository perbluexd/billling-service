-- ============================
-- V021__seed_basicos.sql
-- ============================

-- ---------- Unidades ----------
INSERT INTO unidad (codigo, descripcion) VALUES
  ('HH','Hora-hombre'),
  ('M2','Metro cuadrado'),
  ('M3','Metro cúbico'),
  ('ML','Metro lineal'),
  ('UND','Unidad'),
  ('GLB','Global'),
  ('PTO','Punto'),
  ('MES','Mes')
ON CONFLICT (codigo) DO UPDATE
SET descripcion = EXCLUDED.descripcion;

-- ---------- Categorías de costo ----------
-- Asegura columnas NOT NULL con valores
INSERT INTO categoria_costo (codigo, nombre, orden, incluye_en_cu, visible) VALUES
  ('MO','Mano de Obra',            2, TRUE,  TRUE),
  ('MT','Material',                3, TRUE,  TRUE),
  ('EQ','Equipo',                  4, TRUE,  TRUE),
  ('SC','Subcontrato/Servicio',    5, TRUE,  TRUE),
  ('SP','Subpartida',              6, FALSE, TRUE)
ON CONFLICT (codigo) DO UPDATE
SET nombre        = EXCLUDED.nombre,
    orden         = EXCLUDED.orden,
    incluye_en_cu = EXCLUDED.incluye_en_cu,
    visible       = EXCLUDED.visible;

UPDATE categoria_costo SET incluye_en_cu = TRUE  WHERE incluye_en_cu IS NULL;
UPDATE categoria_costo SET visible       = TRUE  WHERE visible       IS NULL;

-- (Opcional) Defaults por si en el futuro olvidan columnas
-- ALTER TABLE categoria_costo
--   ALTER COLUMN incluye_en_cu SET DEFAULT TRUE,
--   ALTER COLUMN visible       SET DEFAULT TRUE;

-- ---------- Tipo de Insumo: saneo de esquema previo ----------
-- Crea columnas si no existen (tu BD actual no tiene 'codigo' y posiblemente tampoco 'activo/orden/color_hex')
ALTER TABLE tipo_insumo ADD COLUMN IF NOT EXISTS codigo     VARCHAR(16);
ALTER TABLE tipo_insumo ADD COLUMN IF NOT EXISTS nombre     VARCHAR(255);
ALTER TABLE tipo_insumo ADD COLUMN IF NOT EXISTS color_hex  VARCHAR(16);
ALTER TABLE tipo_insumo ADD COLUMN IF NOT EXISTS orden      INTEGER;
ALTER TABLE tipo_insumo ADD COLUMN IF NOT EXISTS activo     BOOLEAN;

-- Si hay filas antiguas sin 'codigo', mapeamos por nombre
UPDATE tipo_insumo SET codigo = 'MO' WHERE codigo IS NULL AND UPPER(nombre) LIKE 'MANO%';
UPDATE tipo_insumo SET codigo = 'MT' WHERE codigo IS NULL AND UPPER(nombre) LIKE 'MATERIAL%';
UPDATE tipo_insumo SET codigo = 'EQ' WHERE codigo IS NULL AND UPPER(nombre) LIKE 'EQUIPO%';
UPDATE tipo_insumo SET codigo = 'SC' WHERE codigo IS NULL AND UPPER(nombre) LIKE 'SERVICIO%';

-- Asegura activo/orden
UPDATE tipo_insumo SET activo = TRUE WHERE activo IS NULL;
UPDATE tipo_insumo
SET orden = CASE codigo
              WHEN 'MO' THEN 1
              WHEN 'MT' THEN 2
              WHEN 'EQ' THEN 3
              WHEN 'SC' THEN 4
              ELSE COALESCE(orden, 99)
            END
WHERE orden IS NULL;

-- Unicidad por codigo (si ya hay, la instrucción IF NOT EXISTS evita choque)
CREATE UNIQUE INDEX IF NOT EXISTS uk_tipo_insumo_codigo ON tipo_insumo(codigo);

-- Ahora sí: NOT NULL en las columnas clave
ALTER TABLE tipo_insumo ALTER COLUMN codigo SET NOT NULL;
ALTER TABLE tipo_insumo ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE tipo_insumo ALTER COLUMN activo SET NOT NULL;

-- ---------- Tipos de insumo (UPSERT por 'codigo') ----------
INSERT INTO tipo_insumo (codigo, nombre, orden, activo) VALUES
  ('MO','MANO DE OBRA', 1, TRUE),
  ('MT','MATERIAL',     2, TRUE),
  ('EQ','EQUIPO',       3, TRUE),
  ('SC','SERVICIO',     4, TRUE)
ON CONFLICT (codigo) DO UPDATE
SET nombre = EXCLUDED.nombre,
    orden  = EXCLUDED.orden,
    activo = EXCLUDED.activo;
