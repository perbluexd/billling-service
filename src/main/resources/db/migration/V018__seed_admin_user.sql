-- V018__seed_admin_user.sql
-- Crea un usuario admin solo si no existe (password BCrypt con pgcrypto)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO usuario (email, hash_password, activo, email_verificado)
SELECT
  'admin@cvanguardistas.com',
  crypt('Admin#2025', gen_salt('bf', 12)),  -- coste 12, compatible con BCryptPasswordEncoder(12)
  TRUE,
  TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE lower(email) = 'admin@cvanguardistas.com'
);
