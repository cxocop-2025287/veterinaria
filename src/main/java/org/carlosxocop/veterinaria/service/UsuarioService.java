package org.carlosxocop.veterinaria.service;

import org.carlosxocop.veterinaria.dto.usuario.UsuarioRequest;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crearUsuario(UsuarioRequest request);
    UsuarioResponse crearVeterinario(UsuarioRequest request);
    UsuarioResponse crearAdministrador(UsuarioRequest request);
    List<UsuarioResponse> obtenerVeterinarios();
    List<UsuarioResponse> obtenerTodos();
}
