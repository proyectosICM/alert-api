package com.icm.alert_api.services;

import com.icm.alert_api.dto.revision.AlertRevisionDetailDto;
import com.icm.alert_api.dto.revision.AlertRevisionSummaryDto;
import com.icm.alert_api.dto.revision.CreateAlertRevisionRequest;
import com.icm.alert_api.dto.revision.UpdateAlertRevisionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AlertRevisionService {

    /**
     * Crea una revisión para una alerta.
     * Recomendación: 1 revisión por alerta (validar en servicio si ya existe).
     */
    AlertRevisionDetailDto create(Long companyId, CreateAlertRevisionRequest request);

    /**
     * Actualiza campos editables de la revisión (sin fotos).
     */
    AlertRevisionDetailDto update(Long companyId, Long revisionId, UpdateAlertRevisionRequest request);

    /**
     * Obtiene revisión por id (idealmente con fotos en detail).
     */
    Optional<AlertRevisionDetailDto> findById(Long companyId, Long revisionId);

    /**
     * Obtiene revisión por alertId (si tu regla es 1 revisión por alerta).
     */
    Optional<AlertRevisionDetailDto> findByAlertId(Long companyId, Long alertId);

    /**
     * Elimina revisión (y por cascade/orphanRemoval elimina fotos).
     */
    void deleteById(Long companyId, Long revisionId);

    /**
     * Lista revisiones por empresa.
     */
    Page<AlertRevisionSummaryDto> listAll(Long companyId, Pageable pageable);

    /**
     * Lista revisiones por alerta (si permites múltiples; si no, igual sirve).
     */
    Page<AlertRevisionSummaryDto> listByAlert(Long companyId, Long alertId, Pageable pageable);

    /**
     * Helper: existe revisión para alerta.
     */
    boolean existsForAlert(Long companyId, Long alertId);
}
