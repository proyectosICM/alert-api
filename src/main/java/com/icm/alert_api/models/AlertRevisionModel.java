package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "alert_revisions",
        indexes = {
                @Index(name = "idx_revision_company_id", columnList = "company_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_revision_alert_id", columnNames = "alert_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRevisionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "alert_id",
            nullable = false,
            unique = true, // <-- clave para que sea 1 a 1
            foreignKey = @ForeignKey(name = "fk_revision_alert")
    )
    private AlertModel alert;

    // (opcional) redundante para filtrar rápido por empresa sin join
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(length = 100)
    private String vehiculo;

    @Column(length = 150)
    private String planta;

    @Column(length = 150)
    private String area;

    @Column(length = 150)
    private String operador;

    @Lob
    @Column(name = "motivo_falla")
    private String motivoFalla;

    @Column(name = "fecha_falla")
    private LocalDate fechaFalla;

    @Lob
    @Column(name = "accion_tomada")
    private String accionTomada;

    @Column(name = "revisor_nombre", length = 150, nullable = false)
    private String revisorNombre;

    @Lob
    @Column(name = "observacion_adicional")
    private String observacionAdicional;

    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertRevisionPhotoModel> fotos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
