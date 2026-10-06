package org.carlosxocop.veterinaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteRequest;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteResponse;
import org.carlosxocop.veterinaria.service.ExpedienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expedientes")
@RequiredArgsConstructor
public class ExpedienteController {

    private final ExpedienteService expedienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<ExpedienteResponse> crearExpediente(
            @Valid @RequestBody ExpedienteRequest request,
            Authentication authentication
    ) {
        ExpedienteResponse response = expedienteService.crearExpediente(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mascota/{mascotaId}")
    @PreAuthorize("hasAnyRole('VET', 'CLIENTE', 'ADMIN')")
    public ResponseEntity<List<ExpedienteResponse>> obtenerHistorialPorMascota(
            @PathVariable Long mascotaId,
            Authentication authentication
    ) {
        List<ExpedienteResponse> historial = expedienteService.obtenerHistorialPorMascota(mascotaId, authentication.getName());
        return ResponseEntity.ok(historial);
    }
}
