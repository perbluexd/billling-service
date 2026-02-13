-- V015__auth_refresh_token_and_auditoria_indexes.sql

-- 1) Refresh tokens
CREATE TABLE IF NOT EXISTS refresh_token (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash   VARCHAR(256) NOT NULL,
    expira_en    TIMESTAMP NOT NULL,
    revocado     BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en    TIMESTAMP NOT NULL DEFAULT NOW(),
    ip           VARCHAR(45),
    user_agent   VARCHAR(512)
);

-- Único por hash (no almacenamos el token en claro)
CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_token_hash ON refresh_token (token_hash);

-- Búsqueda por usuario/expiración (limpieza y consultas)
CREATE INDEX IF NOT EXISTS idx_refresh_usuario_expira ON refresh_token (usuario_id, expira_en);
CREATE INDEX IF NOT EXISTS idx_refresh_expira ON refresh_token (expira_en);

-- 2) Auditoría: asegurar jsonb (si ya existía como text/json)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'auditoria' AND column_name = 'payload_anterior'
  ) THEN
    BEGIN
      ALTER TABLE auditoria
        ALTER COLUMN payload_anterior TYPE jsonb USING payload_anterior::jsonb;
    EXCEPTION WHEN others THEN
      -- si ya es jsonb, ignora
      NULL;
    END;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'auditoria' AND column_name = 'payload_nuevo'
  ) THEN
    BEGIN
      ALTER TABLE auditoria
        ALTER COLUMN payload_nuevo TYPE jsonb USING payload_nuevo::jsonb;
    EXCEPTION WHEN others THEN
      NULL;
    END;
  END IF;
END$$;

-- Índice recomendado para consultas por objeto con orden temporal
CREATE INDEX IF NOT EXISTS idx_aud_entidad_obj_fecha
  ON auditoria (entidad, entidad_id, creado_en);

-- 3) (Opcional) garantizar email único si no existe
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE schemaname = 'public'
      AND indexname = 'uk_usuario_email'
  ) THEN
    BEGIN
      CREATE UNIQUE INDEX uk_usuario_email ON usuario (email);
    EXCEPTION WHEN others THEN
      NULL;
    END;
  END IF;
END$$;
