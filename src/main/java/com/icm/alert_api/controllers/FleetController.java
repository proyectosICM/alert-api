package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.fleet.CreateFleetRequest;
import com.icm.alert_api.dto.fleet.FleetDetailDto;
import com.icm.alert_api.dto.fleet.FleetSummaryDto;
import com.icm.alert_api.dto.fleet.UpdateFleetRequest;
import com.icm.alert_api.services.FleetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/fleets")
@RequiredArgsConstructor
@Validated
public class FleetController {

    private final FleetService fleetService;

    // ============== CREATE ==============

    /**
     * POST /api/fleets
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FleetDetailDto create(@Valid @RequestBody CreateFleetRequest request) {
        try {
            return fleetService.create(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== READ ONE ==============

    /**
     * GET /api/fleets/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public FleetDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId
    ) {
        return fleetService.findById(companyId, fleetId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fleet not found: " + fleetId
                ));
    }

    // ============== LIST / SEARCH ==============

    /**
     * GET /api/fleets?companyId=...&q=...&page=0&size=20
     */
    @GetMapping
    public Page<FleetSummaryDto> search(
            @RequestParam("companyId") Long companyId,
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return fleetService.search(companyId, q, pageable);
    }

    // ============== UPDATE (PATCH) ==============

    /**
     * PATCH /api/fleets/{id}
     * body: { companyId, name?, description?, active?, vehicleCodes? }
     */
    @PatchMapping("/{id}")
    public FleetDetailDto update(
            @PathVariable("id") Long fleetId,
            @Valid @RequestBody UpdateFleetRequest request
    ) {
        try {
            return fleetService.update(fleetId, request);
        } catch (IllegalArgumentException ex) {
            // puede ser not found o belongs to different company
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/fleets/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId
    ) {
        try {
            fleetService.deleteById(companyId, fleetId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== VEHICLES ASSIGNMENT ==============

    @Data
    public static class VehicleCodesRequest {
        @NotEmpty
        private Set<String> vehicleCodes;
    }

    /**
     * POST /api/fleets/{id}/vehicles/add?companyId=...
     * body: { "vehicleCodes": ["FG22010", "MG069"] }
     */
    @PostMapping("/{id}/vehicles/add")
    public FleetDetailDto addVehicles(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId,
            @Valid @RequestBody VehicleCodesRequest request
    ) {
        try {
            return fleetService.addVehicles(companyId, fleetId, request.getVehicleCodes());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    /**
     * POST /api/fleets/{id}/vehicles/remove?companyId=...
     * body: { "vehicleCodes": ["FG22010"] }
     */
    @PostMapping("/{id}/vehicles/remove")
    public FleetDetailDto removeVehicles(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId,
            @Valid @RequestBody VehicleCodesRequest request
    ) {
        try {
            return fleetService.removeVehicles(companyId, fleetId, request.getVehicleCodes());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    /**
     * PUT /api/fleets/{id}/vehicles?companyId=...
     * body: { "vehicleCodes": ["FG22010", "MG069"] }
     *
     * Replace total (set completo)
     */
    @PutMapping("/{id}/vehicles")
    public FleetDetailDto replaceVehicles(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId,
            @Valid @RequestBody VehicleCodesRequest request
    ) {
        try {
            return fleetService.replaceVehicles(companyId, fleetId, request.getVehicleCodes());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    /**
     * GET /api/fleets/{id}/vehicles?companyId=...
     * Devuelve solo códigos, útil para front/autocomplete.
     */
    @GetMapping("/{id}/vehicles")
    public Set<String> listVehicleCodes(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long fleetId
    ) {
        try {
            return Set.copyOf(fleetService.getVehicleCodes(companyId, fleetId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
