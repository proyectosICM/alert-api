package com.icm.alert_api.models;

import com.icm.alert_api.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicle_company_code_norm",
                        columnNames = {"company_id", "vehicle_code_norm"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_vehicle_company_code_norm",
                        columnList = "company_id, vehicle_code_norm"
                ),
                @Index(
                        name = "idx_vehicle_company_plate",
                        columnList = "company_id, license_plate"
                ),
                @Index(
                        name = "idx_vehicle_company_status",
                        columnList = "company_id, status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código como llegó (puede venir con espacios, guiones, minúsculas, etc.)
     * Ej: "fg-22010 ", " MG069"
     */
    @Column(name = "vehicle_code_raw", nullable = false, length = 50)
    private String vehicleCodeRaw;

    /**
     * Código normalizado (para búsquedas y unicidad).
     * Recomendado: trim + upper + quitar espacios internos.
     * Ej: "FG-22010", "MG069"
     */
    @Column(name = "vehicle_code_norm", nullable = false, length = 50)
    private String vehicleCodeNorm;

    /**
     * Placa/patente si viene (opcional).
     */
    @Column(name = "license_plate", length = 50)
    private String licensePlate;

    /**
     * Estado del vehículo:
     * - UNREGISTERED: creado automáticamente por llegada de alertas, falta “ficha” completa
     * - ACTIVE: validado / usado normalmente
     * - DISABLED: dado de baja (opcional)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.UNREGISTERED;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehicle_company")
    )
    private CompanyModel company;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
