package org.carlosxocop.veterinaria.repository;

import org.carlosxocop.veterinaria.entity.CitaMedica;
import org.carlosxocop.veterinaria.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    @Query("SELECT c FROM CitaMedica c WHERE c.veterinario.id = :vetId " +
           "AND c.estado <> :estadoCancelada " +
           "AND c.fechaHora > :inicio " +
           "AND c.fechaHora < :fin")
    List<CitaMedica> findConflictingAppointments(
            @Param("vetId") Long vetId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadoCancelada") EstadoCita estadoCancelada
    );

    @Query("SELECT COUNT(c) FROM CitaMedica c WHERE c.mascota.cliente.id = :clienteId " +
           "AND c.estado = :estadoPendiente " +
           "AND c.fechaHora >= :inicioDia " +
           "AND c.fechaHora <= :finDia")
    long countPendingAppointmentsForClientOnDate(
            @Param("clienteId") Long clienteId,
            @Param("estadoPendiente") EstadoCita estadoPendiente,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia
    );

    @Query("SELECT c FROM CitaMedica c WHERE (:vetId IS NULL OR c.veterinario.id = :vetId) " +
           "AND (:inicio IS NULL OR c.fechaHora >= :inicio) " +
           "AND (:fin IS NULL OR c.fechaHora <= :fin) " +
           "ORDER BY c.fechaHora ASC")
    List<CitaMedica> findAgenda(
            @Param("vetId") Long vetId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    List<CitaMedica> findByMascotaId(Long mascotaId);

    List<CitaMedica> findByVeterinarioId(Long veterinarioId);
}
