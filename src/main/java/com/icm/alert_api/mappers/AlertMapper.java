package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import com.icm.alert_api.models.AlertModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)       // lo pone @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true)       // lo pone @UpdateTimestamp
    @Mapping(target = "acknowledged", constant = "false")   // recién creada = no atendida
    AlertModel toEntity(CreateAlertRequest request);

    // ======= Update DTO -> Entity (patch) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)       // no se toca
    @Mapping(target = "updatedAt", ignore = true)       // no se toca
        // dependiendo de tu lógica, puedes permitir cambiar `acknowledged` o no:
        // @Mapping(target = "acknowledged", ignore = true)
    void updateEntityFromDto(UpdateAlertRequest request, @MappingTarget AlertModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt", // en el DTO
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    AlertDetailDto toDetailDto(AlertModel model);

    // ======= Entity -> Summary DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt", // en el DTO
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    AlertSummaryDto toSummaryDto(AlertModel model);
}
