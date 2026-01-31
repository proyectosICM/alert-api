package com.icm.alert_api.dto.revision;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder
public class AlertRevisionSummaryDto {

    Long id;

    Long companyId;

    Long alertId;

    String vehiculo;
    String planta;
    String area;
    String operador;

    LocalDate fechaFalla;

    String revisorNombre;

    Instant createdAt;

    /**
     * Útil para UI: cuántas fotos tiene (sin traerlas).
     */
    int photosCount;
}
