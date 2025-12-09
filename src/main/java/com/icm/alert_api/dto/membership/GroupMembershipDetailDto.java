package com.icm.alert_api.dto.membership;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class GroupMembershipDetailDto {

    Long id;

    Long groupId;
    Long userId;

    boolean active;

    Instant createdAt;
    Instant updatedAt;
}
