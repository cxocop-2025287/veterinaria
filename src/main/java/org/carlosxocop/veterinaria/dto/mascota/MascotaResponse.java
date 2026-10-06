package org.carlosxocop.veterinaria.dto.mascota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.carlosxocop.veterinaria.enums.Especie;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaResponse {

    private Long id;
    private String nombre;
    private Especie especie;
    private String raza;
    private Integer edad;
    private Long clienteId;
    private String clienteNombre;
    private String clienteEmail;
}
