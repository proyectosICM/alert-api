package com.icm.alert_api.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateGroupRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean active;

    @Size(max = 500)
    private Set<@Size(max = 50) String> vehicleCodes;
}
