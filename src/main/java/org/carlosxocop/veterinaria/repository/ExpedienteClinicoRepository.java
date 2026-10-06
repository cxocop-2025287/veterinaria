package org.carlosxocop.veterinaria.repository;

import org.carlosxocop.veterinaria.entity.ExpedienteClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteClinicoRepository extends JpaRepository<ExpedienteClinico, Long> {

    Optional<ExpedienteClinico> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);

    @Query("SELECT e FROM ExpedienteClinico e WHERE e.cita.mascota.id = :mascotaId ORDER BY e.fechaRegistro DESC")
    List<ExpedienteClinico> findByMascotaId(@Param("mascotaId") Long mascotaId);
}
