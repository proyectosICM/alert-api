package com.icm.alert_api.dto.membership;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class GroupMembershipSummaryDto {

    Long id;

    Long groupId;
    Long userId;

    boolean active;

    Instant createdAt;
}
