package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.user.CreateUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.mappers.UserMapper;
import com.icm.alert_api.models.GroupUserModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.GroupUserRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationGroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ============== CRUD ==============

    @Override
    public GroupUserDetailDto create(Long groupId, CreateUserRequest request) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Notification group not found: " + groupId));

        // Crear usuario en la empresa del grupo
        UserModel user = userMapper.toEntity(request);
        user.setCompany(group.getCompany());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel savedUser = userRepository.save(user);

        // Crear membresía grupo-usuario
        GroupUserModel membership = GroupUserModel.builder()
                .group(group)
                .user(savedUser)
                .active(true)
                .build();

        groupUserRepository.save(membership);

        return userMapper.toDetailDto(savedUser);
    }

    @Override
    public GroupUserDetailDto update(Long groupId, Long userId, UpdateGroupUserRequest request) {
        GroupUserModel membership = groupUserRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in notification group. userId=" + userId + ", groupId=" + groupId
                ));

        UserModel user = membership.getUser();

        // PATCH con MapStruct
        userMapper.updateEntityFromDto(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel updated = userRepository.save(user);
        return userMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long groupId, Long userId) {
        GroupUserModel membership = groupUserRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in notification group. userId=" + userId + ", groupId=" + groupId
                ));

        groupUserRepository.delete(membership);

        // Si quisieras borrar el usuario cuando ya no tiene más grupos:
        // if (user.getMemberships().isEmpty()) { userRepository.delete(user); }
    }

    // ============== Queries ==============

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long groupId, Long userId) {
        return groupUserRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .map(GroupUserModel::getUser)
                .map(userMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDetailDto);
    }

    // ============== Listado / búsqueda miembros de un grupo ==============

    @Override
    @Transactional(readOnly = true)
    public Page<GroupUserSummaryDto> search(Long groupId, String q, Pageable pageable) {
        Page<GroupUserModel> page;

        if (q == null || q.isBlank()) {
            page = groupUserRepository.findByGroup_Id(groupId, pageable);
        } else {
            String query = q.trim();
            page = groupUserRepository.searchMembersInGroup(groupId, query, pageable);
        }

        // Mapear membership -> DTO a partir del User
        return page.map(gu -> userMapper.toSummaryDto(gu.getUser()));
    }
}
