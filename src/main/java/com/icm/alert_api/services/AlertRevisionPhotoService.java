package com.icm.alert_api.services;

import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoDetailDto;
import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import com.icm.alert_api.dto.revision_photo.CreateAlertRevisionPhotoRequest;
import com.icm.alert_api.dto.revision_photo.UpdateAlertRevisionPhotoRequest;

import java.util.List;
import java.util.Optional;

public interface AlertRevisionPhotoService {

    /**
     * Lista fotos de una revisión (solo metadata, sin base64).
     */
    List<AlertRevisionPhotoSummaryDto> listByRevision(Long companyId, Long revisionId);

    /**
     * Obtiene una foto por id (incluye base64).
     */
    Optional<AlertRevisionPhotoDetailDto> findById(Long companyId, Long revisionId, Long photoId);

    /**
     * Crea una foto (base64) asociada a una revisión.
     * Recomendado: validar que revision pertenece a companyId.
     */
    AlertRevisionPhotoDetailDto create(Long companyId, CreateAlertRevisionPhotoRequest request);

    /**
     * Actualiza metadata y/o dataBase64 (si viene).
     */
    AlertRevisionPhotoDetailDto update(
            Long companyId,
            Long revisionId,
            Long photoId,
            UpdateAlertRevisionPhotoRequest request
    );

    /**
     * Borra una foto.
     */
    void deleteById(Long companyId, Long revisionId, Long photoId);

    /**
     * Reemplaza todas las fotos de la revisión (útil para "editar lista"):
     * - elimina las actuales y crea las nuevas.
     */
    List<AlertRevisionPhotoSummaryDto> replaceAll(
            Long companyId,
            Long revisionId,
            List<CreateAlertRevisionPhotoRequest> photos
    );
}
