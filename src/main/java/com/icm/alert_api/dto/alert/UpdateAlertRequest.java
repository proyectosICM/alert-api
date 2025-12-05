package com.icm.alert_api.dto.alert;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAlertRequest {

    @Size(max = 500)
    private String shortDescription;

    /**
     * Permite marcar una alerta como atendida / no atendida.
     */
    private Boolean acknowledged;
}
