package org.carlosxocop.veterinaria.service;

import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.mascota.MascotaRequest;
import org.carlosxocop.veterinaria.dto.mascota.MascotaResponse;
import org.carlosxocop.veterinaria.entity.Mascota;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.exception.ResourceNotFoundException;
import org.carlosxocop.veterinaria.exception.UnauthorizedException;
import org.carlosxocop.veterinaria.repository.MascotaRepository;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public MascotaResponse crearMascota(MascotaRequest request, String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        Usuario duenio;
        if (currentUser.getRol() == Rol.ADMIN && request.getClienteId() != null) {
            duenio = usuarioRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getClienteId()));
        } else {
            // Si es CLIENTE (o ADMIN sin clienteId especificado), el dueño es el usuario autenticado
            duenio = currentUser;
        }

        Mascota mascota = Mascota.builder()
                .nombre(request.getNombre())
                .especie(request.getEspecie())
                .raza(request.getRaza())
                .edad(request.getEdad())
                .cliente(duenio)
                .build();

        Mascota guardada = mascotaRepository.save(mascota);
        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> obtenerMisMascotas(String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        return mascotaRepository.findByClienteId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MascotaResponse obtenerPorId(Long id, String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con id: " + id));

        // Un cliente solo puede ver su propia mascota
        if (currentUser.getRol() == Rol.CLIENTE && !mascota.getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No tienes permiso para consultar información de esta mascota");
        }

        return mapToResponse(mascota);
    }

    private MascotaResponse mapToResponse(Mascota mascota) {
        return MascotaResponse.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .edad(mascota.getEdad())
                .clienteId(mascota.getCliente() != null ? mascota.getCliente().getId() : null)
                .clienteNombre(mascota.getCliente() != null ? mascota.getCliente().getNombre() : null)
                .clienteEmail(mascota.getCliente() != null ? mascota.getCliente().getEmail() : null)
                .build();
    }
}
