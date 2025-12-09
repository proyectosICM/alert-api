package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.services.GroupUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/group-users")
@RequiredArgsConstructor
@Validated
public class GroupUserController {

    private final GroupUserService groupUserService;

    /**
     * Asigna un usuario existente a un grupo.
     *
     * POST /api/group-users?groupId=...&userId=...
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupUserDetailDto addUserToGroup(
            @RequestParam("groupId") Long groupId,
            @RequestParam("userId") Long userId
    ) {
        try {
            return groupUserService.addUserToGroup(groupId, userId);
        } catch (IllegalArgumentException ex) {
            // Si el grupo o el usuario no existen, o son de distinta empresa
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    /**
     * Quita a un usuario de un grupo (soft-delete de la membresía).
     *
     * DELETE /api/group-users?groupId=...&userId=...
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromGroup(
            @RequestParam("groupId") Long groupId,
            @RequestParam("userId") Long userId
    ) {
        try {
            groupUserService.removeUserFromGroup(groupId, userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /**
     * Lista / busca miembros de un grupo.
     *
     * GET /api/group-users?groupId=...&q=...&page=0&size=20
     *
     * (muy similar a lo que ya haces con /api/users?groupId=..., pero aquí queda
     * explícitamente como controller de la relación grupo-usuario)
     */
    @GetMapping
    public Page<GroupUserSummaryDto> listMembers(
            @RequestParam("groupId") Long groupId,
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return groupUserService.listMembers(groupId, q, pageable);
    }
}
