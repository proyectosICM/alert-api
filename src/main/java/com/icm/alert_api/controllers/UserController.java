package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.user.CreateUserRequest;
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

    /**
     * Crea un usuario dentro de una empresa.
     *
     * POST /api/users
     * Body: { fullName, username, dni, password, role, companyId }
     */
    @PostMapping
    public ResponseEntity<GroupUserDetailDto> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        GroupUserDetailDto created = userService.create(request);

        URI location = URI.create("/api/users/" + created.getId());

        return ResponseEntity
                .created(location)
                .body(created);
    }

    // ============== LISTAR / BUSCAR ==============

    @GetMapping
    public Page<GroupUserSummaryDto> searchUsers(
            @RequestParam("companyId") Long companyId,
            @RequestParam(name = "q", required = false) String q,
            Pageable pageable
    ) {
        return userService.search(companyId, q, pageable);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GroupUserDetailDto> getUser(
            @PathVariable Long userId,
            @RequestParam("companyId") Long companyId
    ) {
        Optional<GroupUserDetailDto> opt = userService.findById(companyId, userId);

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

    @PatchMapping("/{userId}")
    public ResponseEntity<GroupUserDetailDto> updateUser(
            @RequestParam("companyId") Long companyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateGroupUserRequest request
    ) {
        GroupUserDetailDto updated = userService.update(companyId, userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @RequestParam("companyId") Long companyId,
            @PathVariable Long userId
    ) {
        userService.deleteById(companyId, userId);
    }
}
