package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import com.icm.alert_api.models.AlertModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "acknowledged", constant = "false")
    AlertModel toEntity(CreateAlertRequest request);

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateAlertRequest request, @MappingTarget AlertModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    // ✅ NUEVO: revisado = existe revision
    @Mapping(
            target = "reviewed",
            expression = "java(model.getRevision() != null)"
    )
    AlertDetailDto toDetailDto(AlertModel model);

    // ======= Entity -> Summary DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    // ✅ NUEVO: revisado = existe revision
    @Mapping(
            target = "reviewed",
            expression = "java(model.getRevision() != null)"
    )
    AlertSummaryDto toSummaryDto(AlertModel model);
}
