package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.group.CreateGroupRequest;
import com.icm.alert_api.dto.group.GroupDetailDto;
import com.icm.alert_api.dto.group.GroupSummaryDto;
import com.icm.alert_api.dto.group.UpdateGroupRequest;
import com.icm.alert_api.mappers.NotificationGroupMapper;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.repositories.AlertRepository;
import com.icm.alert_api.repositories.CompanyRepository;
import com.icm.alert_api.repositories.GroupUserRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.services.NotificationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationGroupServiceImpl implements NotificationGroupService {

    private final NotificationGroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final AlertRepository alertRepository;
    private final CompanyRepository companyRepository;
    private final NotificationGroupMapper groupMapper;

    // ==== Helpers internos ====

    private long resolveUsersCount(Long groupId) {
        return groupUserRepository.countByGroup_IdAndActiveTrue(groupId);
    }

    private long resolveAlertsLast24h(NotificationGroupModel group) {
        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return 0L;
        }
        Long companyId = group.getCompany().getId();
        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(24);
        return alertRepository.countByCompanyIdAndVehicleCodeInAndEventTimeAfter(
                companyId, vehicleCodes, cutoff
        );
    }

    // ============== CRUD ==============

    @Override
    public GroupDetailDto create(CreateGroupRequest request) {
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required in CreateGroupRequest");
        }

        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        NotificationGroupModel model = groupMapper.toEntity(request);
        model.setCompany(company);

        NotificationGroupModel saved = groupRepository.save(model);
        model.setVehiclePlates(normalizePlates(request.getVehiclePlates()));
        model.setVehicleCodes(normalizeCodes(request.getVehicleCodes()));

        long usersCount = 0L; // recién creado, aún sin usuarios asociados
        long alertsLast24h = resolveAlertsLast24h(saved);

        return groupMapper.toDetailDto(saved, usersCount, alertsLast24h);
    }

    @Override
    public GroupDetailDto update(Long groupId, UpdateGroupRequest request) {
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required in UpdateGroupRequest");
        }

        NotificationGroupModel model = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        // patch con MapStruct
        groupMapper.updateEntityFromDto(request, model);

        if (request.getVehiclePlates() != null) {
            model.setVehiclePlates(normalizePlates(request.getVehiclePlates()));
        }
        if (request.getVehicleCodes() != null) {
            model.setVehicleCodes(normalizeCodes(request.getVehicleCodes()));
        }

        NotificationGroupModel updated = groupRepository.save(model);

        long usersCount = resolveUsersCount(updated.getId());
        long alertsLast24h = resolveAlertsLast24h(updated);

        return groupMapper.toDetailDto(updated, usersCount, alertsLast24h);
    }

    @Override
    public void deleteById(Long companyId, Long groupId) {
        NotificationGroupModel model = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        groupRepository.delete(model);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupDetailDto> findById(Long companyId, Long groupId) {
        return groupRepository.findById(groupId)
                .filter(g -> g.getCompany().getId().equals(companyId))
                .map(model -> {
                    long usersCount = resolveUsersCount(model.getId());
                    long alertsLast24h = resolveAlertsLast24h(model);
                    return groupMapper.toDetailDto(model, usersCount, alertsLast24h);
                });
    }

    // ============== Listado / búsqueda ==============

    @Override
    @Transactional(readOnly = true)
    public Page<GroupSummaryDto> search(Long companyId, String q, Pageable pageable) {
        Page<NotificationGroupModel> page;

        if (q == null || q.isBlank()) {
            page = groupRepository.findByCompanyId(companyId, pageable);
        } else {
            String query = q.trim();
            page = groupRepository.searchByCompanyAndText(companyId, query, pageable);
        }

        return page.map(model -> {
            long usersCount = resolveUsersCount(model.getId());
            long alertsLast24h = resolveAlertsLast24h(model);
            return groupMapper.toSummaryDto(model, usersCount, alertsLast24h);
        });
    }

    // ============== Helpers de normalización ==============

    private Set<String> normalizePlates(Collection<String> plates) {
        if (plates == null) return new HashSet<>();

        return plates.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::normalizePlate)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizePlate(String s) {
        // UPPER + quitar espacios + quitar guiones
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        x = x.replace("-", "");
        return x;
    }

    private Set<String> normalizeCodes(Collection<String> codes) {
        if (codes == null) return new HashSet<>();

        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::normalizeCode)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeCode(String s) {
        // igual que tu backend: trim + upper + quitar espacios internos
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        return x;
    }
}
