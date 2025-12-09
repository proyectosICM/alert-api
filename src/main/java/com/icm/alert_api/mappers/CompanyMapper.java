package com.icm.alert_api.mappers;

import com.icm.alert_api.dto.company.CompanyDetailDto;
import com.icm.alert_api.dto.company.CompanySummaryDto;
import com.icm.alert_api.dto.company.CreateCompanyRequest;
import com.icm.alert_api.dto.company.UpdateCompanyRequest;
import com.icm.alert_api.models.CompanyModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // ======= Create DTO -> Entity =======

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CompanyModel toEntity(CreateCompanyRequest request);

    // ======= Update DTO -> Entity (PATCH) =======

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateCompanyRequest request,
                             @MappingTarget CompanyModel entity);

    // ======= Entity -> Detail DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "groupsCount", expression = "java(groupsCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() != null ? model.getUpdatedAt().toInstant() : null)"
    )
    CompanyDetailDto toDetailDto(CompanyModel model,
                                 long usersCount,
                                 long groupsCount,
                                 long alertsLast24h);

    // ======= Entity -> Summary DTO =======

    @Mapping(target = "usersCount", expression = "java(usersCount)")
    @Mapping(target = "groupsCount", expression = "java(groupsCount)")
    @Mapping(target = "alertsLast24h", expression = "java(alertsLast24h)")
    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toInstant() : null)"
    )
    CompanySummaryDto toSummaryDto(CompanyModel model,
                                   long usersCount,
                                   long groupsCount,
                                   long alertsLast24h);
}
