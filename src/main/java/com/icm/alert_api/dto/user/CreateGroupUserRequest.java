package com.icm.alert_api.dto.user;

import com.icm.alert_api.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupUserRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Size(max = 60)
    private String username;

    @NotBlank
    @Size(max = 15)
    private String dni;

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;

    /**
     * Rol dentro del grupo (ADMIN / USER).
     * Si quieres que sea opcional y por defecto USER,
     * puedes cambiarlo a Role y manejar el default en el servicio.
     */
    @NotNull
    private Role role;
}
