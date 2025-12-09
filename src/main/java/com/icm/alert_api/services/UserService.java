package com.icm.alert_api.services;

import com.icm.alert_api.dto.user.CreateUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    // Crear usuario nuevo en una empresa (companyId viene en el DTO)
    GroupUserDetailDto create(CreateUserRequest request);

    // Actualizar datos del usuario dentro de una empresa
    GroupUserDetailDto update(Long companyId, Long userId, UpdateGroupUserRequest request);

    void deleteById(Long companyId, Long userId);

    Optional<GroupUserDetailDto> findById(Long companyId, Long userId);

    Optional<GroupUserDetailDto> findById(Long userId);

    Optional<GroupUserDetailDto> findByUsername(String username);

    Page<GroupUserSummaryDto> search(Long companyId, String q, Pageable pageable);
}
