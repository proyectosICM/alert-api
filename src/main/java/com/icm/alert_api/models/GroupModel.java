package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection
    @CollectionTable(
            name = "group_vehicle_codes",
            joinColumns = @JoinColumn(
                    name = "group_id",
                    foreignKey = @ForeignKey(name = "fk_group_vehicle_group")
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
    private Set<UserModel> users = new HashSet<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
