package com.icm.alert_api.dto.shift;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder
public class ShiftSummaryDto {

    Long id;

    Long companyId;
    String companyName;

    LocalDate rosterDate;
    String shiftName;

    boolean active;

    int responsiblesCount;
    int vehiclePlatesCount;

    Instant createdAt;
}
