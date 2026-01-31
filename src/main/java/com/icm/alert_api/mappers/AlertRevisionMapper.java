package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.revision.AlertRevisionDetailDto;
import com.icm.alert_api.dto.revision.AlertRevisionSummaryDto;
import com.icm.alert_api.dto.revision.CreateAlertRevisionRequest;
import com.icm.alert_api.dto.revision.UpdateAlertRevisionRequest;
import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import com.icm.alert_api.models.AlertRevisionModel;
import com.icm.alert_api.models.AlertRevisionPhotoModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AlertRevisionMapper {

    // ======= Create DTO -> Entity =======
    // Nota: alert (relación) normalmente la setea el Service buscando AlertModel por alertId,
    // por eso lo ignoramos acá.

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alert", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "fotos", ignore = true)     // se manejan por endpoint/servicio (multipart)
    AlertRevisionModel toEntity(CreateAlertRevisionRequest request);

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alert", ignore = true)
    @Mapping(target = "companyId", ignore = true) // si no quieres permitir cambiar empresa por PATCH
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "fotos", ignore = true)     // fotos se actualizan con endpoints separados
    void updateEntityFromDto(UpdateAlertRevisionRequest request, @MappingTarget AlertRevisionModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(
            target = "alertId",
            expression = "java(model.getAlert() != null ? model.getAlert().getId() : null)"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(target = "fotos", source = "fotos")
    AlertRevisionDetailDto toDetailDto(AlertRevisionModel model);

    // ======= Entity -> Summary DTO =======

    @Mapping(
            target = "alertId",
            expression = "java(model.getAlert() != null ? model.getAlert().getId() : null)"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "photosCount",
            expression = "java(model.getFotos() != null ? model.getFotos().size() : 0)"
    )
    AlertRevisionSummaryDto toSummaryDto(AlertRevisionModel model);

    // ======= Photo: Entity -> DTO =======

    @Mapping(
            target = "createdAt",
            expression = "java(photo.getCreatedAt() != null ? photo.getCreatedAt().toInstant() : null)"
    )
    AlertRevisionPhotoSummaryDto toPhotoDto(AlertRevisionPhotoModel photo);
}
