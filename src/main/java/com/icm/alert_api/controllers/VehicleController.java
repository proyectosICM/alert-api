package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.vehicle.CreateVehicleRequest;
import com.icm.alert_api.dto.vehicle.UpdateVehicleRequest;
import com.icm.alert_api.dto.vehicle.VehicleDetailDto;
import com.icm.alert_api.dto.vehicle.VehicleSummaryDto;
import com.icm.alert_api.services.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Validated
public class VehicleController {

    private final VehicleService vehicleService;

    // ============== CREATE ==============

    /**
     * POST /api/vehicles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDetailDto create(@Valid @RequestBody CreateVehicleRequest request) {
        try {
            return vehicleService.create(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (DataIntegrityViolationException ex) {
            // unique(company_id, vehicle_code_norm) por ejemplo
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle already exists", ex);
        }
    }

    // ============== READ ONE ==============

    /**
     * GET /api/vehicles/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public VehicleDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        return vehicleService.findById(companyId, id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found: " + id));
    }

    /**
     * GET /api/vehicles/by-code?companyId=...&code=MG069
     * (busca por vehicleCodeNorm, normalizando)
     */
    @GetMapping("/by-code")
    public VehicleDetailDto findByCodeNorm(
            @RequestParam("companyId") Long companyId,
            @RequestParam("code") String vehicleCodeNorm
    ) {
        return vehicleService.findByCodeNorm(companyId, vehicleCodeNorm)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found for code: " + vehicleCodeNorm));
    }

    // ============== LIST ==============

    /**
     * GET /api/vehicles?companyId=...&page=0&size=20
     */
    @GetMapping
    public Page<VehicleSummaryDto> listAll(
            @RequestParam("companyId") Long companyId,
            Pageable pageable
    ) {
        try {
            return vehicleService.listAll(companyId, pageable);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== UPDATE (PATCH) ==============

    /**
     * PATCH /api/vehicles/{id}?companyId=...
     */
    @PatchMapping("/{id}")
    public VehicleDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        try {
            return vehicleService.update(companyId, id, request);
        } catch (IllegalArgumentException ex) {
            // según tu service, puede ser not found / no pertenece / etc
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle code already exists", ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/vehicles/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            vehicleService.deleteById(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== HELPERS (opcional, pero útil para ingest) ==============

    /**
     * POST /api/vehicles/get-or-create?companyId=...&vehicleCodeRaw=...&licensePlate=...
     * Útil para debugging/manual, pero en producción normalmente se llama desde AlertService.
     */
    @PostMapping("/get-or-create")
    public VehicleDetailDto getOrCreateFromIncomingAlert(
            @RequestParam("companyId") Long companyId,
            @RequestParam("vehicleCodeRaw") String vehicleCodeRaw,
            @RequestParam(value = "licensePlate", required = false) String licensePlate
    ) {
        try {
            return vehicleService.getOrCreateFromIncomingAlert(companyId, vehicleCodeRaw, licensePlate);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (DataIntegrityViolationException ex) {
            // carrera: dos requests crean el mismo vehicle a la vez
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle already exists", ex);
        }
    }
}
