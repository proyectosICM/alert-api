package com.icm.alert_api.models;

import com.icm.alert_api.persistence.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shifts",
        indexes = {
                @Index(name = "idx_shift_company_active", columnList = "company_id, active"),
                @Index(name = "idx_shift_company_date", columnList = "company_id, roster_date"),
                @Index(name = "idx_shift_company_batch", columnList = "company_id, batch_id")
        },
        uniqueConstraints = {
                // evita duplicar el mismo turno del mismo día en la misma importación
                @UniqueConstraint(
                        name = "uk_shift_company_date_name_batch",
                        columnNames = {"company_id", "roster_date", "shift_name", "batch_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyModel company;

    @Column(name = "roster_date", nullable = false)
    private LocalDate rosterDate;

    @Column(name = "shift_name", nullable = false, length = 120)
    private String shiftName;

    @Column(name = "batch_id", nullable = false, length = 36)
    private String batchId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "responsible_dnis_json", nullable = false, columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> responsibleDnis = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "vehicle_plates_json", nullable = false, columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> vehiclePlates = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
