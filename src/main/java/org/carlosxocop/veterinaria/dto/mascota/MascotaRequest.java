package org.carlosxocop.veterinaria.dto.mascota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.carlosxocop.veterinaria.enums.Especie;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaRequest {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String nombre;

    @NotNull(message = "La especie es obligatoria")
    private Especie especie;

    private String raza;

    @Positive(message = "La edad debe ser un número positivo")
    private Integer edad;

    private Long clienteId;
}
