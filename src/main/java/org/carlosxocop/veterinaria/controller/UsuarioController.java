package org.carlosxocop.veterinaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioRequest;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioResponse;
import org.carlosxocop.veterinaria.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/veterinarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crearVeterinario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crearVeterinario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crearAdministrador(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crearAdministrador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/veterinarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'VET')")
    public ResponseEntity<List<UsuarioResponse>> obtenerVeterinarios() {
        List<UsuarioResponse> veterinarios = usuarioService.obtenerVeterinarios();
        return ResponseEntity.ok(veterinarios);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }
}
