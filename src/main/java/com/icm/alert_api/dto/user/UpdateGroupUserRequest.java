package com.icm.alert_api.dto.user;

import com.icm.alert_api.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateGroupUserRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 60)
    private String username;

    @Size(max = 15)
    private String dni;

    @Size(min = 6, max = 120)
    private String password;

    private Role role;

    /**
     * Si luego agregas un campo "active" en UserModel,
     * aquí lo puedes controlar también.
     */
    private Boolean active;
}
