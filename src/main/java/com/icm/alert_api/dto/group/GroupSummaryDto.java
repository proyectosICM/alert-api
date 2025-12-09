package com.icm.alert_api.dto.group;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
public class GroupSummaryDto {
    Long id;
    Long companyId;
    String companyName;
    String name;
    String description;
    Instant createdAt;
    long usersCount;
    long vehiclesCount;
    long alertsLast24h;
    boolean active;
    @Size(max = 500)
    Set<@Size(max = 50) String> vehicleCodes;
}