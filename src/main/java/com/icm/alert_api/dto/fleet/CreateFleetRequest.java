package com.icm.alert_api.dto.fleet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateFleetRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    /**
     * Opcional. Si no viene, por default active=true en el modelo.
     */
    private Boolean active;

    /**
     * PRINCIPAL: placas (ideal para filtros).
     * Se recomienda normalizar en backend: trim + upper + quitar espacios/guiones.
     */
    private Set<@NotBlank @Size(max = 50) String> vehiclePlates;

    /**
     * OPCIONAL/LEGACY: códigos internos (si un vehículo no tiene placa).
     * Se recomienda normalizar en backend: trim + upper + quitar espacios internos.
     */
    private Set<@NotBlank @Size(max = 50) String> vehicleCodes;
}
