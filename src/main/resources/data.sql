-- ===============================================
-- DATOS INICIALES - CLÍNICA VETERINARIA
-- ===============================================
-- Único usuario inicial: Administrador del Sistema
-- Email: admin@veterinaria.com
-- Contraseña: Admin123* (Hash BCrypt verificado)

INSERT INTO usuarios (id, nombre, telefono, email, password, rol) VALUES
(1, 'Administrador del Sistema', '55550101', 'admin@veterinaria.com', '$2a$10$Vm/Xc0FBIN.LX9umVdya1eTZFqV.BI8uTCk3Grgzznf4pdMKS6chO', 'ADMIN')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
