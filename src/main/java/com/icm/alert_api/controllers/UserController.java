package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.user.CreateGroupUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ============== CREATE ==============

    @PostMapping
    public ResponseEntity<GroupUserDetailDto> createUser(
            @RequestParam("groupId") Long groupId,
            @Valid @RequestBody CreateGroupUserRequest request
    ) {
        GroupUserDetailDto created = userService.create(groupId, request);

        // Location: /api/users/{id}?groupId={groupId}
        URI location = URI.create(
                String.format("/api/users/%d?groupId=%d", created.getId(), groupId)
        );

        return ResponseEntity
                .created(location)
                .body(created);
    }

    // ============== LISTAR / BUSCAR ==============

    @GetMapping
    public Page<GroupUserSummaryDto> searchUsers(
            @RequestParam("groupId") Long groupId,
            @RequestParam(name = "q", required = false) String q,
            Pageable pageable
    ) {
        return userService.search(groupId, q, pageable);
    }

    // ============== DETALLE ==============

    @GetMapping("/{userId}")
    public ResponseEntity<GroupUserDetailDto> getUser(
            @PathVariable Long userId,
            @RequestParam(value = "groupId", required = false) Long groupId
    ) {
        Optional<GroupUserDetailDto> opt;

        if (groupId != null) {
            // Búsqueda restringida al grupo
            opt = userService.findById(groupId, userId);
        } else {
            // Búsqueda solo por id
            opt = userService.findById(userId);
        }

        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-username")
    public ResponseEntity<GroupUserDetailDto> getByUsername(
            @RequestParam("username") String username
    ) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ============== UPDATE (PATCH) ==============

    @PatchMapping("/{userId}")
    public ResponseEntity<GroupUserDetailDto> updateUser(
            @RequestParam("groupId") Long groupId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateGroupUserRequest request
    ) {
        GroupUserDetailDto updated = userService.update(groupId, userId, request);
        return ResponseEntity.ok(updated);
    }

    // ============== DELETE ==============

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @RequestParam("groupId") Long groupId,
            @PathVariable Long userId
    ) {
        userService.deleteById(groupId, userId);
    }
}
