package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.user.CreateGroupUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.models.UserModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ======= Create DTO -> Entity =======
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)        // se setea en el service
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")   // por defecto activo
    UserModel toEntity(CreateGroupUserRequest request);

    // ======= Update DTO -> Entity (patch) =======
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)        // no cambiamos el grupo vía update
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateGroupUserRequest request, @MappingTarget UserModel entity);

    // ======= Entity -> Detail DTO =======
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() != null ? model.getUpdatedAt().toInstant() : null)"
    )
    GroupUserDetailDto toDetailDto(UserModel model);

    // ======= Entity -> Summary DTO =======
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    GroupUserSummaryDto toSummaryDto(UserModel model);
}
