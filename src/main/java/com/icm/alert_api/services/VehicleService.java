package com.icm.alert_api.services;

import com.icm.alert_api.dto.vehicle.CreateVehicleRequest;
import com.icm.alert_api.dto.vehicle.UpdateVehicleRequest;
import com.icm.alert_api.dto.vehicle.VehicleDetailDto;
import com.icm.alert_api.dto.vehicle.VehicleSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VehicleService {

    VehicleDetailDto create(CreateVehicleRequest request);

    VehicleDetailDto update(Long companyId, Long vehicleId, UpdateVehicleRequest request);

    Optional<VehicleDetailDto> findById(Long companyId, Long vehicleId);

    Optional<VehicleDetailDto> findByCodeNorm(Long companyId, String vehicleCodeNorm);

    Page<VehicleSummaryDto> listAll(Long companyId, Pageable pageable);

    /**
     * ✅ Para tu flujo de alertas:
     * - Normaliza el code
     * - Busca (company + norm)
     * - Si no existe, lo crea en UNREGISTERED
     */
    VehicleDetailDto getOrCreateFromIncomingAlert(
            Long companyId,
            String vehicleCodeRaw,
            String licensePlate
    );

    void deleteById(Long companyId, Long vehicleId);
}
