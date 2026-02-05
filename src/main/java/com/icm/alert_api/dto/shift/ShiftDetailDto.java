package com.icm.alert_api.dto.shift;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class ShiftDetailDto {

    Long id;

    Long companyId;
    String companyName;

    LocalDate rosterDate;
    String shiftName;

    String batchId;
    boolean active;

    List<String> responsibleDnis;
    List<String> vehiclePlates;

    Instant createdAt;
    Instant updatedAt;
}
