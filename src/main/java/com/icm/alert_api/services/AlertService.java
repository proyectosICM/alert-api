package com.icm.alert_api.services;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

public interface AlertService {

    // CRUD
    AlertDetailDto create(CreateAlertRequest request);

    AlertDetailDto update(Long companyId, Long alertId, UpdateAlertRequest request);

    Optional<AlertDetailDto> findById(Long companyId, Long alertId);

    void deleteById(Long companyId, Long alertId);

    Page<AlertSummaryDto> listAll(Long companyId, Pageable pageable);

    // Historial por grupo
    Page<AlertSummaryDto> listByGroup(Long companyId, Long groupId, Pageable pageable);

    Page<AlertSummaryDto> listByGroupAndDateRange(
            Long companyId,
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    long countLast24hForGroup(Long companyId, Long groupId);

    AlertDetailDto acknowledge(Long companyId, Long alertId);

    Page<AlertSummaryDto> listByUser(Long companyId, Long userId, Pageable pageable);

    long countByDay(Long companyId, LocalDate day, ZoneId zone, Long fleetId);


    Page<AlertSummaryDto> search(
            Long companyId,
            Set<String> alertTypes,
            Long fleetId,
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Boolean acknowledged,
            Pageable pageable
    );
}
