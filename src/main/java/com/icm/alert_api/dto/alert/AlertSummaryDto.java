package com.icm.alert_api.dto.alert;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertSummaryDto {

    Long id;

    Long companyId;
    String companyName;

    String vehicleCode;
    String licensePlate;

    String alertType;
    String severity;

    String plant;
    String area;

    String operatorName;
    String operatorId;

    String shortDescription;

    Instant eventTime;
    Instant receivedAt;

    boolean acknowledged;
}
