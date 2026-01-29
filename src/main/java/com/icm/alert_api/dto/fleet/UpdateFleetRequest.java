package com.icm.alert_api.dto.fleet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateFleetRequest {

    /**
     * Mantengo la misma lógica que en groups:
     * el update valida que la flota pertenece a la company del request.
     */
    @NotNull
    private Long companyId;

    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean active;

    /**
     * Si lo envías, reemplaza el set completo (PATCH “replace”).
     * Si es null, no toca los vehicleCodes.
     */
    private Set<@Size(max = 50) String> vehicleCodes;
}
