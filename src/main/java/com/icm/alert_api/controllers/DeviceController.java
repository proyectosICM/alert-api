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

        String expoToken = dto.getExpoPushToken();
        String platform = dto.getPlatform();

        // 👇 buscar si ya existe registro para ese user + token
        DeviceRegistrationModel model = deviceRepo
                .findByUserIdAndExpoPushToken(user.getId(), expoToken)
                .orElseGet(() -> DeviceRegistrationModel.builder()
                        .user(user)
                        .expoPushToken(expoToken)
                        .build()
                );

        // actualizar campos
        model.setPlatform(platform);
        model.setActive(true); // al registrar, queda activo

        deviceRepo.save(model);
    }
}

