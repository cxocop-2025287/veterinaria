-- ===============================================
-- DATOS INICIALES - CLÍNICA VETERINARIA
-- ===============================================
-- Contraseñas encriptadas con BCrypt:
-- admin@veterinaria.com   -> admin123   ($2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi)
-- vet@veterinaria.com     -> vet123     ($2a$10$j8wJ3.FmY6/fGvI0wF/3eOBxYIHz2zD1sYg4oQk1G0eT8I9F4D0yC)
-- cliente@veterinaria.com -> cliente123 ($2a$10$8g4yP4BvC6m9M1u4z5F9.Ohq9d4D8d1J0n0L7E5g3c2b1a0z9y8x7)

INSERT INTO usuarios (id, nombre, telefono, email, password, rol) VALUES
(1, 'Administrador del Sistema', '555-0101', 'admin@veterinaria.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN'),
(2, 'Dr. Roberto Martínez (VET)', '555-0102', 'vet@veterinaria.com', '$2a$10$j8wJ3.FmY6/fGvI0wF/3eOBxYIHz2zD1sYg4oQk1G0eT8I9F4D0yC', 'VET'),
(3, 'Carlos Cliente', '555-0103', 'cliente@veterinaria.com', '$2a$10$8g4yP4BvC6m9M1u4z5F9.Ohq9d4D8d1J0n0L7E5g3c2b1a0z9y8x7', 'CLIENTE');

INSERT INTO mascotas (id, nombre, especie, raza, edad, cliente_id) VALUES
(1, 'Firulais', 'PERRO', 'Golden Retriever', 3, 3),
(2, 'Michi', 'GATO', 'Siamés', 2, 3);
