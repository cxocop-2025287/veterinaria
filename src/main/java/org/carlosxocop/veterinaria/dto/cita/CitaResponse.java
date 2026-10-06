package org.carlosxocop.veterinaria.dto.cita;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.carlosxocop.veterinaria.enums.EstadoCita;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponse {

    private Long id;
    private Long mascotaId;
    private String mascotaNombre;
    private Long clienteId;
    private String clienteNombre;
    private Long veterinarioId;
    private String veterinarioNombre;
    private LocalDateTime fechaHora;
    private LocalDateTime fechaHoraFin;
    private String motivo;
    private EstadoCita estado;
}
