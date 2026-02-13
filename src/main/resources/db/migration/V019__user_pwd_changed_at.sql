ALTER TABLE usuario
  ADD COLUMN IF NOT EXISTS pwd_changed_at timestamptz;
