package org.carlosxocop.veterinaria.service;

import org.carlosxocop.veterinaria.dto.expediente.ExpedienteRequest;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteResponse;

import java.util.List;

public interface ExpedienteService {
    ExpedienteResponse crearExpediente(ExpedienteRequest request, String currentUserEmail);
    List<ExpedienteResponse> obtenerHistorialPorMascota(Long mascotaId, String currentUserEmail);
}
