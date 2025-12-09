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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification-groups")
@RequiredArgsConstructor
@Validated
public class NotificationGroupController {

    private final NotificationGroupService groupService;

    // ============== CREATE (companyId viene en el DTO) ==============

    /**
     * POST /api/notification-groups
     * companyId viene en CreateGroupRequest.companyId
     */
    @PostMapping
    public ResponseEntity<GroupDetailDto> create(
            @Valid @RequestBody CreateGroupRequest request
    ) {
        GroupDetailDto created = groupService.create(request);
        URI location = URI.create("/api/notification-groups/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // ============== READ ONE (usa companyId por query param) ==============

    /**
     * GET /api/notification-groups/{id}?companyId=...
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupDetailDto> getById(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        Optional<GroupDetailDto> opt = groupService.findById(companyId, id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ============== LIST / SEARCH (companyId por query param) ==============

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

    // ============== UPDATE (companyId viene en el DTO) ==============

    /**
     * PATCH /api/notification-groups/{id}
     * companyId viene en UpdateGroupRequest.companyId
     */
    @PatchMapping("/{id}")
    public ResponseEntity<GroupDetailDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        GroupDetailDto updated = groupService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    // ============== DELETE (companyId por query param) ==============

    /**
     * DELETE /api/notification-groups/{id}?companyId=...
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id
    ) {
        groupService.deleteById(companyId, id);
    }
}
