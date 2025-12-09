package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.DeviceRegistrationRequest;
import com.icm.alert_api.models.DeviceRegistrationModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.DeviceRegistrationRepository;
import com.icm.alert_api.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRegistrationRepository deviceRepo;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody DeviceRegistrationRequest dto) {
        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + dto.getUserId())
                );

        DeviceRegistrationModel model = DeviceRegistrationModel.builder()
                .user(user)                              // <-- aquí va el UserModel
                .expoPushToken(dto.getExpoPushToken())
                .platform(dto.getPlatform())
                .active(true)
                .build();

        deviceRepo.save(model);
    }
}
