package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "group_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_user",
                        columnNames = {"group_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_user_group",
                        columnList = "group_id"
                ),
                @Index(
                        name = "idx_group_user_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_group_user_group"))
    private NotificationGroupModel group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_group_user_user"))
    private UserModel user;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
