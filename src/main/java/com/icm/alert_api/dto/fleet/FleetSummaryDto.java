package com.icm.alert_api.dto.fleet;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FleetSummaryDto {

    Long id;

    Long companyId;
    String companyName;

    String name;
    String description;

    boolean active;

    /**
     * Para listar rápido sin mandar el set completo.
     */
    long vehiclesCount;
}
