    package com.icm.alert_api.controllers;
    
    import com.icm.alert_api.dto.alert.AlertDetailDto;
    import com.icm.alert_api.dto.alert.AlertSummaryDto;
    import com.icm.alert_api.dto.alert.CreateAlertRequest;
    import com.icm.alert_api.dto.alert.UpdateAlertRequest;
    import com.icm.alert_api.services.AlertService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.server.ResponseStatusException;
    
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
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public AlertDetailDto create(@Valid @RequestBody CreateAlertRequest request) {
            return alertService.create(request);
        }
    
        // ============== READ ONE ==============
    
        /**
         * Obtiene el detalle de una alerta por id.
         *
         * GET /api/alerts/{id}
         */
        @GetMapping("/{id}")
        public AlertDetailDto findById(@PathVariable("id") Long id) {
            return alertService.findById(id)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id));
        }

        @GetMapping
        public Page<AlertSummaryDto> listAll(Pageable pageable) {
            return alertService.listAll(pageable);
        }

        // ============== LIST / HISTORY BY GROUP ==============
    
        /**
         * Historial de alertas para un grupo, usando sus vehicleCodes,
         * ordenado de más reciente a más antiguo.
         *
         * GET /api/alerts/group/{groupId}?page=0&size=20
         */
        @GetMapping("/group/{groupId}")
        public Page<AlertSummaryDto> listByGroup(
                @PathVariable("groupId") Long groupId,
                Pageable pageable
        ) {
            return alertService.listByGroup(groupId, pageable);
        }
    
        /**
         * Historial de alertas para un grupo en un rango de fechas.
         * Los parámetros from y to deben venir en formato ISO-8601,
         * por ejemplo: 2025-12-05T10:00:00-05:00
         *
         * GET /api/alerts/group/{groupId}/range?from=...&to=...
         */
        @GetMapping("/group/{groupId}/range")
        public Page<AlertSummaryDto> listByGroupAndDateRange(
                @PathVariable("groupId") Long groupId,
                @RequestParam("from") String from,
                @RequestParam("to") String to,
                Pageable pageable
        ) {
            try {
                ZonedDateTime fromDt = ZonedDateTime.parse(from);
                ZonedDateTime toDt = ZonedDateTime.parse(to);
    
                return alertService.listByGroupAndDateRange(groupId, fromDt, toDt, pageable);
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
         * Actualiza (patch) una alerta.
         * Ej: para marcar acknowledged = true.
         *
         * PATCH /api/alerts/{id}
         */
        @PatchMapping("/{id}")
        public AlertDetailDto update(
                @PathVariable("id") Long id,
                @Valid @RequestBody UpdateAlertRequest request
        ) {
            try {
                return alertService.update(id, request);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
        }
    
        // ============== DELETE ==============
    
        /**
         * Elimina una alerta por id.
         *
         * DELETE /api/alerts/{id}
         */
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable("id") Long id) {
            try {
                alertService.deleteById(id);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
        }
    }
