package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.models.NotificationGroupModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationGroupMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)     // company se setea en el service
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // si no envían "active", por defecto true
    @Mapping(
            target = "active",
            expression = "java(request.getActive() != null ? request.getActive() : true)"
    )
    // inicializamos siempre el Set de vehicleCodes
    @Mapping(
            target = "vehicleCodes",
            expression =
                    "java(request.getVehicleCodes() != null " +
                            "? new java.util.HashSet<>(request.getVehicleCodes()) " +
                            ": new java.util.HashSet<>())"
    )
    NotificationGroupModel toEntity(CreateGroupRequest request);

    // ======= Update DTO -> Entity (patch) =======
    // Usamos IGNORE y luego tratamos vehicleCodes en @AfterMapping

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)     // no cambiamos la company desde el DTO
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "vehicleCodes", ignore = true) // lo manejamos a mano
    void updateEntityFromDto(UpdateGroupRequest request,
                             @MappingTarget NotificationGroupModel entity);

    @AfterMapping
    default void afterUpdate(UpdateGroupRequest request,
                             @MappingTarget NotificationGroupModel entity) {
        // Regla:
        // - vehicleCodes == null  -> no tocar
        // - vehicleCodes == []    -> limpiar todos
        // - vehicleCodes con data -> reemplazar
        if (request.getVehicleCodes() != null) {
            entity.getVehicleCodes().clear();
            entity.getVehicleCodes().addAll(request.getVehicleCodes());
        }
    }

    // ======= Entity -> Detail DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(
            target = "createdAt",
            expression =
                    "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    GroupDetailDto toDetailDto(NotificationGroupModel model,
                               long usersCount,
                               long alertsLast24h);

    // ======= Entity -> Summary DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(
            target = "createdAt",
            expression =
                    "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "vehiclesCount",
            expression =
                    "java(model.getVehicleCodes() != null ? model.getVehicleCodes().size() : 0L)"
    )
    @Mapping(
            target = "vehicleCodes",
            expression =
                    "java(model.getVehicleCodes() != null " +
                            "? new java.util.HashSet<>(model.getVehicleCodes()) " +
                            ": java.util.Collections.emptySet())"
    )
    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    GroupSummaryDto toSummaryDto(NotificationGroupModel model,
                                 long usersCount,
                                 long alertsLast24h);
}
