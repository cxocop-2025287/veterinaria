package org.carlosxocop.veterinaria.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearUsuarioSiNoExiste("admin@veterinaria.com", "Administrador del Sistema", "555-0101", "Admin123*", Rol.ADMIN);
        crearUsuarioSiNoExiste("vet@veterinaria.com", "Dr. Roberto Martínez (VET)", "555-0102", "Vet123*", Rol.VET);
        crearUsuarioSiNoExiste("cliente@veterinaria.com", "Carlos Cliente", "555-0103", "Cliente123*", Rol.CLIENTE);
    }

    private void crearUsuarioSiNoExiste(String email, String nombre, String telefono, String rawPassword, Rol rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario usuario = Usuario.builder()
                    .email(email)
                    .nombre(nombre)
                    .telefono(telefono)
                    .password(passwordEncoder.encode(rawPassword))
                    .rol(rol)
                    .build();
            usuarioRepository.save(usuario);
            log.info("Usuario inicial creado dinámicamente: {} ({})", email, rol);
        }
    }
}
