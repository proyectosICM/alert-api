package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.mappers.UserMapper;
import com.icm.alert_api.models.GroupUserModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.GroupUserRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.GroupUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupUserServiceImpl implements GroupUserService {

    private final GroupUserRepository groupUserRepository;
    private final NotificationGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public GroupUserDetailDto addUserToGroup(Long groupId, Long userId) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Notification group not found: " + groupId));

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validar que sean de la misma empresa
        if (!user.getCompany().getId().equals(group.getCompany().getId())) {
            throw new IllegalArgumentException("User and group belong to different companies");
        }

        // Si ya existe membresía, la reactivamos; si no, la creamos
        GroupUserModel membership = groupUserRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .map(existing -> {
                    existing.setActive(true);
                    return existing;
                })
                .orElseGet(() -> GroupUserModel.builder()
                        .group(group)
                        .user(user)
                        .active(true)
                        .build()
                );

        groupUserRepository.save(membership);

        // Detail del usuario dentro del contexto del grupo (pero DTO sigue siendo de usuario)
        return userMapper.toDetailDto(user);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        GroupUserModel membership = groupUserRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in notification group. userId=" + userId + ", groupId=" + groupId
                ));

        // Soft delete: lo marcamos inactivo (puedes cambiar a delete si prefieres)
        membership.setActive(false);
        groupUserRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupUserSummaryDto> listMembers(Long groupId, String q, Pageable pageable) {
        Page<GroupUserModel> page;

        if (q == null || q.isBlank()) {
            // Si agregaste el método nuevo en el repo, usa éste:
            page = groupUserRepository.findByGroup_IdAndActiveTrue(groupId, pageable);
            // Si NO quieres tocar el repo, podrías usar:
            // page = groupUserRepository.findByGroup_Id(groupId, pageable);
        } else {
            String query = q.trim();
            page = groupUserRepository.searchMembersInGroup(groupId, query, pageable);
        }

        // Devuelves Summary a partir del User asociado
        return page.map(gu -> userMapper.toSummaryDto(gu.getUser()));
    }
}
