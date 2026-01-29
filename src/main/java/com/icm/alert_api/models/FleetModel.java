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
        name = "fleets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fleet_company_name",
                        columnNames = {"company_id", "name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_fleet_company_name",
                        columnList = "company_id, name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetModel {

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
            name = "fleet_vehicle_codes",
            joinColumns = @JoinColumn(
                    name = "fleet_id",
                    foreignKey = @ForeignKey(name = "fk_fleet_vehicle")
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_fleet_vehicle_code",
                            columnNames = {"fleet_id", "vehicle_code"}
                    )
            }
    )
    @Column(name = "vehicle_code", nullable = false, length = 50)
    @Builder.Default
    private Set<String> vehicleCodes = new HashSet<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fleet_company")
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
