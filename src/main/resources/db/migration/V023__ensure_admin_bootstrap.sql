-- V023__ensure_admin_bootstrap.sql
-- Propósito:
-- 1) Asegurar rol ADMIN
-- 2) Asegurar usuario admin con password conocida (BCrypt)
-- 3) Asegurar vínculo usuario_rol (admin ↔ ADMIN)
-- 4) Revocar refresh tokens previos del admin (coherencia de sesiones)

-- Asegura extensión para BCrypt en PostgreSQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) Rol ADMIN (idempotente)
INSERT INTO rol (nombre, descripcion, creado_fecha)
VALUES ('ADMIN', 'Administrador del sistema', NOW())
ON CONFLICT (nombre) DO NOTHING;

-- 2) Usuario admin (idempotente + fuerza password conocida)
--    ⚠️ Cambia 'Admin#2025' si deseas otra contraseña inicial.
--    Usa coste 12 (compatible con BCryptPasswordEncoder(12) de Spring).
DO $$
DECLARE
    v_uid BIGINT;
BEGIN
    -- Si no existe, créalo
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE lower(email) = 'admin@cvanguardistas.com') THEN
        INSERT INTO usuario (
            nombres, apellidos, email, hash_password,
            activo, primer_login, email_verificado,
            creado_fecha, actualizado_fecha, pwd_changed_at
        ) VALUES (
            'Admin', 'Sistema', 'admin@cvanguardistas.com',
            crypt('Admin#2025', gen_salt('bf', 12)),
            TRUE, TRUE, TRUE,
            NOW(), NOW(), NOW()
        );
    ELSE
        -- Si existe, asegura estado coherente y password conocida
        UPDATE usuario
        SET
            hash_password   = crypt('Admin#2025', gen_salt('bf', 12)),
            activo          = TRUE,
            email_verificado= TRUE,
            primer_login    = TRUE,
            pwd_changed_at  = NOW(),
            actualizado_fecha = NOW()
        WHERE lower(email) = 'admin@cvanguardistas.com';
    END IF;

    -- Toma el id del admin
    SELECT id INTO v_uid FROM usuario WHERE lower(email) = 'admin@cvanguardistas.com';

    -- 3) Vincula ADMIN ↔ admin (idempotente)
    INSERT INTO usuario_rol (usuario_id, rol_id, asignado_fecha)
    SELECT v_uid, r.id, NOW()
    FROM rol r
    WHERE r.nombre = 'ADMIN'
    ON CONFLICT (usuario_id, rol_id) DO NOTHING;

    -- 4) Revoca refresh tokens previos (si la tabla existe)
    --    Nota: comenta este bloque si no usas refresh_token aún.
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_name = 'refresh_token') THEN
        UPDATE refresh_token
        SET revocado = TRUE
        WHERE usuario_id = v_uid
          AND revocado = FALSE;
    END IF;
END $$;
