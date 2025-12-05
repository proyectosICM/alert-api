package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.user.CreateGroupUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.mappers.UserMapper;
import com.icm.alert_api.models.GroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.GroupRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserMapper groupUserMapper;

    // ============== CRUD ==============

    @Override
    public GroupUserDetailDto create(Long groupId, CreateGroupUserRequest request) {
        GroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        UserModel user = groupUserMapper.toEntity(request);
        user.setGroup(group); // muy importante para la FK

        UserModel saved = userRepository.save(user);
        return groupUserMapper.toDetailDto(saved);
    }

    @Override
    public GroupUserDetailDto update(Long groupId, Long userId, UpdateGroupUserRequest request) {
        UserModel user = userRepository.findByIdAndGroup_Id(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in group. userId=" + userId + ", groupId=" + groupId
                ));

        // PATCH con MapStruct (ignora nulls)
        groupUserMapper.updateEntityFromDto(request, user);

        UserModel updated = userRepository.save(user);
        return groupUserMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long groupId, Long userId) {
        UserModel user = userRepository.findByIdAndGroup_Id(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in group. userId=" + userId + ", groupId=" + groupId
                ));

        userRepository.delete(user);
        // Si quisieras soft-delete:
        // user.setActive(false);
        // userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long groupId, Long userId) {
        return userRepository.findByIdAndGroup_Id(userId, groupId)
                .map(groupUserMapper::toDetailDto);
    }

    // ============== Listado / búsqueda ==============

    @Override
    @Transactional(readOnly = true)
    public Page<GroupUserSummaryDto> search(Long groupId, String q, Pageable pageable) {
        Page<UserModel> page;

        if (q == null || q.isBlank()) {
            page = userRepository.findByGroup_Id(groupId, pageable);
        } else {
            String query = q.trim();
            page = userRepository.searchInGroup(groupId, query, pageable);
        }

        return page.map(groupUserMapper::toSummaryDto);
    }
}
