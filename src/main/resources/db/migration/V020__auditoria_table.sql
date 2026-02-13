CREATE TABLE IF NOT EXISTS auditoria (
  id               BIGSERIAL PRIMARY KEY,
  usuario_id       BIGINT NOT NULL,
  entidad          VARCHAR(120) NOT NULL,
  entidad_id       VARCHAR(80)  NOT NULL,
  accion           VARCHAR(80)  NOT NULL,
  payload_anterior JSONB,
  payload_nuevo    JSONB,
  ip               VARCHAR(64),
  user_agent       TEXT,
  creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
  correlation_id   VARCHAR(64),
  razon_cambio     TEXT,
  CONSTRAINT fk_auditoria_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_aud_usuario_fecha ON auditoria(usuario_id, creado_en);
CREATE INDEX IF NOT EXISTS idx_aud_entidad       ON auditoria(entidad, entidad_id);
