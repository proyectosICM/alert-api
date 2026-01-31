package com.icm.alert_api.dto.revision_photo;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertRevisionPhotoDetailDto {

    Long id;
    Long revisionId;

    String fileName;
    String contentType;

    Integer sizeBytes;

    /**
     * Base64 puro (sin prefijo).
     * Si quieres dataUrl, lo puedes armar en frontend:
     * `data:${contentType};base64,${dataBase64}`
     */
    String dataBase64;

    Instant createdAt;
}
