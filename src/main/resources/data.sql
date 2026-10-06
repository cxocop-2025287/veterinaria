-- ===============================================
-- DATOS INICIALES - CLÍNICA VETERINARIA
-- ===============================================
-- Contraseñas encriptadas con BCrypt:
-- admin@veterinaria.com   -> Admin123*   ($2a$10$wL4/fC.vG852B7W1YmH34OhnN/5FhE383ZgJtLyg/Z7Nn7UjNnB/C)
-- vet@veterinaria.com     -> Vet123*     ($2a$10$8g4yP4BvC6m9M1u4z5F9.Ohq9d4D8d1J0n0L7E5g3c2b1a0z9y8x7)
-- cliente@veterinaria.com -> Cliente123* ($2a$10$8g4yP4BvC6m9M1u4z5F9.Ohq9d4D8d1J0n0L7E5g3c2b1a0z9y8x7)

INSERT INTO usuarios (id, nombre, telefono, email, password, rol) VALUES
(1, 'Administrador del Sistema', '555-0101', 'admin@veterinaria.com', '$2a$10$wL4/fC.vG852B7W1YmH34OhnN/5FhE383ZgJtLyg/Z7Nn7UjNnB/C', 'ADMIN'),
(2, 'Dr. Roberto Martínez (VET)', '555-0102', 'vet@veterinaria.com', '$2a$10$wL4/fC.vG852B7W1YmH34OhnN/5FhE383ZgJtLyg/Z7Nn7UjNnB/C', 'VET'),
(3, 'Carlos Cliente', '555-0103', 'cliente@veterinaria.com', '$2a$10$wL4/fC.vG852B7W1YmH34OhnN/5FhE383ZgJtLyg/Z7Nn7UjNnB/C', 'CLIENTE');

INSERT INTO mascotas (id, nombre, especie, raza, edad, cliente_id) VALUES
(1, 'Firulais', 'PERRO', 'Golden Retriever', 3, 3),
(2, 'Michi', 'GATO', 'Siamés', 2, 3);
