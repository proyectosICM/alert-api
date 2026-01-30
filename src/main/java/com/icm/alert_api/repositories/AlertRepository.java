package com.icm.alert_api.repositories;

import com.icm.alert_api.models.AlertModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;

public interface AlertRepository extends JpaRepository<AlertModel, Long>, JpaSpecificationExecutor<AlertModel> {

    // Total de alertas en un dia (empresa)
    long countByCompany_IdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            Long companyId,
            ZonedDateTime fromInclusive,
            ZonedDateTime toExclusive
    );

    // =========================
    // HISTORIAL por VEHICLE CODE
    // =========================

    Page<AlertModel> findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(
            Long companyId,
            Collection<String> vehicleCodes,
            Pageable pageable
    );

    Page<AlertModel> findByCompanyIdAndVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
            Long companyId,
            Collection<String> vehicleCodes,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    long countByCompanyIdAndVehicleCodeInAndEventTimeAfter(
            Long companyId,
            Collection<String> vehicleCodes,
            ZonedDateTime cutoff
    );

    long countByCompany_IdAndVehicleCodeInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            Long companyId,
            Set<String> vehicleCodes,
            ZonedDateTime from,
            ZonedDateTime to
    );

    // =========================
    // HISTORIAL por LICENSE PLATE (✅ NUEVO, PRINCIPAL)
    // =========================

    Page<AlertModel> findByCompanyIdAndLicensePlateInOrderByEventTimeDesc(
            Long companyId,
            Collection<String> licensePlates,
            Pageable pageable
    );

    Page<AlertModel> findByCompanyIdAndLicensePlateInAndEventTimeBetweenOrderByEventTimeDesc(
            Long companyId,
            Collection<String> licensePlates,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    long countByCompanyIdAndLicensePlateInAndEventTimeAfter(
            Long companyId,
            Collection<String> licensePlates,
            ZonedDateTime cutoff
    );

    long countByCompanyIdAndLicensePlateInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            Long companyId,
            Collection<String> licensePlates,
            ZonedDateTime from,
            ZonedDateTime to
    );

    // =========================
    // Empresa general
    // =========================

    Page<AlertModel> findByCompanyIdOrderByEventTimeDesc(
            Long companyId,
            Pageable pageable
    );

    long countByCompanyIdAndEventTimeAfter(
            Long companyId,
            ZonedDateTime cutoff
    );

    long countByCompany_Id(Long companyId);
}
