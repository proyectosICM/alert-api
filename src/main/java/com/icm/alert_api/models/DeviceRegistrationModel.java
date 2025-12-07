package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 255)
    private String expoPushToken;

    @Column(length = 50)
    private String platform;

    private boolean active = true;
}

