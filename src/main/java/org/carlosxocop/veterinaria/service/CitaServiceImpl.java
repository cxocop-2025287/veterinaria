package org.carlosxocop.veterinaria.service;

import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.cita.CitaRequest;
import org.carlosxocop.veterinaria.dto.cita.CitaResponse;
import org.carlosxocop.veterinaria.entity.CitaMedica;
import org.carlosxocop.veterinaria.entity.Mascota;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.EstadoCita;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.exception.BusinessException;
import org.carlosxocop.veterinaria.exception.ConflictException;
import org.carlosxocop.veterinaria.exception.ResourceNotFoundException;
import org.carlosxocop.veterinaria.exception.UnauthorizedException;
import org.carlosxocop.veterinaria.repository.CitaMedicaRepository;
import org.carlosxocop.veterinaria.repository.MascotaRepository;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaMedicaRepository citaMedicaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public CitaResponse crearCita(CitaRequest request, String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con id: " + request.getMascotaId()));

        // Validación de pertenencia si el usuario es CLIENTE
        if (currentUser.getRol() == Rol.CLIENTE && !mascota.getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Solo puedes agendar citas para tus propias mascotas");
        }

        Usuario veterinario = usuarioRepository.findByIdWithLock(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con id: " + request.getVeterinarioId()));

        if (veterinario.getRol() != Rol.VET) {
            throw new BusinessException("El usuario seleccionado no tiene el rol de VETERINARIO (VET)");
        }

        // Regla 17: Límite de citas del cliente (máximo 2 citas PENDIENTE para el mismo día)
        Usuario clienteDueno = mascota.getCliente();
        LocalDateTime inicioDia = request.getFechaHora().toLocalDate().atStartOfDay();
        LocalDateTime finDia = request.getFechaHora().toLocalDate().atTime(23, 59, 59, 999999999);

        long citasPendientesCliente = citaMedicaRepository.countPendingAppointmentsForClientOnDate(
                clienteDueno.getId(),
                EstadoCita.PENDIENTE,
                inicioDia,
                finDia
        );

        if (citasPendientesCliente >= 2) {
            throw new BusinessException("El cliente ya tiene 2 citas pendientes para esta fecha (" + request.getFechaHora().toLocalDate() + ")");
        }

        // Regla 16: Regla de disponibilidad del veterinario (cada cita dura 30 minutos)
        // Se solapan si |fechaExistente - fechaNueva| < 30 minutos
        LocalDateTime inicioVentana = request.getFechaHora().minusMinutes(30);
        LocalDateTime finVentana = request.getFechaHora().plusMinutes(30);

        List<CitaMedica> citasConflicto = citaMedicaRepository.findConflictingAppointments(
                veterinario.getId(),
                inicioVentana,
                finVentana,
                EstadoCita.CANCELADA
        );

        if (!citasConflicto.isEmpty()) {
            throw new ConflictException("El veterinario ya tiene una cita en ese horario o en un intervalo que se cruza");
        }

        CitaMedica nuevaCita = CitaMedica.builder()
                .mascota(mascota)
                .veterinario(veterinario)
                .fechaHora(request.getFechaHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .build();

        CitaMedica guardada = citaMedicaRepository.save(nuevaCita);
        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerAgenda(LocalDate fecha, Long veterinarioId) {
        LocalDateTime inicio = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime fin = fecha != null ? fecha.atTime(23, 59, 59, 999999999) : null;

        return citaMedicaRepository.findAgenda(veterinarioId, inicio, fin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CitaResponse cancelarCita(Long id, String currentUserEmail) {
        Usuario currentUser = usuarioRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + currentUserEmail));

        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con id: " + id));

        // Validar si ya está completada o cancelada
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BusinessException("No se puede cancelar una cita que ya está en estado " + cita.getEstado());
        }

        // Si es CLIENTE, solo puede cancelar sus propias citas
        if (currentUser.getRol() == Rol.CLIENTE && !cita.getMascota().getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No tienes permiso para cancelar citas de otros clientes");
        }

        // Regla 19: Una cita solamente puede cancelarse cuando faltan más de 2 horas para la hora programada
        LocalDateTime ahoraMasDosHoras = LocalDateTime.now().plusHours(2);
        if (!cita.getFechaHora().isAfter(ahoraMasDosHoras)) {
            throw new BusinessException("La cita solo puede cancelarse con más de 2 horas de anticipación");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        CitaMedica actualizada = citaMedicaRepository.save(cita);
        return mapToResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con id: " + id));
        return mapToResponse(cita);
    }

    private CitaResponse mapToResponse(CitaMedica cita) {
        return CitaResponse.builder()
                .id(cita.getId())
                .mascotaId(cita.getMascota() != null ? cita.getMascota().getId() : null)
                .mascotaNombre(cita.getMascota() != null ? cita.getMascota().getNombre() : null)
                .clienteId(cita.getMascota() != null && cita.getMascota().getCliente() != null ? cita.getMascota().getCliente().getId() : null)
                .clienteNombre(cita.getMascota() != null && cita.getMascota().getCliente() != null ? cita.getMascota().getCliente().getNombre() : null)
                .veterinarioId(cita.getVeterinario() != null ? cita.getVeterinario().getId() : null)
                .veterinarioNombre(cita.getVeterinario() != null ? cita.getVeterinario().getNombre() : null)
                .fechaHora(cita.getFechaHora())
                .fechaHoraFin(cita.getFechaHoraFin())
                .motivo(cita.getMotivo())
                .estado(cita.getEstado())
                .build();
    }
}
