package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.company.CompanyDetailDto;
import com.icm.alert_api.dto.company.CompanySummaryDto;
import com.icm.alert_api.dto.company.CreateCompanyRequest;
import com.icm.alert_api.dto.company.UpdateCompanyRequest;
import com.icm.alert_api.services.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    // CREATE
    @PostMapping
    public ResponseEntity<CompanyDetailDto> create(
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        CompanyDetailDto created = companyService.create(request);
        URI location = URI.create("/api/companies/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // READ ONE
    @GetMapping("/{companyId}")
    public CompanyDetailDto findById(@PathVariable Long companyId) {
        return companyService.findById(companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found: " + companyId));
    }

    // LIST / SEARCH
    @GetMapping
    public Page<CompanySummaryDto> search(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return companyService.search(q, pageable);
    }

    // UPDATE (PATCH)
    @PatchMapping("/{companyId}")
    public CompanyDetailDto update(
            @PathVariable Long companyId,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        try {
            return companyService.update(companyId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // DELETE
    @DeleteMapping("/{companyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long companyId) {
        try {
            companyService.deleteById(companyId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
