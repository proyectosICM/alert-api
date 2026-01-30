package com.icm.alert_api.dto.vehicle;

import com.icm.alert_api.enums.VehicleStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVehicleRequest {

    @Size(max = 50)
    private String vehicleCodeRaw;

    @Size(max = 50)
    private String vehicleCodeNorm;

    @Size(max = 50)
    private String licensePlate;

    /**
     * Permite “promover” UNREGISTERED -> ACTIVE, o dar de baja -> DISABLED
     */
    private VehicleStatus status;
}
