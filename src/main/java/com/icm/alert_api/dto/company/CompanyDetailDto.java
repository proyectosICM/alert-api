package com.icm.alert_api.dto.company;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CompanyDetailDto {

    Long id;
    String name;

    long usersCount;
    long groupsCount;
    long alertsLast24h;

    Instant createdAt;
    Instant updatedAt;
}
