package com.icm.alert_api.services;

import com.icm.alert_api.dto.user.CreateGroupUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    // ============== CRUD ==============

    GroupUserDetailDto create(Long groupId, CreateGroupUserRequest request);

    GroupUserDetailDto update(Long groupId, Long userId, UpdateGroupUserRequest request);

    void deleteById(Long groupId, Long userId);

    Optional<GroupUserDetailDto> findById(Long userId);

    Optional<GroupUserDetailDto> findById(Long groupId, Long userId);

    // ============== Listado / búsqueda ==============

    Page<GroupUserSummaryDto> search(Long groupId, String q, Pageable pageable);
}
