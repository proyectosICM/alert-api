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
    @Mapping(target = "receivedAt", ignore = true)          // lo pone @CreationTimestamp
    @Mapping(target = "acknowledged", constant = "false")   // recién creada = no atendida
    AlertModel toEntity(CreateAlertRequest request);

    // ======= Update DTO -> Entity (patch) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receivedAt", ignore = true)          // no se toca
    void updateEntityFromDto(UpdateAlertRequest request, @MappingTarget AlertModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt",
            expression = "java(model.getReceivedAt() != null ? model.getReceivedAt().toInstant() : null)"
    )
    AlertDetailDto toDetailDto(AlertModel model);

    // ======= Entity -> Summary DTO =======

    @Mapping(
            target = "eventTime",
            expression = "java(model.getEventTime() != null ? model.getEventTime().toInstant() : null)"
    )
    @Mapping(
            target = "receivedAt",
            expression = "java(model.getReceivedAt() != null ? model.getReceivedAt().toInstant() : null)"
    )
    AlertSummaryDto toSummaryDto(AlertModel model);
}
