package com.icm.alert_api.dto.revision_photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAlertRevisionPhotoRequest {

    @NotNull
    private Long revisionId;

    @Size(max = 200)
    private String fileName;

    @Size(max = 60)
    private String contentType; // image/jpeg, image/png

    /**
     * Base64 puro (sin "data:image/...;base64,")
     * Si quieres aceptar dataUrl también, lo parseas en service.
     */
    @NotBlank
    private String dataBase64;
}
