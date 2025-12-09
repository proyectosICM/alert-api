package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.user.CreateUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import com.icm.alert_api.mappers.UserMapper;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.models.GroupUserModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.CompanyRepository;
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
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GroupUserDetailDto create(CreateUserRequest request) {
        Long companyId = request.getCompanyId();

        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        UserModel user = userMapper.toEntity(request);
        user.setCompany(company);
        user.setActive(true);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel saved = userRepository.save(user);
        return userMapper.toDetailDto(saved);
    }

    @Override
    public GroupUserDetailDto update(Long companyId, Long userId, UpdateGroupUserRequest request) {
        UserModel user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in company. userId=" + userId + ", companyId=" + companyId
                ));

        userMapper.updateEntityFromDto(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        UserModel updated = userRepository.save(user);
        return userMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long companyId, Long userId) {
        UserModel user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found in company. userId=" + userId + ", companyId=" + companyId
                ));

        userRepository.delete(user);
    }

    // 🔹 Búsqueda por id SOLO por userId
    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupUserDetailDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupUserSummaryDto> search(Long companyId, String q, Pageable pageable) {
        Page<UserModel> page;

        if (q == null || q.isBlank()) {
            page = userRepository.findByCompanyId(companyId, pageable);
        } else {
            String query = q.trim();
            page = userRepository.searchInCompany(companyId, query, pageable);
        }

        return page.map(userMapper::toSummaryDto);
    }
}
