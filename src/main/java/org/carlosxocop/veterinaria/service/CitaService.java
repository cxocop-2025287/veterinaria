package org.carlosxocop.veterinaria.service;

import org.carlosxocop.veterinaria.dto.cita.CitaRequest;
import org.carlosxocop.veterinaria.dto.cita.CitaResponse;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {
    CitaResponse crearCita(CitaRequest request, String currentUserEmail);
    List<CitaResponse> obtenerAgenda(LocalDate fecha, Long veterinarioId);
    List<CitaResponse> obtenerMisCitas(String currentUserEmail);
    CitaResponse cancelarCita(Long id, String currentUserEmail);
    CitaResponse obtenerPorId(Long id);
}
