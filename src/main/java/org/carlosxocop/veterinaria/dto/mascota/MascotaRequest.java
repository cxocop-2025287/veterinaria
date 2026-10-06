package org.carlosxocop.veterinaria.dto.mascota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "La especie es obligatoria")
    private Especie especie;

    @Size(max = 100, message = "La raza no puede exceder 100 caracteres")
    private String raza;

    @PositiveOrZero(message = "La edad debe ser 0 o un número positivo")
    private Integer edad;

    private Long clienteId;
}
