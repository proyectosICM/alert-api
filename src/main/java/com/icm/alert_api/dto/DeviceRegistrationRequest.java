package com.icm.alert_api.dto;

import lombok.Data;

@Data
public class DeviceRegistrationRequest {
    private Long userId;
    private String expoPushToken;
    private String platform;
    private Boolean active;
}
