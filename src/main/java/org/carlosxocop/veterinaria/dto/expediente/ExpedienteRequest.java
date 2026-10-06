package org.carlosxocop.veterinaria.dto.expediente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpedienteRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long citaId;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 1000, message = "El diagnóstico no puede exceder 1000 caracteres")
    private String diagnostico;

    @NotBlank(message = "El tratamiento es obligatorio")
    @Size(max = 1000, message = "El tratamiento no puede exceder 1000 caracteres")
    private String tratamiento;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser un número positivo")
    private Double pesoKg;
}
