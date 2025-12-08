package com.icm.alert_api.repositories;

import com.icm.alert_api.models.NotificationGroupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroupModel, Long> {

    Page<NotificationGroupModel> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
    );

    @Query("""
           SELECT g
           FROM NotificationGroupModel g
           WHERE :vehicleCode MEMBER OF g.vehicleCodes
           """)
    List<NotificationGroupModel> findByVehicleCodeAssigned(@Param("vehicleCode") String vehicleCode);
}
