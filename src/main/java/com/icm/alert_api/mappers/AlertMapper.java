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
    @Mapping(target = "createdAt", ignore = true)           // @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true)           // @UpdateTimestamp
    @Mapping(target = "acknowledged", constant = "false")   // recién creada = no atendida
    AlertModel toEntity(CreateAlertRequest request);
    // Campos como vehicleCode, licensePlate, alertType, plant, etc.
    // se mapean automáticamente por nombre.

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
        // acknowledged se actualiza si viene en el request (Boolean)
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
    AlertDetailDto toDetailDto(AlertModel model);
    // El resto: vehicleCode, licensePlate, alertType, alertSubtype,
    // templateSource, severity, plant, area, ownerOrVendor, brandModel,
    // operatorName, operatorId, shortDescription, details, rawPayload,
    // acknowledged => se mapean por nombre.

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
    AlertSummaryDto toSummaryDto(AlertModel model);
}
