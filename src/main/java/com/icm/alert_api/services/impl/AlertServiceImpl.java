package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import com.icm.alert_api.mappers.AlertMapper;
import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.repositories.AlertRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.services.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final NotificationGroupRepository groupRepository;
    private final AlertMapper alertMapper;

    // ============== CRUD ==============ac

    @Override
    public AlertDetailDto create(CreateAlertRequest request) {
        AlertModel model = alertMapper.toEntity(request);
        AlertModel saved = alertRepository.save(model);
        return alertMapper.toDetailDto(saved);
    }

    @Override
    public AlertDetailDto update(Long alertId, UpdateAlertRequest request) {
        AlertModel model = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        // PATCH con MapStruct (ignora nulls)
        alertMapper.updateEntityFromDto(request, model);

        AlertModel updated = alertRepository.save(model);
        return alertMapper.toDetailDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlertDetailDto> findById(Long alertId) {
        return alertRepository.findById(alertId)
                .map(alertMapper::toDetailDto);
    }

    @Override
    public void deleteById(Long alertId) {
        if (!alertRepository.existsById(alertId)) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }
        alertRepository.deleteById(alertId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listAll(Pageable pageable) {
        Page<AlertModel> page = alertRepository.findAllByOrderByEventTimeDesc(pageable);
        return page.map(alertMapper::toSummaryDto);
    }

    // ============== Historial por grupo ==============

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByGroup(Long groupId, Pageable pageable) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            // Grupo sin montacargas asignados => no hay alertas
            return Page.empty(pageable);
        }

        Page<AlertModel> page = alertRepository
                .findByVehicleCodeInOrderByEventTimeDesc(vehicleCodes, pageable);

        return page.map(alertMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByGroupAndDateRange(
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    ) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<AlertModel> page = alertRepository
                .findByVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
                        vehicleCodes, from, to, pageable
                );

        return page.map(alertMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLast24hForGroup(Long groupId) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return 0L;
        }

        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(24);
        return alertRepository.countByVehicleCodeInAndEventTimeAfter(vehicleCodes, cutoff);
    }

    @Override
    public AlertDetailDto acknowledge(Long alertId) {
        AlertModel model = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        // Si ya está marcada, simplemente devolvemos el detalle
        if (!model.isAcknowledged()) {           // 👈 AQUÍ el cambio
            model.setAcknowledged(true);

            // Si luego agregas un campo acknowledgedAt:
            // model.setAcknowledgedAt(ZonedDateTime.now());

            alertRepository.save(model);
        }

        return alertMapper.toDetailDto(model);
    }
}
