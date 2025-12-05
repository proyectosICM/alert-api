package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.mappers.NotificationGroupMapper;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.services.NotificationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationGroupServiceImpl implements NotificationGroupService {

    private final NotificationGroupRepository groupRepository;
    private final NotificationGroupMapper groupMapper;
    // Si luego tienes GroupUserRepository / AlertRepository, puedes inyectarlos aquí
    // private final GroupUserRepository groupUserRepository;
    // private final AlertRepository alertRepository;

    // ============== CRUD ==============

    @Override
    public GroupDetailDto create(CreateGroupRequest request) {
        NotificationGroupModel model = groupMapper.toEntity(request);
        NotificationGroupModel saved = groupRepository.save(model);

        long usersCount = 0L;     // TODO: cuando tengas usuarios reales
        long alertsLast24h = 0L;  // TODO: cuando tengas alertas

        return groupMapper.toDetailDto(saved, usersCount, alertsLast24h);
    }

    @Override
    public GroupDetailDto update(Long groupId, UpdateGroupRequest request) {
        NotificationGroupModel model = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        // patch con MapStruct
        groupMapper.updateEntityFromDto(request, model);

        NotificationGroupModel updated = groupRepository.save(model);

        long usersCount = 0L;     // TODO
        long alertsLast24h = 0L;  // TODO

        return groupMapper.toDetailDto(updated, usersCount, alertsLast24h);
    }

    @Override
    public void deleteById(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        groupRepository.deleteById(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupDetailDto> findById(Long groupId) {
        return groupRepository.findById(groupId)
                .map(model -> {
                    long usersCount = 0L;     // TODO: traer desde repositorio de usuarios
                    long alertsLast24h = 0L;  // TODO: traer desde alertas
                    return groupMapper.toDetailDto(model, usersCount, alertsLast24h);
                });
    }

    // ============== Listado / búsqueda ==============

    @Override
    @Transactional(readOnly = true)
    public Page<GroupSummaryDto> search(String q, Pageable pageable) {
        Page<NotificationGroupModel> page;

        if (q == null || q.isBlank()) {
            page = groupRepository.findAll(pageable);
        } else {
            String query = q.trim();
            page = groupRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            query, query, pageable
                    );
        }

        return page.map(model -> {
            long usersCount = 0L;     // TODO: contar usuarios del grupo
            long alertsLast24h = 0L;  // TODO: contar alertas 24h
            return groupMapper.toSummaryDto(model, usersCount, alertsLast24h);
        });
    }
}
