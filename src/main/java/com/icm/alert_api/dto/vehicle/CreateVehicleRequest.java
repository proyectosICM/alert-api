package com.icm.alert_api.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVehicleRequest {

    /**
     * Código como llega (puede venir con espacios, minúsculas, etc.)
     * Ej: "fg-22010 ", " MG069"
     */
    @NotBlank
    @Size(max = 50)
    private String vehicleCodeRaw;

    /**
     * Normalizado (trim + upper + quitar espacios internos).
     * Si prefieres calcularlo en backend, puedes hacerlo opcional.
     */
    @NotBlank
    @Size(max = 50)
    private String vehicleCodeNorm;

    @Size(max = 50)
    private String licensePlate;

    @NotNull
    private Long companyId;
}
