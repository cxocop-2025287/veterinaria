package org.carlosxocop.veterinaria.service;

import org.carlosxocop.veterinaria.dto.mascota.MascotaRequest;
import org.carlosxocop.veterinaria.dto.mascota.MascotaResponse;

import java.util.List;

public interface MascotaService {
    MascotaResponse crearMascota(MascotaRequest request, String currentUserEmail);
    List<MascotaResponse> obtenerMisMascotas(String currentUserEmail);
    MascotaResponse obtenerPorId(Long id, String currentUserEmail);
}
