package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.vehicle.CreateVehicleRequest;
import com.icm.alert_api.dto.vehicle.UpdateVehicleRequest;
import com.icm.alert_api.dto.vehicle.VehicleDetailDto;
import com.icm.alert_api.dto.vehicle.VehicleSummaryDto;
import com.icm.alert_api.enums.VehicleStatus;
import com.icm.alert_api.mappers.VehicleMapper;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.models.VehicleModel;
import com.icm.alert_api.repositories.CompanyRepository;
import com.icm.alert_api.repositories.VehicleRepository;
import com.icm.alert_api.services.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final VehicleMapper vehicleMapper;

    // ---------- CRUD ----------

    @Override
    public VehicleDetailDto create(CreateVehicleRequest request) {
        if (request.getCompanyId() == null) throw new IllegalArgumentException("companyId is required");
        if (request.getVehicleCodeRaw() == null || request.getVehicleCodeRaw().isBlank()) {
            throw new IllegalArgumentException("vehicleCodeRaw is required");
        }

        CompanyModel company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + request.getCompanyId()));

        // Si te mandan norm, úsalo; si no, calcúlalo.
        String raw = request.getVehicleCodeRaw();
        String norm = (request.getVehicleCodeNorm() == null || request.getVehicleCodeNorm().isBlank())
                ? normalize(raw)
                : normalize(request.getVehicleCodeNorm());

        VehicleModel model = VehicleModel.builder()
                .vehicleCodeRaw(raw)
                .vehicleCodeNorm(norm)
                .licensePlate(blankToNull(request.getLicensePlate()))
                .status(VehicleStatus.UNREGISTERED)
                .company(company)
                .build();

        try {
            VehicleModel saved = vehicleRepository.save(model);
            return vehicleMapper.toDetailDto(saved);
        } catch (DataIntegrityViolationException dup) {
            // por unique(company_id, vehicle_code_norm)
            return vehicleRepository.findByCompany_IdAndVehicleCodeNorm(company.getId(), norm)
                    .map(vehicleMapper::toDetailDto)
                    .orElseThrow(() -> dup);
        }
    }

    @Override
    public VehicleDetailDto update(Long companyId, Long vehicleId, UpdateVehicleRequest request) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");

        VehicleModel model = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Vehicle does not belong to company: " + companyId);
        }

        // PATCH manual (o MapStruct si lo prefieres)
        if (request.getVehicleCodeRaw() != null && !request.getVehicleCodeRaw().isBlank()) {
            model.setVehicleCodeRaw(request.getVehicleCodeRaw());
        }
        if (request.getVehicleCodeNorm() != null && !request.getVehicleCodeNorm().isBlank()) {
            model.setVehicleCodeNorm(normalize(request.getVehicleCodeNorm()));
        } else if (request.getVehicleCodeRaw() != null && !request.getVehicleCodeRaw().isBlank()) {
            // si cambió raw y no mandan norm, recalcula
            model.setVehicleCodeNorm(normalize(request.getVehicleCodeRaw()));
        }

        if (request.getLicensePlate() != null) {
            model.setLicensePlate(blankToNull(request.getLicensePlate()));
        }

        if (request.getStatus() != null) {
            model.setStatus(request.getStatus());
        }

        VehicleModel saved = vehicleRepository.save(model);
        return vehicleMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleDetailDto> findById(Long companyId, Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .filter(v -> v.getCompany().getId().equals(companyId))
                .map(vehicleMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleDetailDto> findByCodeNorm(Long companyId, String vehicleCodeNorm) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (vehicleCodeNorm == null || vehicleCodeNorm.isBlank()) return Optional.empty();
        return vehicleRepository.findByCompany_IdAndVehicleCodeNorm(companyId, normalize(vehicleCodeNorm))
                .map(vehicleMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleSummaryDto> listAll(Long companyId, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        // Asumiendo que tienes un método tipo findByCompany_Id(...) o spec.
        // Si no lo tienes, agrégalo al repo:
        // Page<VehicleModel> findByCompany_Id(Long companyId, Pageable pageable);
        return vehicleRepository.findByCompany_Id(companyId, pageable).map(vehicleMapper::toSummaryDto);
    }

    @Override
    public VehicleDetailDto getOrCreateFromIncomingAlert(Long companyId, String vehicleCodeRaw, String licensePlate) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (vehicleCodeRaw == null || vehicleCodeRaw.isBlank()) {
            throw new IllegalArgumentException("vehicleCodeRaw is required");
        }

        String norm = normalize(vehicleCodeRaw);

        // 1) si ya existe => devuélvelo
        Optional<VehicleModel> existing = vehicleRepository.findByCompany_IdAndVehicleCodeNorm(companyId, norm);
        if (existing.isPresent()) {
            // si llegó placa y el registro no tiene, la guardamos “gratis”
            VehicleModel v = existing.get();
            if ((v.getLicensePlate() == null || v.getLicensePlate().isBlank()) && notBlank(licensePlate)) {
                v.setLicensePlate(licensePlate.trim());
                vehicleRepository.save(v);
            }
            return vehicleMapper.toDetailDto(v);
        }

        // 2) no existe => crear UNREGISTERED
        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        VehicleModel toCreate = VehicleModel.builder()
                .vehicleCodeRaw(vehicleCodeRaw)
                .vehicleCodeNorm(norm)
                .licensePlate(blankToNull(licensePlate))
                .status(VehicleStatus.UNREGISTERED)
                .company(company)
                .build();

        try {
            VehicleModel saved = vehicleRepository.save(toCreate);
            return vehicleMapper.toDetailDto(saved);
        } catch (DataIntegrityViolationException race) {
            // carrera (dos alertas llegan a la vez)
            return vehicleRepository.findByCompany_IdAndVehicleCodeNorm(companyId, norm)
                    .map(vehicleMapper::toDetailDto)
                    .orElseThrow(() -> race);
        }
    }

    @Override
    public void deleteById(Long companyId, Long vehicleId) {
        VehicleModel model = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Vehicle does not belong to company: " + companyId);
        }

        vehicleRepository.delete(model);
    }

    // ---------- Helpers ----------

    private static String normalize(String s) {
        if (s == null) return null;
        // trim + upper + quitar espacios internos (y tab)
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        return x;
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isBlank();
    }
}
