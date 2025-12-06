package com.icm.alert_api.repositories;

import com.icm.alert_api.models.AlertModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface AlertRepository extends JpaRepository<AlertModel, Long> {

    /**
     * Historial de alertas para una lista de montacargas,
     * ordenado de más reciente a más antiguo.
     *
     * Usado para: pantalla de historial de un grupo.
     */
    Page<AlertModel> findByVehicleCodeInOrderByEventTimeDesc(
            Collection<String> vehicleCodes,
            Pageable pageable
    );

    /**
     * Historial filtrado por rango de fechas.
     * Ej: últimas N horas o entre dos fechas.
     */
    Page<AlertModel> findByVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
            Collection<String> vehicleCodes,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    Page<AlertModel> findAllByOrderByEventTimeDesc(Pageable pageable);

    /**
     * KPI: cuántas alertas han ocurrido desde un instante dado
     * (ej: now - 24h) para una lista de montacargas.
     * Perfecto para "alertsLast24h" del GroupDetailDto.
     */
    long countByVehicleCodeInAndEventTimeAfter(
            Collection<String> vehicleCodes,
            ZonedDateTime cutoff
    );
}
