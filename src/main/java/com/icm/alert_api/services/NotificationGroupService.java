package com.icm.alert_api.services;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationGroupService {

    GroupDetailDto create(Long companyId, CreateGroupRequest request);

    GroupDetailDto update(Long companyId, Long groupId, UpdateGroupRequest request);

    void deleteById(Long companyId, Long groupId);

    Optional<GroupDetailDto> findById(Long companyId, Long groupId);

    Page<GroupSummaryDto> search(Long companyId, String q, Pageable pageable);
}
