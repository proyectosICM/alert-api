package com.icm.alert_api.services;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface AlertService {

    // ======= CRUD básico (para el script Python, etc.) =======

    AlertDetailDto create(CreateAlertRequest request);

    AlertDetailDto update(Long alertId, UpdateAlertRequest request);

    Optional<AlertDetailDto> findById(Long alertId);

    void deleteById(Long alertId);

    // ======= Historial por grupo =======

    /**
     * Lista de alertas para un grupo, usando sus vehicleCodes,
     * ordenadas de más reciente a más antigua.
     */
    Page<AlertSummaryDto> listByGroup(Long groupId, Pageable pageable);

    /**
     * Lista de alertas para un grupo, filtradas por rango de fechas.
     */
    Page<AlertSummaryDto> listByGroupAndDateRange(
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    /**
     * KPI: cuántas alertas ha tenido el grupo en las últimas 24 horas.
     * Útil para el campo alertsLast24h del GroupDetailDto.
     */
    long countLast24hForGroup(Long groupId);
}
