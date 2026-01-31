package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.revision.AlertRevisionDetailDto;
import com.icm.alert_api.dto.revision.AlertRevisionSummaryDto;
import com.icm.alert_api.dto.revision.CreateAlertRevisionRequest;
import com.icm.alert_api.dto.revision.UpdateAlertRevisionRequest;
import com.icm.alert_api.mappers.AlertRevisionMapper;
import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.AlertRevisionModel;
import com.icm.alert_api.repositories.AlertRepository;
import com.icm.alert_api.repositories.AlertRevisionRepository;
import com.icm.alert_api.services.AlertRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertRevisionServiceImpl implements AlertRevisionService {

    private final AlertRevisionRepository revisionRepository;
    private final AlertRepository alertRepository;
    private final AlertRevisionMapper revisionMapper;

    @Override
    public AlertRevisionDetailDto create(Long companyId, CreateAlertRevisionRequest request) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getAlertId() == null) throw new IllegalArgumentException("alertId is required");

        Long alertId = request.getAlertId();

        AlertModel alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (alert.getCompany() == null || !alert.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Alert does not belong to company: " + companyId);
        }

        // Regla: 1 revisión por alerta (si esa es tu regla)
        if (revisionRepository.existsByCompanyIdAndAlert_Id(companyId, alertId)) {
            throw new IllegalArgumentException("Revision already exists for alert: " + alertId);
        }

        AlertRevisionModel model = revisionMapper.toEntity(request);
        model.setAlert(alert);
        model.setCompanyId(companyId);

        AlertRevisionModel saved = revisionRepository.save(model);
        return revisionMapper.toDetailDto(saved);
    }

    @Override
    public AlertRevisionDetailDto update(Long companyId, Long revisionId, UpdateAlertRevisionRequest request) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (revisionId == null) throw new IllegalArgumentException("revisionId is required");

        AlertRevisionModel model = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found: " + revisionId));

        if (!model.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Revision does not belong to company: " + companyId);
        }

        revisionMapper.updateEntityFromDto(request, model);

        AlertRevisionModel updated = revisionRepository.save(model);
        return revisionMapper.toDetailDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<AlertRevisionDetailDto> findById(Long companyId, Long revisionId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (revisionId == null) throw new IllegalArgumentException("revisionId is required");

        return revisionRepository.findById(revisionId)
                .filter(r -> r.getCompanyId().equals(companyId))
                .map(revisionMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<AlertRevisionDetailDto> findByAlertId(Long companyId, Long alertId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (alertId == null) throw new IllegalArgumentException("alertId is required");

        return revisionRepository.findByCompanyIdAndAlert_Id(companyId, alertId)
                .map(revisionMapper::toDetailDto);
    }

    @Override
    public void deleteById(Long companyId, Long revisionId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (revisionId == null) throw new IllegalArgumentException("revisionId is required");

        AlertRevisionModel model = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found: " + revisionId));

        if (!model.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Revision does not belong to company: " + companyId);
        }

        // Cascade + orphanRemoval elimina fotos
        revisionRepository.delete(model);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertRevisionSummaryDto> listAll(Long companyId, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        return revisionRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                .map(revisionMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertRevisionSummaryDto> listByAlert(Long companyId, Long alertId, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (alertId == null) throw new IllegalArgumentException("alertId is required");

        return revisionRepository.findByCompanyIdAndAlert_IdOrderByCreatedAtDesc(companyId, alertId, pageable)
                .map(revisionMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForAlert(Long companyId, Long alertId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (alertId == null) throw new IllegalArgumentException("alertId is required");
        return revisionRepository.existsByCompanyIdAndAlert_Id(companyId, alertId);
    }
}
