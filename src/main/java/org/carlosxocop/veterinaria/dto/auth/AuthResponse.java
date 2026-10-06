package org.carlosxocop.veterinaria.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.carlosxocop.veterinaria.enums.Rol;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tipo = "Bearer";

    private Rol rol;

    private String email;

    private String nombre;
}
