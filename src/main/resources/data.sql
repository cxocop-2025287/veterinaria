-- ===============================================
-- DATOS INICIALES - CLÍNICA VETERINARIA
-- ===============================================
-- Contraseñas verificadas encriptadas con BCrypt:
-- admin@veterinaria.com   -> Admin123*   ($2a$10$Vm/Xc0FBIN.LX9umVdya1eTZFqV.BI8uTCk3Grgzznf4pdMKS6chO)
-- vet@veterinaria.com     -> Vet123*     ($2a$10$A4PkMGkvPdB7R2KjCbAWm.Wj5.6XsbiWyl3aaK.umnspSlC7sjQZm)
-- cliente@veterinaria.com -> Cliente123* ($2a$10$fV4eyqJ.deqZQrDZ/.KCQebyosSaoqfzwxACMY8wDAbcyLuxIqrEO)

INSERT INTO usuarios (id, nombre, telefono, email, password, rol) VALUES
(1, 'Administrador del Sistema', '555-0101', 'admin@veterinaria.com', '$2a$10$Vm/Xc0FBIN.LX9umVdya1eTZFqV.BI8uTCk3Grgzznf4pdMKS6chO', 'ADMIN'),
(2, 'Dr. Roberto Martínez (VET)', '555-0102', 'vet@veterinaria.com', '$2a$10$A4PkMGkvPdB7R2KjCbAWm.Wj5.6XsbiWyl3aaK.umnspSlC7sjQZm', 'VET'),
(3, 'Carlos Cliente', '555-0103', 'cliente@veterinaria.com', '$2a$10$fV4eyqJ.deqZQrDZ/.KCQebyosSaoqfzwxACMY8wDAbcyLuxIqrEO', 'CLIENTE')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO mascotas (id, nombre, especie, raza, edad, cliente_id) VALUES
(1, 'Firulais', 'PERRO', 'Golden Retriever', 3, 3),
(2, 'Michi', 'GATO', 'Siamés', 2, 3)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
