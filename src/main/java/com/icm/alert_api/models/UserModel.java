package com.icm.alert_api.models;

import com.icm.alert_api.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_username",
                        columnNames = {"notification_group_id", "username"}
                ),
                @UniqueConstraint(
                        name = "uk_group_dni",
                        columnNames = {"notification_group_id", "dni"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_username",
                        columnList = "notification_group_id, username"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 15)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notification_group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_notification_group")
    )
    private NotificationGroupModel notificationGroup;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
