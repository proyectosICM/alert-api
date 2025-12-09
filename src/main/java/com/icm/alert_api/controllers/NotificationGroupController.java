package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.services.NotificationGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notification-groups")
@RequiredArgsConstructor
@Validated
public class NotificationGroupController {

    private final NotificationGroupService groupService;

    // ============== CREATE ==============

    /**
     * POST /api/notification-groups?companyId=...
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetailDto create(
            @RequestParam("companyId") Long companyId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        return groupService.create(companyId, request);
    }

    // ============== READ ONE ==============

    /**
     * GET /api/notification-groups/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public GroupDetailDto findById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        return groupService.findById(companyId, id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + id));
    }

    // ============== LIST / SEARCH ==============

    /**
     * GET /api/notification-groups?companyId=...&q=texto&page=0&size=20
     */
    @GetMapping
    public Page<GroupSummaryDto> search(
            @RequestParam("companyId") Long companyId,
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return groupService.search(companyId, q, pageable);
    }

    // ============== UPDATE ==============

    /**
     * PATCH /api/notification-groups/{id}?companyId=...
     */
    @PatchMapping("/{id}")
    public GroupDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        try {
            return groupService.update(companyId, id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ============== DELETE ==============

    /**
     * DELETE /api/notification-groups/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        try {
            groupService.deleteById(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
