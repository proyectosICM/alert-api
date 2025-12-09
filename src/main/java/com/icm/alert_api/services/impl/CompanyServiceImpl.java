package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.company.CompanyDetailDto;
import com.icm.alert_api.dto.company.CompanySummaryDto;
import com.icm.alert_api.dto.company.CreateCompanyRequest;
import com.icm.alert_api.dto.company.UpdateCompanyRequest;
import com.icm.alert_api.mappers.CompanyMapper;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.repositories.AlertRepository;
import com.icm.alert_api.repositories.CompanyRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationGroupRepository groupRepository;
    private final AlertRepository alertRepository;
    private final CompanyMapper companyMapper;

    private long resolveUsersCount(Long companyId) {
        return userRepository.countByCompanyId(companyId);
    }

    private long resolveGroupsCount(Long companyId) {
        return groupRepository.countByCompany_Id(companyId);
    }

    private long resolveAlertsCount(Long companyId) {
        return alertRepository.countByCompany_Id(companyId);
    }

    @Override
    public CompanyDetailDto create(CreateCompanyRequest request) {
        CompanyModel model = companyMapper.toEntity(request);
        CompanyModel saved = companyRepository.save(model);

        long usersCount = 0L;
        long groupsCount = 0L;
        long alertsCount = 0L;

        return companyMapper.toDetailDto(saved, usersCount, groupsCount, alertsCount);
    }

    @Override
    public CompanyDetailDto update(Long companyId, UpdateCompanyRequest request) {
        CompanyModel model = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        companyMapper.updateEntityFromDto(request, model);

        CompanyModel updated = companyRepository.save(model);

        long usersCount = resolveUsersCount(updated.getId());
        long groupsCount = resolveGroupsCount(updated.getId());
        long alertsCount = resolveAlertsCount(updated.getId());

        return companyMapper.toDetailDto(updated, usersCount, groupsCount, alertsCount);
    }

    @Override
    public void deleteById(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Company not found: " + companyId);
        }
        // gracias al cascade = ALL + orphanRemoval = true en CompanyModel,
        // se borran users, groups y alerts de esa empresa automáticamente.
        companyRepository.deleteById(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyDetailDto> findById(Long companyId) {
        return companyRepository.findById(companyId)
                .map(model -> {
                    long usersCount = resolveUsersCount(model.getId());
                    long groupsCount = resolveGroupsCount(model.getId());
                    long alertsCount = resolveAlertsCount(model.getId());
                    return companyMapper.toDetailDto(model, usersCount, groupsCount, alertsCount);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanySummaryDto> search(String q, Pageable pageable) {
        Page<CompanyModel> page;

        if (q == null || q.isBlank()) {
            page = companyRepository.findAll(pageable);
        } else {
            String query = q.trim();
            page = companyRepository.findByNameContainingIgnoreCase(query, pageable);
        }

        return page.map(model -> {
            long usersCount = resolveUsersCount(model.getId());
            long groupsCount = resolveGroupsCount(model.getId());
            long alertsCount = resolveAlertsCount(model.getId());
            return companyMapper.toSummaryDto(model, usersCount, groupsCount, alertsCount);
        });
    }
}
