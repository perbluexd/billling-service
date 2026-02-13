-- V014__presupuesto_snapshot.sql
-- Tabla de snapshots por presupuesto + índices profesionales

CREATE TABLE IF NOT EXISTS presupuesto_snapshot (
    id              BIGSERIAL PRIMARY KEY,
    presupuesto_id  BIGINT NOT NULL,
    version         VARCHAR(100) NOT NULL,
    creado_en       TIMESTAMP NOT NULL DEFAULT NOW(),
    json_snapshot   JSONB NOT NULL,
    CONSTRAINT fk_ps_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id)
        ON DELETE CASCADE
);

-- Evita versiones en blanco (espacios)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ck_ps_version_not_blank'
  ) THEN
    ALTER TABLE presupuesto_snapshot
      ADD CONSTRAINT ck_ps_version_not_blank
      CHECK (length(btrim(version)) > 0);
  END IF;
END$$;

-- Un snapshot por versión y presupuesto
CREATE UNIQUE INDEX IF NOT EXISTS uk_ps_presupuesto_version
  ON presupuesto_snapshot (presupuesto_id, version);

-- Índices de consulta comunes
CREATE INDEX IF NOT EXISTS idx_ps_presupuesto ON presupuesto_snapshot (presupuesto_id);
CREATE INDEX IF NOT EXISTS idx_ps_creado_en   ON presupuesto_snapshot (creado_en);
