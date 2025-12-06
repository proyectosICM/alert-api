package com.icm.alert_api.repositories;

import com.icm.alert_api.models.NotificationGroupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroupModel, Long> {

    long countByNotificationGroup_Id(Long groupId);

    Page<NotificationGroupModel> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
    );
}
