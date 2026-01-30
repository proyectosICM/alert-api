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

        if (request.getActive() == null) {
            model.setActive(true);
        }

        // Normalizar placas (principal)
        if (request.getVehiclePlates() != null) {
            model.setVehiclePlates(normalizeVehiclePlates(request.getVehiclePlates()));
        } else if (model.getVehiclePlates() == null) {
            model.setVehiclePlates(new HashSet<>());
        } else {
            model.setVehiclePlates(normalizeVehiclePlates(model.getVehiclePlates()));
        }

        // Normalizar códigos (opcional)
        if (request.getVehicleCodes() != null) {
            model.setVehicleCodes(normalizeVehicleCodes(request.getVehicleCodes()));
        } else if (model.getVehicleCodes() == null) {
            model.setVehicleCodes(new HashSet<>());
        } else {
            model.setVehicleCodes(normalizeVehicleCodes(model.getVehicleCodes()));
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

        // Si mandaron vehiclePlates en el PATCH => reemplaza set completo
        if (request.getVehiclePlates() != null) {
            model.setVehiclePlates(normalizeVehiclePlates(request.getVehiclePlates()));
        }

        // Si mandaron vehicleCodes en el PATCH => reemplaza set completo
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

    // ================= Asignación de vehículos (placas principal, códigos opcional) =================

    @Override
    public FleetDetailDto addVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes) {
        // DEPRECADO si vas a migrar a placas, pero lo dejo por compatibilidad.
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

    // NUEVO: add plates
    public FleetDetailDto addPlates(Long companyId, Long fleetId, Set<String> vehiclePlates) {
        if (vehiclePlates == null || vehiclePlates.isEmpty()) {
            throw new IllegalArgumentException("vehiclePlates is required");
        }

        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = normalizeVehiclePlates(vehiclePlates);

        if (model.getVehiclePlates() == null) {
            model.setVehiclePlates(new HashSet<>());
        }

        model.getVehiclePlates().addAll(normalized);

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

    // NUEVO: remove plates
    public FleetDetailDto removePlates(Long companyId, Long fleetId, Set<String> vehiclePlates) {
        if (vehiclePlates == null || vehiclePlates.isEmpty()) {
            throw new IllegalArgumentException("vehiclePlates is required");
        }

        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = normalizeVehiclePlates(vehiclePlates);

        if (model.getVehiclePlates() != null && !model.getVehiclePlates().isEmpty()) {
            model.getVehiclePlates().removeAll(normalized);
        }

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    public FleetDetailDto replaceVehicles(Long companyId, Long fleetId, Set<String> vehicleCodes) {
        // compat: reemplaza códigos
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

    // NUEVO: replace plates
    public FleetDetailDto replacePlates(Long companyId, Long fleetId, Set<String> vehiclePlates) {
        FleetModel model = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found: " + fleetId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Fleet does not belong to company: " + companyId);
        }

        Set<String> normalized = (vehiclePlates == null)
                ? new HashSet<>()
                : normalizeVehiclePlates(vehiclePlates);

        model.setVehiclePlates(normalized);

        FleetModel saved = fleetRepository.save(model);
        return fleetMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVehicleCodes(Long companyId, Long fleetId) {
        List<String> codes = fleetRepository.findVehicleCodes(companyId, fleetId);
        if (codes == null) return List.of();

        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    // NUEVO: get plates
    @Transactional(readOnly = true)
    public List<String> getVehiclePlates(Long companyId, Long fleetId) {
        List<String> plates = fleetRepository.findVehiclePlates(companyId, fleetId);
        if (plates == null) return List.of();

        return plates.stream()
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
                .map(this::normalizeCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeCode(String s) {
        // trim + upper + quitar espacios internos
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        return x;
    }

    private Set<String> normalizeVehiclePlates(Collection<String> plates) {
        if (plates == null) return new HashSet<>();

        return plates.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::normalizePlate)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizePlate(String s) {
        // UPPER + sin espacios + sin guiones
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        x = x.replace("-", "");
        return x;
    }
}
