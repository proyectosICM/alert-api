package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "device_registrations",
        indexes = {
                @Index(
                        name = "idx_device_user_active",
                        columnList = "user_id, active"
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_user")
    )
    private UserModel user;

    @Column(nullable = false, length = 255)
    private String expoPushToken;

    @Column(length = 50)
    private String platform;

    private boolean active = true;
}

