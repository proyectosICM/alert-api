package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.revision.AlertRevisionDetailDto;
import com.icm.alert_api.dto.revision.AlertRevisionSummaryDto;
import com.icm.alert_api.dto.revision.CreateAlertRevisionRequest;
import com.icm.alert_api.dto.revision.UpdateAlertRevisionRequest;
import com.icm.alert_api.services.AlertRevisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/alert-revisions")
@RequiredArgsConstructor
@Validated
public class AlertRevisionController {

    private final AlertRevisionService revisionService;

    // ============== CREATE ==============

    /**
     * POST /api/alert-revisions?companyId=...
     * Crea una revisión para una alerta (usualmente 1 por alerta).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRevisionDetailDto create(
            @RequestParam("companyId") Long companyId,
            @Valid @RequestBody CreateAlertRevisionRequest request
    ) {
        try {
            return revisionService.create(companyId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== READ ONE BY ID ==============

    /**
     * GET /api/alert-revisions/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public AlertRevisionDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            return revisionService.findById(companyId, id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Alert revision not found: " + id
                    ));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== READ ONE BY ALERT ID ==============

    /**
     * GET /api/alert-revisions/by-alert/{alertId}?companyId=...
     * Obtiene la revisión asociada a una alerta (si tu regla es 1 por alerta).
     */
    @GetMapping("/by-alert/{alertId}")
    public AlertRevisionDetailDto findByAlertId(
            @RequestParam("companyId") Long companyId,
            @PathVariable("alertId") Long alertId
    ) {
        try {
            return revisionService.findByAlertId(companyId, alertId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Alert revision not found for alertId: " + alertId
                    ));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== EXISTS ==============

    /**
     * GET /api/alert-revisions/exists?companyId=...&alertId=...
     */
    @GetMapping("/exists")
    public ExistsResponse existsForAlert(
            @RequestParam("companyId") Long companyId,
            @RequestParam("alertId") Long alertId
    ) {
        try {
            boolean exists = revisionService.existsForAlert(companyId, alertId);
            return new ExistsResponse(exists);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    public record ExistsResponse(boolean exists) {}

    // ============== LIST ALL ==============

    /**
     * GET /api/alert-revisions?companyId=...&page=0&size=20
     */
    @GetMapping
    public Page<AlertRevisionSummaryDto> listAll(
            @RequestParam("companyId") Long companyId,
            Pageable pageable
    ) {
        try {
            return revisionService.listAll(companyId, pageable);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== LIST BY ALERT ==============

    /**
     * GET /api/alert-revisions/alert/{alertId}?companyId=...&page=0&size=20
     * Si permites múltiples revisiones por alerta (o igual te sirve para historial).
     */
    @GetMapping("/alert/{alertId}")
    public Page<AlertRevisionSummaryDto> listByAlert(
            @RequestParam("companyId") Long companyId,
            @PathVariable("alertId") Long alertId,
            Pageable pageable
    ) {
        try {
            return revisionService.listByAlert(companyId, alertId, pageable);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== UPDATE (PATCH) ==============

    /**
     * PATCH /api/alert-revisions/{id}?companyId=...
     */
    @PatchMapping("/{id}")
    public AlertRevisionDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAlertRevisionRequest request
    ) {
        try {
            return revisionService.update(companyId, id, request);
        } catch (IllegalArgumentException ex) {
            // igual que tu AlertController: not found/mismatch -> 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/alert-revisions/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            revisionService.deleteById(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
