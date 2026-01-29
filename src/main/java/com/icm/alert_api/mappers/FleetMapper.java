package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.fleet.CreateFleetRequest;
import com.icm.alert_api.dto.fleet.FleetDetailDto;
import com.icm.alert_api.dto.fleet.FleetSummaryDto;
import com.icm.alert_api.dto.fleet.UpdateFleetRequest;
import com.icm.alert_api.models.FleetModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FleetMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // @UpdateTimestamp
    @Mapping(target = "version", ignore = true)
    FleetModel toEntity(CreateFleetRequest request);
    // name, description, active, vehicleCodes se mapean por nombre (si están en el DTO).

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UpdateFleetRequest request, @MappingTarget FleetModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() != null ? model.getUpdatedAt().toInstant() : null)"
    )
    FleetDetailDto toDetailDto(FleetModel model);

    // ======= Entity -> Summary DTO =======

    @Mapping(
            target = "companyId",
            expression = "java(model.getCompany() != null ? model.getCompany().getId() : null)"
    )
    @Mapping(
            target = "companyName",
            expression = "java(model.getCompany() != null ? model.getCompany().getName() : null)"
    )
    @Mapping(
            target = "vehiclesCount",
            expression = "java(model.getVehicleCodes() != null ? (long) model.getVehicleCodes().size() : 0L)"
    )
    FleetSummaryDto toSummaryDto(FleetModel model);
}
