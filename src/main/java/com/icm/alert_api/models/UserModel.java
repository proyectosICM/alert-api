package com.icm.alert_api.models;

import com.icm.alert_api.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_company_username",
                        columnNames = {"company_id", "username"}
                ),
                @UniqueConstraint(
                        name = "uk_user_company_dni",
                        columnNames = {"company_id", "dni"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_company_username",
                        columnList = "company_id, username"
                ),
                @Index(
                        name = "idx_user_company_dni",
                        columnList = "company_id, dni"
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

    @Column(length = 60)
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GroupUserModel> memberships = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DeviceRegistrationModel> devices = new HashSet<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_company"))
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
