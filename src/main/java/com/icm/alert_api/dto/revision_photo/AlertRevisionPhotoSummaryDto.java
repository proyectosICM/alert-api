package com.icm.alert_api.dto.revision_photo;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertRevisionPhotoSummaryDto {

    Long id;
    Long revisionId;

    String fileName;
    String contentType;

    Integer sizeBytes;

    Instant createdAt;
}
