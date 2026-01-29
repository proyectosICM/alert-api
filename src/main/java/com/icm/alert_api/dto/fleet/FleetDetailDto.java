package com.icm.alert_api.dto.fleet;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
public class FleetDetailDto {

    Long id;

    Long companyId;
    String companyName;

    String name;
    String description;

    boolean active;

    Set<String> vehicleCodes;

    Long version;

    Instant createdAt;
    Instant updatedAt;
}
