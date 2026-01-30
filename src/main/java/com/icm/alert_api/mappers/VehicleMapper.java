package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.vehicle.CreateVehicleRequest;
import com.icm.alert_api.dto.vehicle.UpdateVehicleRequest;
import com.icm.alert_api.dto.vehicle.VehicleDetailDto;
import com.icm.alert_api.dto.vehicle.VehicleSummaryDto;
import com.icm.alert_api.models.VehicleModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // @UpdateTimestamp
        // status: si en el CreateVehicleRequest no viene, se queda con @Builder.Default (UNREGISTERED)
    VehicleModel toEntity(CreateVehicleRequest request);

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateVehicleRequest request, @MappingTarget VehicleModel entity);

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
    VehicleDetailDto toDetailDto(VehicleModel model);

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
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() != null ? model.getUpdatedAt().toInstant() : null)"
    )
    VehicleSummaryDto toSummaryDto(VehicleModel model);
}
