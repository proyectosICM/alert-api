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
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // si no envían "active", por defecto true
    @Mapping(target = "active",
            expression = "java(request.getActive() != null ? request.getActive() : true)")
    NotificationGroupModel toEntity(CreateGroupRequest request);

    // ======= Update DTO -> Entity (patch) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateGroupRequest request, @MappingTarget NotificationGroupModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)")
    GroupDetailDto toDetailDto(NotificationGroupModel model, long usersCount, long alertsLast24h);

    // ======= Entity -> Summary DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)")
    GroupSummaryDto toSummaryDto(NotificationGroupModel model, long usersCount, long alertsLast24h);
}
