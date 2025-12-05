package com.icm.alert_api.dto.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CreateAlertRequest {

    /**
     * Código / placa del montacargas (ej: "MG069").
     */
    @NotBlank
    @Size(max = 50)
    private String vehicleCode;

    /**
     * Tipo de alerta (IMPACTO, EXCESO_VELOCIDAD, etc.).
     */
    @NotBlank
    @Size(max = 50)
    private String type;

    @Size(max = 255)
    private String subject;

    @Size(max = 100)
    private String area;

    @Size(max = 150)
    private String operatorName;

    @Size(max = 100)
    private String operatorId;

    /**
     * Momento real del evento (el que viene en el correo).
     * Puedes enviarlo en ISO-8601 y parsearlo en el controlador.
     */
    @NotNull
    private ZonedDateTime eventTime;

    @Size(max = 500)
    private String shortDescription;

    /**
     * Payload crudo (texto completo del correo o bloque relevante).
     */
    private String rawPayload;
}
