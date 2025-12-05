package com.icm.alert_api.dto.group;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateGroupRequest {

    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean active;
}