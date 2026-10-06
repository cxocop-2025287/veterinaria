package org.carlosxocop.veterinaria.dto.cita;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequest {

    @NotNull(message = "El ID de la mascota es obligatorio")
    private Long mascotaId;

    @NotNull(message = "El ID del veterinario es obligatorio")
    private Long veterinarioId;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha y hora de la cita debe ser futura")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;
}
