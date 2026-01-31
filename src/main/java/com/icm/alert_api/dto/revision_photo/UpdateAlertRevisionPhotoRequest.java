package com.icm.alert_api.dto.revision_photo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAlertRevisionPhotoRequest {

    @Size(max = 200)
    private String fileName;

    @Size(max = 60)
    private String contentType;

    /**
     * Si viene, reemplaza la data.
     * Si no viene, se mantiene la data actual.
     */
    private String dataBase64;
}
