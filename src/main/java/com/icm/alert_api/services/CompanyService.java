package com.icm.alert_api.services;

import com.icm.alert_api.dto.company.CompanyDetailDto;
import com.icm.alert_api.dto.company.CompanySummaryDto;
import com.icm.alert_api.dto.company.CreateCompanyRequest;
import com.icm.alert_api.dto.company.UpdateCompanyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompanyService {

    CompanyDetailDto create(CreateCompanyRequest request);

    CompanyDetailDto update(Long companyId, UpdateCompanyRequest request);

    void deleteById(Long companyId);

    Optional<CompanyDetailDto> findById(Long companyId);

    Page<CompanySummaryDto> search(String q, Pageable pageable);
}
