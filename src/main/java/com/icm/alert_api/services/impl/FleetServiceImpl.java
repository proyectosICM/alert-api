package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.fleet.CreateFleetRequest;
import com.icm.alert_api.dto.fleet.FleetDetailDto;
import com.icm.alert_api.dto.fleet.FleetSummaryDto;
import com.icm.alert_api.dto.fleet.UpdateFleetRequest;
import com.icm.alert_api.mappers.FleetMapper;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.models.FleetModel;
import com.icm.alert_api.repositories.CompanyRepository;
import com.icm.alert_api.repositories.FleetRepository;
import com.icm.alert_api.services.FleetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FleetServiceImpl implements FleetService {

    private final FleetRepository fleetRepository;
    private final CompanyRepository companyRepository;
    private final FleetMapper fleetMapper;

    // ================= CRUD =================

    @Override
    public FleetDetailDto create(CreateFleetRequest request) {
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required in CreateFleetRequest");
        }

        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        FleetModel model = fleetMapper.toEntity(request);
        model.setCompany(company);

        // Default active=true si no viene en request
        if (request.getActive() == null) {
            model.setActive(true);
        }

        // Normalizar vehicleCodes (trim + remover vacíos)
        if (model.getVehicleCodes() != null) {
            model.setVehicleCodes(normalizeVehicleCodes(model.getVehicleCodes()));
        } else {
            model.setVehicleCodes(new HashSet<>());
        }

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    public FleetDetailDto update(Long fleetId, UpdateFleetRequest request) {
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required in UpdateFleetRequest");
        }

        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        // PATCH (ignora nulls)
        fleetMapper.updateEntityFromDto(request, model);

        // Si mandaron vehicleCodes en el PATCH => reemplaza el set completo
        if (request.getVehicleCodes() != null) {
            model.setVehicleCodes(normalizeVehicleCodes(request.getVehicleCodes()));
        }

        FleetModel updated = fleetRepository.save(model);
        return fleetMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long companyId, Long fleetId) {
        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        fleetRepository.delete(model);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FleetDetailDto> findById(Long companyId, Long fleetId) {
        return fleetRepository.findById(fleetId)
                .filter(f -> f.getCompany().getId().equals(companyId))
                .map(fleetMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FleetSummaryDto> search(Long companyId, String q, Pageable pageable) {
        Page<FleetModel> page;

        if (q == null || q.isBlank()) {
            page = fleetRepository.findByCompany_Id(companyId, pageable);
        } else {
            page = fleetRepository.searchByCompanyAndText(companyId, q.trim(), pageable);
        }

        return page.map(fleetMapper::toSummaryDto);
    }

    // ================= Asignación de vehículos =================

    @Override
    public FleetDetailDto addVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes) {
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            throw new IllegalArgumentException("vehicleCodes is required");
        }

        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = normalizeVehicleCodes(vehicleCodes);

        if (model.getVehicleCodes() == null) {
            model.setVehicleCodes(new HashSet<>());
        }

        model.getVehicleCodes().addAll(normalized);

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    public FleetDetailDto removeVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes) {
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            throw new IllegalArgumentException("vehicleCodes is required");
        }

        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = normalizeVehicleCodes(vehicleCodes);

        if (model.getVehicleCodes() != null && !model.getVehicleCodes().isEmpty()) {
            model.getVehicleCodes().removeAll(normalized);
        }

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    public FleetDetailDto replaceVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes) {
        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = (vehicleCodes == null)
                ? new HashSet<>()
                : normalizeVehicleCodes(vehicleCodes);

        model.setVehicleCodes(normalized);

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVehicleCodes(Long companyId, Long fleetId) {
        // Esto evita cargar toda la entidad (si tu repo tiene el @Query)
        List<String> codes = fleetRepository.findVehicleCodes(companyId, fleetId);
        if (codes == null) return List.of();

        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    // ================= Helpers =================

    private Set<String> normalizeVehicleCodes(Collection<String> codes) {
        if (codes == null) return new HashSet<>();

        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }
}
