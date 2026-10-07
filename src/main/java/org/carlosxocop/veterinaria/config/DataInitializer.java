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
        // Inicializar únicamente el Administrador del Sistema
        crearUsuarioSiNoExiste("admin@veterinaria.com", "Administrador del Sistema", "55550101", "Admin123*", Rol.ADMIN);
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
