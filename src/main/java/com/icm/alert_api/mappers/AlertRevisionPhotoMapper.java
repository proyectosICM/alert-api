package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoDetailDto;
import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import com.icm.alert_api.dto.revision_photo.CreateAlertRevisionPhotoRequest;
import com.icm.alert_api.dto.revision_photo.UpdateAlertRevisionPhotoRequest;
import com.icm.alert_api.models.AlertRevisionPhotoModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Base64;

@Mapper(componentModel = "spring")
public interface AlertRevisionPhotoMapper {

    // =========================
    // Helpers Base64
    // =========================

    default String toBase64(byte[] data) {
        if (data == null || data.length == 0) return null;
        return Base64.getEncoder().encodeToString(data);
    }

    default byte[] fromBase64(String base64) {
        if (base64 == null || base64.isBlank()) return null;
        return Base64.getDecoder().decode(base64);
    }

    // =========================
    // Create DTO -> Entity
    // =========================
    // revision se setea en el Service: buscar AlertRevisionModel por revisionId y asignar.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "data", expression = "java(fromBase64(request.getDataBase64()))")
    AlertRevisionPhotoModel toEntity(CreateAlertRevisionPhotoRequest request);

    // =========================
    // Update DTO -> Entity (PATCH)
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "data", expression = "java(request.getDataBase64() != null ? fromBase64(request.getDataBase64()) : entity.getData())")
    void updateEntityFromDto(UpdateAlertRevisionPhotoRequest request, @MappingTarget AlertRevisionPhotoModel entity);

    // =========================
    // Entity -> Summary DTO (lista)
    // =========================
    @Mapping(
            target = "revisionId",
            expression = "java(model.getRevision() != null ? model.getRevision().getId() : null)"
    )
    @Mapping(
            target = "sizeBytes",
            expression = "java(model.getData() != null ? model.getData().length : 0)"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    AlertRevisionPhotoSummaryDto toSummaryDto(AlertRevisionPhotoModel model);

    // =========================
    // Entity -> Detail DTO (incluye base64)
    // =========================
    @Mapping(
            target = "revisionId",
            expression = "java(model.getRevision() != null ? model.getRevision().getId() : null)"
    )
    @Mapping(
            target = "sizeBytes",
            expression = "java(model.getData() != null ? model.getData().length : 0)"
    )
    @Mapping(
            target = "dataBase64",
            expression = "java(toBase64(model.getData()))"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    AlertRevisionPhotoDetailDto toDetailDto(AlertRevisionPhotoModel model);
}
