package com.icm.alert_api.dto.alert;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertDetailDto {

    Long id;

    Long companyId;
    String companyName;

    String vehicleCode;
    String licensePlate;

    String alertType;       // IMPACTO, CHECKLIST, etc.
    String alertSubtype;    // "Sensor Digital Impacto", "Máquina sin condiciones", etc.

    String templateSource;  // ALARM_EMAIL, CHECKLIST_EMAIL, ...

    String severity;        // INFO, WARNING, CRITICAL, ...

    String subject;

    String plant;           // ABI-MA-PE-T1-Ate Beer
    String area;            // PATIO, BAHIAS CARPA LATAS, LINEA 10, ...

    String ownerOrVendor;   // EQUANS, etc.
    String brandModel;      // Toyota/32-8FG25, Linde/H40T-02, ...

    String operatorName;
    String operatorId;

    /**
     * Momento real del evento (normalizado a Instant).
     */
    Instant eventTime;

    /**
     * Momento en que el sistema registró la alerta (normalmente createdAt).
     */
    Instant receivedAt;

    String shortDescription;  // resumen corto
    String details;           // texto largo normalizado (markdown/PLAIN)

    String rawPayload;        // HTML / texto crudo del correo

    boolean acknowledged;
    boolean reviewed;
}
