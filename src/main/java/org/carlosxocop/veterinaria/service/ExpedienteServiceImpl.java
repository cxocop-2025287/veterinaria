package org.carlosxocop.veterinaria.service;

import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteRequest;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteResponse;
import org.carlosxocop.veterinaria.entity.CitaMedica;
import org.carlosxocop.veterinaria.entity.ExpedienteClinico;
import org.carlosxocop.veterinaria.entity.Mascota;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.EstadoCita;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.exception.BusinessException;
import org.carlosxocop.veterinaria.exception.ConflictException;
import org.carlosxocop.veterinaria.exception.ResourceNotFoundException;
import org.carlosxocop.veterinaria.exception.UnauthorizedException;
import org.carlosxocop.veterinaria.repository.CitaMedicaRepository;
import org.carlosxocop.veterinaria.repository.ExpedienteClinicoRepository;
import org.carlosxocop.veterinaria.repository.MascotaRepository;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpedienteServiceImpl implements ExpedienteService {

    private final ExpedienteClinicoRepository expedienteClinicoRepository;
    private final CitaMedicaRepository citaMedicaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ExpedienteResponse crearExpediente(ExpedienteRequest request, String currentUserEmail) {
        CitaMedica cita = citaMedicaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con id: " + request.getCitaId()));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BusinessException("No se puede registrar un expediente para una cita cancelada");
        }

        if (expedienteClinicoRepository.existsByCitaId(request.getCitaId())) {
            throw new ConflictException("Ya existe un expediente clínico registrado para esta cita");
        }

        ExpedienteClinico expediente = ExpedienteClinico.builder()
                .cita(cita)
                .diagnostico(request.getDiagnostico())
                .tratamiento(request.getTratamiento())
                .pesoKg(request.getPesoKg())
                .fechaRegistro(LocalDateTime.now())
                .build();

        // Al registrar correctamente el expediente: CitaMedica.estado = COMPLETADA
        cita.setEstado(EstadoCita.COMPLETADA);
        citaMedicaRepository.save(cita);

        ExpedienteClinico guardado = expedienteClinicoRepository.save(expediente);
        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteResponse> obtenerHistorialPorMascota(Long mascotaId, String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con id: " + mascotaId));

        // Si es CLIENTE, solo puede ver el historial de sus propias mascotas
        if (currentUser.getRol() == Rol.CLIENTE && !mascota.getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No tienes permiso para consultar el historial de esta mascota");
        }

        return expedienteClinicoRepository.findByMascotaId(mascotaId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ExpedienteResponse mapToResponse(ExpedienteClinico expediente) {
        CitaMedica cita = expediente.getCita();
        Mascota mascota = cita != null ? cita.getMascota() : null;
        Usuario vet = cita != null ? cita.getVeterinario() : null;

        return ExpedienteResponse.builder()
                .id(expediente.getId())
                .citaId(cita != null ? cita.getId() : null)
                .mascotaId(mascota != null ? mascota.getId() : null)
                .mascotaNombre(mascota != null ? mascota.getNombre() : null)
                .veterinarioId(vet != null ? vet.getId() : null)
                .veterinarioNombre(vet != null ? vet.getNombre() : null)
                .diagnostico(expediente.getDiagnostico())
                .tratamiento(expediente.getTratamiento())
                .pesoKg(expediente.getPesoKg())
                .fechaRegistro(expediente.getFechaRegistro())
                .build();
    }
}
