package com.icm.alert_api.repositories;

import com.icm.alert_api.models.CompanyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyModel, Long> {

    Optional<CompanyModel> findByNameIgnoreCase(String name);

    Page<CompanyModel> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
