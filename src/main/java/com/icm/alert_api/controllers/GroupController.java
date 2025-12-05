package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;

    // ============== CREATE ==============

    /**
     * Crea un nuevo grupo.
     *
     * POST /api/groups
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetailDto create(@Valid @RequestBody CreateGroupRequest request) {
        return groupService.create(request);
    }

    // ============== READ ONE ==============

    /**
     * Obtiene el detalle de un grupo por id.
     *
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public GroupDetailDto findById(@PathVariable("id") Long id) {
        return groupService.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + id));
    }

    // ============== LIST / SEARCH ==============

    /**
     * Lista / busca grupos con paginación.
     *
     * GET /api/groups?q=texto&page=0&size=20
     */
    @GetMapping
    public Page<GroupSummaryDto> search(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return groupService.search(q, pageable);
    }

    // ============== UPDATE ==============

    /**
     * Actualiza (patch) un grupo existente.
     *
     * PATCH /api/groups/{id}
     */
    @PatchMapping("/{id}")
    public GroupDetailDto update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        try {
            return groupService.update(id, request);
        } catch (IllegalArgumentException ex) {
            // Reusamos la misma semántica que en findById
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * Elimina un grupo por id.
     *
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        try {
            groupService.deleteById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
