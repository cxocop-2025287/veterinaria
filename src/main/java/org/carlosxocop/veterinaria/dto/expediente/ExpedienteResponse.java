package org.carlosxocop.veterinaria.dto.expediente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpedienteResponse {

    private Long id;
    private Long citaId;
    private Long mascotaId;
    private String mascotaNombre;
    private Long veterinarioId;
    private String veterinarioNombre;
    private String diagnostico;
    private String tratamiento;
    private Double pesoKg;
    private LocalDateTime fechaRegistro;
}
