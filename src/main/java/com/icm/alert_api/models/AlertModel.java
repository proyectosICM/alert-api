package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "alerts",
        indexes = {
                @Index(
                        name = "idx_alert_vehicle_time",
                        columnList = "vehicle_code, event_time"
                ),
                @Index(
                        name = "idx_alert_type_time",
                        columnList = "alert_type, event_time"
                ),
                @Index(
                        name = "idx_alert_ack_time",
                        columnList = "acknowledged, event_time"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código / placa del montacargas (ej: "MG069").
     */
    @Column(name = "vehicle_code", nullable = false, length = 50)
    private String vehicleCode;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String type; // IMPACTO, EXCESO_VELOCIDAD, etc.

    @Column(length = 255)
    private String subject;

    @Column(length = 100)
    private String area;

    @Column(length = 150)
    private String operatorName;

    @Column(length = 100)
    private String operatorId;

    @Column(name = "event_time", nullable = false)
    private ZonedDateTime eventTime;

    @Column(length = 500)
    private String shortDescription;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
