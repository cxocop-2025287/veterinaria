package org.carlosxocop.veterinaria.service;

import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioRequest;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioResponse;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.exception.ConflictException;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflictException("El email ya se encuentra registrado: " + emailNormalizado);
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .email(emailNormalizado)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse crearVeterinario(UsuarioRequest request) {
        request.setRol(Rol.VET);
        return crearUsuario(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerVeterinarios() {
        return usuarioRepository.findByRol(Rol.VET)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .telefono(usuario.getTelefono())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
