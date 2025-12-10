package com.icm.alert_api.repositories;

import com.icm.alert_api.models.DeviceRegistrationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistrationModel, Long> {

    Optional<DeviceRegistrationModel> findByUserIdAndExpoPushToken(Long userId, String expoPushToken);

    List<DeviceRegistrationModel> findByUserIdAndActiveTrue(Long userId);

    List<DeviceRegistrationModel> findByUserIdInAndActiveTrue(Collection<Long> userIds);
}
