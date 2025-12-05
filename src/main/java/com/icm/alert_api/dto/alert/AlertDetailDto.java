package com.icm.alert_api.dto.alert;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertDetailDto {

    Long id;

    String vehicleCode;
    String type;
    String subject;
    String area;
    String operatorName;
    String operatorId;

    /**
     * Momento real del evento (normalizado a Instant).
     */
    Instant eventTime;

    /**
     * Momento en que el sistema registró la alerta.
     */
    Instant receivedAt;

    String shortDescription;

    String rawPayload;

    boolean acknowledged;
}
