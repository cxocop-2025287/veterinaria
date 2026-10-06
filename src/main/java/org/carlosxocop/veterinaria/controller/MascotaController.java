package org.carlosxocop.veterinaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.mascota.MascotaRequest;
import org.carlosxocop.veterinaria.dto.mascota.MascotaResponse;
import org.carlosxocop.veterinaria.service.MascotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping("/mis-mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<MascotaResponse>> obtenerMisMascotas(Authentication authentication) {
        List<MascotaResponse> response = mascotaService.obtenerMisMascotas(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<MascotaResponse> crearMascota(
            @Valid @RequestBody MascotaRequest request,
            Authentication authentication
    ) {
        MascotaResponse response = mascotaService.crearMascota(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<MascotaResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        MascotaResponse response = mascotaService.obtenerPorId(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
