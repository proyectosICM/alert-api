package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoDetailDto;
import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import com.icm.alert_api.dto.revision_photo.CreateAlertRevisionPhotoRequest;
import com.icm.alert_api.dto.revision_photo.UpdateAlertRevisionPhotoRequest;
import com.icm.alert_api.services.AlertRevisionPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/alert-revisions/{revisionId}/photos")
@RequiredArgsConstructor
@Validated
public class AlertRevisionPhotoController {

    private final AlertRevisionPhotoService photoService;

    // ============== LIST (SUMMARY) ==============

    /**
     * GET /api/alert-revisions/{revisionId}/photos?companyId=...
     * Lista fotos de una revisión (solo metadata, sin base64).
     */
    @GetMapping
    public List<AlertRevisionPhotoSummaryDto> listByRevision(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId
    ) {
        try {
            return photoService.listByRevision(companyId, revisionId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== READ ONE (DETAIL) ==============

    /**
     * GET /api/alert-revisions/{revisionId}/photos/{photoId}?companyId=...
     * Obtiene una foto (incluye base64).
     */
    @GetMapping("/{photoId}")
    public AlertRevisionPhotoDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId,
            @PathVariable("photoId") Long photoId
    ) {
        try {
            return photoService.findById(companyId, revisionId, photoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Alert revision photo not found: " + photoId
                    ));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== CREATE ==============

    /**
     * POST /api/alert-revisions/{revisionId}/photos?companyId=...
     * Crea una foto (base64) asociada a una revisión.
     *
     * Nota: aunque el service solo pide companyId + request,
     * aquí forzamos que request.revisionId coincida con el path.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRevisionPhotoDetailDto create(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId,
            @Valid @RequestBody CreateAlertRevisionPhotoRequest request
    ) {
        try {
            // Si tu DTO tiene revisionId, lo validamos/forzamos:
            // request.setRevisionId(revisionId);
            enforceRevisionIdOnCreate(request, revisionId);

            return photoService.create(companyId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ============== UPDATE (PATCH) ==============

    /**
     * PATCH /api/alert-revisions/{revisionId}/photos/{photoId}?companyId=...
     * Actualiza metadata y/o dataBase64 (si viene).
     */
    @PatchMapping("/{photoId}")
    public AlertRevisionPhotoDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId,
            @PathVariable("photoId") Long photoId,
            @Valid @RequestBody UpdateAlertRevisionPhotoRequest request
    ) {
        try {
            return photoService.update(companyId, revisionId, photoId, request);
        } catch (IllegalArgumentException ex) {
            // igual que tu AlertController: not found/mismatch -> 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/alert-revisions/{revisionId}/photos/{photoId}?companyId=...
     */
    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId,
            @PathVariable("photoId") Long photoId
    ) {
        try {
            photoService.deleteById(companyId, revisionId, photoId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== REPLACE ALL ==============

    /**
     * PUT /api/alert-revisions/{revisionId}/photos?companyId=...
     * Reemplaza todas las fotos de la revisión.
     * Devuelve lista summary (sin base64).
     */
    @PutMapping
    public List<AlertRevisionPhotoSummaryDto> replaceAll(
            @RequestParam("companyId") Long companyId,
            @PathVariable("revisionId") Long revisionId,
            @Valid @RequestBody List<CreateAlertRevisionPhotoRequest> photos
    ) {
        try {
            if (photos == null) {
                throw new IllegalArgumentException("photos list is required");
            }

            // Si el DTO tiene revisionId, forzamos/validamos uno por uno:
            for (CreateAlertRevisionPhotoRequest p : photos) {
                if (p == null) continue;
                enforceRevisionIdOnCreate(p, revisionId);
            }

            return photoService.replaceAll(companyId, revisionId, photos);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // =========================================================
    // Helpers
    // =========================================================
    /**
     * Para no acoplar el controller a tu DTO exacto:
     * - Si CreateAlertRevisionPhotoRequest tiene getRevisionId/setRevisionId, úsalo.
     * - Si NO lo tiene, elimina este helper y asume que el service resuelve por otro campo.
     */
    private void enforceRevisionIdOnCreate(CreateAlertRevisionPhotoRequest request, Long revisionId) {
        if (request == null) throw new IllegalArgumentException("request is required");

        try {
            // reflection suave para evitar error si tu DTO no tiene revisionId
            var getter = request.getClass().getMethod("getRevisionId");
            Object current = getter.invoke(request);

            if (current != null && !revisionId.equals(current)) {
                throw new IllegalArgumentException("revisionId mismatch (path vs body)");
            }

            var setter = request.getClass().getMethod("setRevisionId", Long.class);
            setter.invoke(request, revisionId);
        } catch (NoSuchMethodException ignore) {
            // el DTO no tiene revisionId; no hacemos nada
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid request body for revisionId binding");
        }
    }
}
