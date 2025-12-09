package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.membership.CreateGroupMembershipRequest;
import com.icm.alert_api.dto.membership.GroupMembershipDetailDto;
import com.icm.alert_api.dto.membership.GroupMembershipSummaryDto;
import com.icm.alert_api.dto.membership.UpdateGroupMembershipRequest;
import com.icm.alert_api.models.GroupUserModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GroupMembershipMapper {

    // ======= Create DTO -> Entity =======
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)   // se setean en el servicio
    @Mapping(target = "user", ignore = true)
    @Mapping(
            target = "active",
            expression = "java(request.getActive() != null ? request.getActive() : true)"
    )
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GroupUserModel toEntity(CreateGroupMembershipRequest request);

    // ======= Update DTO -> Entity (PATCH) =======
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateGroupMembershipRequest request,
                             @MappingTarget GroupUserModel entity);

    // ======= Entity -> Detail DTO =======
    @Mapping(target = "groupId", expression = "java(model.getGroup().getId())")
    @Mapping(target = "userId", expression = "java(model.getUser().getId())")
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() != null ? model.getUpdatedAt().toInstant() : null)"
    )
    GroupMembershipDetailDto toDetailDto(GroupUserModel model);

    // ======= Entity -> Summary DTO =======
    @Mapping(target = "groupId", expression = "java(model.getGroup().getId())")
    @Mapping(target = "userId", expression = "java(model.getUser().getId())")
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    GroupMembershipSummaryDto toSummaryDto(GroupUserModel model);
}
