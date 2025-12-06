package com.icm.alert_api.dto.alert;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAlertRequest {

    @Size(max = 1000)
    private String shortDescription;

    private String details;

    /**
     * Posible actualización de severidad, si la quieres ajustar después.
     */
    @Size(max = 20)
    private String severity;

    /**
     * Permite marcar una alerta como atendida / no atendida.
     */
    private Boolean acknowledged;
}
