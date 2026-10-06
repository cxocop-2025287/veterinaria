package org.carlosxocop.veterinaria.service;

import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.auth.AuthResponse;
import org.carlosxocop.veterinaria.dto.auth.LoginRequest;
import org.carlosxocop.veterinaria.dto.auth.RegisterRequest;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.exception.ConflictException;
import org.carlosxocop.veterinaria.exception.ResourceNotFoundException;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.carlosxocop.veterinaria.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya se encuentra registrado: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.CLIENTE) // Registro público siempre asigna CLIENTE
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(guardado, guardado.getRol());

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .rol(guardado.getRol())
                .email(guardado.getEmail())
                .nombre(guardado.getNombre())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.getEmail()));

        String token = jwtService.generateToken(usuario, usuario.getRol());

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .rol(usuario.getRol())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .build();
    }
}
