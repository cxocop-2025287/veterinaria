package org.carlosxocop.veterinaria.service;

import org.carlosxocop.veterinaria.dto.auth.AuthResponse;
import org.carlosxocop.veterinaria.dto.auth.LoginRequest;
import org.carlosxocop.veterinaria.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
