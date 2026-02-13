ALTER TABLE partida_catalogo
  ADD COLUMN IF NOT EXISTS codigo varchar(64),
  ADD COLUMN IF NOT EXISTS tipo varchar(20);

-- si ya tienes datos y quieres evitar nulls:
UPDATE partida_catalogo SET tipo = 'HOJA' WHERE tipo IS NULL;

-- opcional: índice/unique
CREATE UNIQUE INDEX IF NOT EXISTS uk_partida_catalogo_codigo ON partida_catalogo (codigo);
CREATE INDEX IF NOT EXISTS idx_partida_catalogo_tipo ON partida_catalogo (tipo);
CREATE INDEX IF NOT EXISTS idx_partida_catalogo_nombre_ci ON partida_catalogo (lower(nombre));
