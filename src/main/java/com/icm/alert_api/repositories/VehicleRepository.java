package com.icm.alert_api.repositories;

import com.icm.alert_api.models.VehicleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleModel, Long> {

    Optional<VehicleModel> findByCompany_IdAndVehicleCodeNorm(Long companyId, String vehicleCodeNorm);
    Page<VehicleModel> findByCompany_Id(Long companyId, Pageable pageable);
    Optional<VehicleModel> findByCompany_IdAndLicensePlate(Long companyId, String licensePlate);
    boolean existsByCompany_IdAndVehicleCodeNorm(Long companyId, String vehicleCodeNorm);

}
