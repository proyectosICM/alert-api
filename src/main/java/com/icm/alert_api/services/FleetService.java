package com.icm.alert_api.services;

import com.icm.alert_api.dto.fleet.CreateFleetRequest;
import com.icm.alert_api.dto.fleet.FleetDetailDto;
import com.icm.alert_api.dto.fleet.FleetSummaryDto;
import com.icm.alert_api.dto.fleet.UpdateFleetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FleetService {

    // CRUD
    FleetDetailDto create(CreateFleetRequest request);

    FleetDetailDto update(Long fleetId, UpdateFleetRequest request);

    void deleteById(Long companyId, Long fleetId);

    Optional<FleetDetailDto> findById(Long companyId, Long fleetId);

    Page<FleetSummaryDto> search(Long companyId, String q, Pageable pageable);

    // === Asignación de vehículos ===

    /**
     * Agrega (merge) vehicleCodes a la flota.
     * Si el vehicleCode ya existe, lo ignora (Set).
     */
    FleetDetailDto addVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes);

    /**
     * Remueve vehicleCodes de la flota.
     * Si no existe alguno, simplemente no pasa nada.
     */
    FleetDetailDto removeVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes);

    /**
     * Reemplaza la lista completa (útil para un PATCH “replace”).
     */
    FleetDetailDto replaceVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes);

    /**
     * Resolver SOLO los vehicleCodes de la flota (para filtros de alertas).
     */
    List<String> getVehicleCodes(Long companyId, Long fleetId);
}
