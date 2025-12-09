package com.icm.alert_api.services;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationGroupService {

    // companyId viene dentro de CreateGroupRequest
    GroupDetailDto create(CreateGroupRequest request);

    // companyId viene dentro de UpdateGroupRequest
    GroupDetailDto update(Long groupId, UpdateGroupRequest request);

    // Para DELETE seguimos usando companyId por query param
    void deleteById(Long companyId, Long groupId);

    // Para READ ONE también companyId por query param
    Optional<GroupDetailDto> findById(Long companyId, Long groupId);

    // Para SEARCH igualmente companyId por query param
    Page<GroupSummaryDto> search(Long companyId, String q, Pageable pageable);
}
