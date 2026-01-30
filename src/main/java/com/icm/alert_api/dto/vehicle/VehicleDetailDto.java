package com.icm.alert_api.dto.vehicle;

import com.icm.alert_api.enums.VehicleStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class VehicleDetailDto {

    Long id;

    Long companyId;
    String companyName;

    String vehicleCodeRaw;
    String vehicleCodeNorm;

    String licensePlate;

    VehicleStatus status;

    Instant createdAt;
    Instant updatedAt;
}
