package com.icm.alert_api.dto.fleet;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class FleetSummaryDto {

    Long id;

    Long companyId;
    String companyName;

    String name;
    String description;

    boolean active;

    Set<String> vehicleCodes;
    long vehiclesCount;
}
