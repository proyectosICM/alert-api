package com.icm.alert_api.dto.group;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class GroupSummaryDto {
    Long id;
    String name;
    String description;
    Instant createdAt;
    long usersCount;
    long vehiclesCount;
    long alertsLast24h;
    boolean active;
}