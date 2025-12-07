package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.user.CreateGroupUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.mappers.UserMapper;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
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
    private final UserMapper groupUserMapper;
    private final PasswordEncoder passwordEncoder;

    // ============== CRUD ==============

    @Override
    public GroupUserDetailDto create(Long groupId, CreateGroupUserRequest request) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Notification group not found: " + groupId));

        UserModel user = groupUserMapper.toEntity(request);
        user.setNotificationGroup(group);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel saved = userRepository.save(user);
        return groupUserMapper.toDetailDto(saved);
    }

    @Override
    public GroupUserDetailDto update(Long groupId, Long userId, UpdateGroupUserRequest request) {
        UserModel user = userRepository.findByIdAndNotificationGroup_Id(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in notification group. userId=" + userId + ", groupId=" + groupId
                ));

        // PATCH con MapStruct (ignora nulls)
        groupUserMapper.updateEntityFromDto(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel updated = userRepository.save(user);
        return groupUserMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long groupId, Long userId) {
        UserModel user = userRepository.findByIdAndNotificationGroup_Id(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in notification group. userId=" + userId + ", groupId=" + groupId
                ));

        userRepository.delete(user);
        // Soft-delete opcional:
        // user.setActive(false);
        // userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long userId) {
        return userRepository.findById(userId)
                .map(groupUserMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long groupId, Long userId) {
        return userRepository.findByIdAndNotificationGroup_Id(userId, groupId)
                .map(groupUserMapper::toDetailDto);
    }

    // ============== Listado / búsqueda ==============

    @Override
    @Transactional(readOnly = true)
    public Page<GroupUserSummaryDto> search(Long groupId, String q, Pageable pageable) {
        Page<UserModel> page;

        if (q == null || q.isBlank()) {
            page = userRepository.findByNotificationGroup_Id(groupId, pageable);
        } else {
            String query = q.trim();
            page = userRepository.searchInGroup(groupId, query, pageable);
        }

        return page.map(groupUserMapper::toSummaryDto);
    }
}
