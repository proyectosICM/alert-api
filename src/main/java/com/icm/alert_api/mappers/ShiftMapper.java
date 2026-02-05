package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.shift.CreateShiftRequest;
import com.icm.alert_api.dto.shift.ShiftDetailDto;
import com.icm.alert_api.dto.shift.ShiftSummaryDto;
import com.icm.alert_api.dto.shift.UpdateShiftRequest;
import com.icm.alert_api.models.ShiftModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShiftMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // @UpdateTimestamp
    @Mapping(
            target = "active",
            expression = "java(request.getActive() != null ? request.getActive() : true)"
    )
    @Mapping(
            target = "batchId",
            expression = "java(request.getBatchId() != null && !request.getBatchId().isBlank() ? request.getBatchId() : null)"
    )
    ShiftModel toEntity(CreateShiftRequest request);
    // rosterDate, shiftName, responsibleDnis, vehiclePlates -> por nombre

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateShiftRequest request, @MappingTarget ShiftModel entity);

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
    ShiftDetailDto toDetailDto(ShiftModel model);

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
            target = "responsiblesCount",
            expression = "java(sizeSafe(model.getResponsibleDnis()))"
    )
    @Mapping(
            target = "vehiclePlatesCount",
            expression = "java(sizeSafe(model.getVehiclePlates()))"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    ShiftSummaryDto toSummaryDto(ShiftModel model);

    // ======= helper =======
    default int sizeSafe(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
