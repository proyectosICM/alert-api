package com.icm.alert_api.dto.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CreateAlertRequest {

    /**
     * Código / placa del montacargas (ej: "MG069" o "FG22010").
     */
    @NotBlank
    @Size(max = 50)
    private String vehicleCode;

    /**
     * Placa/patente visible en el correo (opcional si coincide con vehicleCode).
     */
    @Size(max = 50)
    private String licensePlate;

    /**
     * Tipo de alerta (IMPACTO, CHECKLIST, EXCESO_VELOCIDAD, etc.).
     */
    @NotBlank
    @Size(max = 50)
    private String alertType;

    /**
     * Subtipo / disparador: "Sensor Digital Impacto", "Máquina sin condiciones", etc.
     */
    @Size(max = 80)
    private String alertSubtype;

    /**
     * Origen / plantilla del correo.
     */
    @Size(max = 40)
    private String templateSource;

    /**
     * Severidad: INFO, WARNING, CRITICAL, BLOQUEA_OPERACION, etc.
     */
    @Size(max = 20)
    private String severity;

    @Size(max = 255)
    private String subject;

    @Size(max = 150)
    private String plant;

    @Size(max = 100)
    private String area;

    @Size(max = 150)
    private String ownerOrVendor;

    @Size(max = 150)
    private String brandModel;

    @Size(max = 150)
    private String operatorName;

    @Size(max = 100)
    private String operatorId;

    /**
     * Momento real del evento (el que viene en el correo).
     */
    @NotNull
    private ZonedDateTime eventTime;

    @Size(max = 1000)
    private String shortDescription;

    /**
     * Detalle largo normalizado (por ej. lista de checks, observaciones, etc.).
     */
    private String details;

    /**
     * Payload crudo (texto/HTML completo del correo o bloque relevante).
     */
    @NotBlank
    private String rawPayload;
}
