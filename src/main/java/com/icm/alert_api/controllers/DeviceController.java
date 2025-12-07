package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.DeviceRegistrationRequest;
import com.icm.alert_api.models.DeviceRegistrationModel;
import com.icm.alert_api.repositories.DeviceRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRegistrationRepository deviceRepo;

    @PostMapping("/register")
    public void register(@RequestBody DeviceRegistrationRequest dto) {
        DeviceRegistrationModel model = DeviceRegistrationModel.builder()
                .userId(dto.getUserId())
                .expoPushToken(dto.getExpoPushToken())
                .platform(dto.getPlatform())
                .active(true)
                .build();
        deviceRepo.save(model);
    }
}
