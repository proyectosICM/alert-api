package com.icm.alert_api.dto.user;

import com.icm.alert_api.enums.Role;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class GroupUserSummaryDto {
    Long id;
    Long companyId;
    String companyName;
    String fullName;
    String username;
    String dni;
    Role role;
    boolean active;
    Instant createdAt;
}
