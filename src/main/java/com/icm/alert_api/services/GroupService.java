package com.icm.alert_api.services;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface GroupService {
    // ============== CRUD ==============

    GroupDetailDto create(CreateGroupRequest request);

    GroupDetailDto update(Long groupId, UpdateGroupRequest request);

    void deleteById(Long groupId);

    Optional<GroupDetailDto> findById(Long groupId);

    // ============== Listado / búsqueda ==============

    Page<GroupSummaryDto> search(String q, Pageable pageable);
}
