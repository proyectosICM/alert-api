package com.icm.alert_api.dto.membership;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupMembershipRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private Long userId;

    // Si quieres, podrías permitir crearla ya desactivada
    private Boolean active;
}
