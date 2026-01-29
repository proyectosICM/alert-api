package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.alert.*;
import com.icm.alert_api.services.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Validated
public class AlertController {

    private final AlertService alertService;

    // ============== CREATE ==============

    /**
     * Crea una nueva alerta.
     * Pensado para ser llamado por el script que procesa los correos.
     *
     * POST /api/alerts
     * La compañía se resuelve internamente a partir del vehicleCode
     * buscando los grupos que lo contengan.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertDetailDto create(
            @Valid @RequestBody CreateAlertRequest request
    ) {
        try {
            return alertService.create(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== READ ONE ==============

    /**
     * GET /api/alerts/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public AlertDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        return alertService.findById(companyId, id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id));
    }

    /**
     * GET /api/alerts?companyId=...&page=0&size=20
     */
    @GetMapping
    public Page<AlertSummaryDto> listAll(
            @RequestParam("companyId") Long companyId,
            Pageable pageable
    ) {
        return alertService.listAll(companyId, pageable);
    }

    // ============== LIST / HISTORY BY GROUP ==============

    /**
     * GET /api/alerts/group/{groupId}?companyId=...&page=0&size=20
     */
    @GetMapping("/group/{groupId}")
    public Page<AlertSummaryDto> listByGroup(
            @RequestParam("companyId") Long companyId,
            @PathVariable("groupId") Long groupId,
            Pageable pageable
    ) {
        return alertService.listByGroup(companyId, groupId, pageable);
    }

    /**
     * GET /api/alerts/group/{groupId}/range?companyId=...&from=...&to=...
     */
    @GetMapping("/group/{groupId}/range")
    public Page<AlertSummaryDto> listByGroupAndDateRange(
            @RequestParam("companyId") Long companyId,
            @PathVariable("groupId") Long groupId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            Pageable pageable
    ) {
        try {
            ZonedDateTime fromDt = ZonedDateTime.parse(from);
            ZonedDateTime toDt = ZonedDateTime.parse(to);

            return alertService.listByGroupAndDateRange(companyId, groupId, fromDt, toDt, pageable);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido para 'from' o 'to'. Usa ISO-8601 (ej: 2025-12-05T10:00:00-05:00)",
                    ex
            );
        }
    }

    // ============== UPDATE (PATCH) ==============

    /**
     * PATCH /api/alerts/{id}?companyId=...
     */
    @PatchMapping("/{id}")
    public AlertDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAlertRequest request
    ) {
        try {
            return alertService.update(companyId, id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/alerts/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            alertService.deleteById(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /**
     * POST /api/alerts/{id}/ack?companyId=...
     */
    @PostMapping("/{id}/ack")
    public AlertDetailDto acknowledge(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            return alertService.acknowledge(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/user/{userId}")
    public Page<AlertSummaryDto> listByUser(
            @RequestParam("companyId") Long companyId,
            @PathVariable("userId") Long userId,
            Pageable pageable
    ) {
        try {
            return alertService.listByUser(companyId, userId, pageable);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/count")
    public AlertCountResponse countByDay(
            @RequestParam Long companyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "zone", required = false, defaultValue = "America/Lima") String zone
    ) {
        ZoneId zoneId = ZoneId.of(zone);
        long total = alertService.countByDay(companyId, date, zoneId);
        return new AlertCountResponse(total);
    }
}
