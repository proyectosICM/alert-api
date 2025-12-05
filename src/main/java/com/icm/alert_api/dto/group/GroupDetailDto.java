package com.icm.alert_api.dto.group;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class GroupDetailDto {
    Long id;
    String name;
    String description;
    Instant createdAt;
    boolean active;
    long usersCount;
    long alertsLast24h;
}
