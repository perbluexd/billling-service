-- Backfill por si quedara alguna fila nula (no debería en limpio)
UPDATE partida_catalogo SET rendimiento = 1.00 WHERE rendimiento IS NULL;

-- Asegura NOT NULL + DEFAULT (si ya es NOT NULL, el ALTER solo agrega default)
ALTER TABLE partida_catalogo
    ALTER COLUMN rendimiento SET DEFAULT 1.00,
    ALTER COLUMN rendimiento SET NOT NULL;
