package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "notification_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_company_name",
                        columnNames = {"company_id", "name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_company_name",
                        columnList = "company_id, name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationGroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "notification_group_vehicle_codes",
            joinColumns = @JoinColumn(
                    name = "group_id",
                    foreignKey = @ForeignKey(name = "fk_notification_group_vehicle")
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_group_vehicle_code",
                            columnNames = {"group_id", "vehicle_code"}
                    )
            }
    )
    @Column(name = "vehicle_code", nullable = false, length = 50)
    @Builder.Default
    private Set<String> vehicleCodes = new HashSet<>();

    @OneToMany(
            mappedBy = "group",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<GroupUserModel> memberships = new HashSet<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_company")
    )
    private CompanyModel company;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
