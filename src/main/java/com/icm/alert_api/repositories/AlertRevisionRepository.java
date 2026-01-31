package com.icm.alert_api.repositories;

import com.icm.alert_api.models.AlertRevisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRevisionRepository extends JpaRepository<AlertRevisionModel, Long> {

    // Listar por empresa
    Page<AlertRevisionModel> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    // Listar por alerta (dentro de una empresa)
    Page<AlertRevisionModel> findByCompanyIdAndAlert_IdOrderByCreatedAtDesc(
            Long companyId,
            Long alertId,
            Pageable pageable
    );

    // Obtener 1 revisión por alerta (si manejas 1:1)
    Optional<AlertRevisionModel> findByCompanyIdAndAlert_Id(Long companyId, Long alertId);

    // Validar existencia (1 revisión por alerta)
    boolean existsByCompanyIdAndAlert_Id(Long companyId, Long alertId);
}
