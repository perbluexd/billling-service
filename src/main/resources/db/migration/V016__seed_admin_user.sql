-- Rol ADMIN
INSERT INTO rol (nombre, descripcion, creado_fecha)
VALUES ('ADMIN', 'Administrador del sistema', NOW())
ON CONFLICT (nombre) DO NOTHING;

-- Usuario admin (sin password por ahora; primer_login=true obliga a setearla)
INSERT INTO usuario (
  nombres, apellidos, email, hash_password,
  activo, primer_login, email_verificado,
  creado_fecha, actualizado_fecha
) VALUES (
  'Admin', 'Sistema', 'admin@cvanguardistas.com', NULL,
  TRUE, TRUE, TRUE,
  NOW(), NOW()
)
ON CONFLICT (email) DO NOTHING;

-- Vincular ADMIN
INSERT INTO usuario_rol (usuario_id, rol_id, asignado_fecha)
SELECT u.id, r.id, NOW()
FROM usuario u, rol r
WHERE u.email = 'admin@cvanguardistas.com' AND r.nombre = 'ADMIN'
ON CONFLICT ON CONSTRAINT uk_usuario_rol DO NOTHING;
