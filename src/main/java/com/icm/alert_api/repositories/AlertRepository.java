package com.icm.alert_api.repositories;

import com.icm.alert_api.models.AlertModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;

public interface    AlertRepository extends JpaRepository<AlertModel, Long>, JpaSpecificationExecutor<AlertModel> {

    // Total de alertas en un dia
    long countByCompany_IdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            Long companyId,
            ZonedDateTime fromInclusive,
            ZonedDateTime toExclusive
    );

    /**
     * Historial de alertas para una lista de montacargas
     * de una empresa concreta, ordenado de más reciente a más antiguo.
     *
     * Usado para: pantalla de historial de un grupo.
     */
    Page<AlertModel> findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(
            Long companyId,
            Collection<String> vehicleCodes,
            Pageable pageable
    );

    /**
     * Historial filtrado por rango de fechas (empresa + vehículos).
     */
    Page<AlertModel> findByCompanyIdAndVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
            Long companyId,
            Collection<String> vehicleCodes,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    /**
     * Listar todas las alertas de una empresa, ordenadas por fecha.
     * (útil para el panel principal de Alerty por compañía).
     */
    Page<AlertModel> findByCompanyIdOrderByEventTimeDesc(
            Long companyId,
            Pageable pageable
    );

    /**
     * KPI: cuántas alertas han ocurrido desde un instante dado
     * (ej: now - 24h) para una lista de montacargas de una empresa.
     */
    long countByCompanyIdAndVehicleCodeInAndEventTimeAfter(
            Long companyId,
            Collection<String> vehicleCodes,
            ZonedDateTime cutoff
    );

    /**
     * KPI: cuántas alertas totales en las últimas 24h para una empresa
     * (por si quieres un dashboard general).
     */
    long countByCompanyIdAndEventTimeAfter(
            Long companyId,
            ZonedDateTime cutoff
    );

    long countByCompany_Id(Long companyId);

    long countByCompany_IdAndVehicleCodeInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            Long companyId,
            Set<String> vehicleCodes,
            ZonedDateTime from,
            ZonedDateTime to
    );
}
