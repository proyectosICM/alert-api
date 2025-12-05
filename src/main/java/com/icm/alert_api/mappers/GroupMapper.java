package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.models.GroupModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // si no envían "active", por defecto true
    @Mapping(target = "active",
            expression = "java(request.getActive() != null ? request.getActive() : true)")
    GroupModel toEntity(CreateGroupRequest request);

    // ======= Update DTO -> Entity (patch) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateGroupRequest request, @MappingTarget GroupModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)")
    GroupDetailDto toDetailDto(GroupModel model, long usersCount, long alertsLast24h);

    // ======= Entity -> Summary DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)")
    GroupSummaryDto toSummaryDto(GroupModel model, long usersCount, long alertsLast24h);
}
