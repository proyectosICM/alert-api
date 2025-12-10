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
                        name = "idx_alert_company_vehicle_time",
                        columnList = "company_id, vehicle_code, event_time"
                ),
                @Index(
                        name = "idx_alert_company_type_time",
                        columnList = "company_id, alert_type, event_time"
                ),
                @Index(
                        name = "idx_alert_company_ack_time",
                        columnList = "company_id, acknowledged, event_time"
                ),
                @Index(
                        name = "idx_alert_company_event_time",
                        columnList = "company_id, event_time"
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
     * Código principal del montacargas / máquina.
     * Puedes usar aquí la placa (FG22010, 308FG25-3, etc.) o el código interno
     * (MG069, H2X394Y02347) según cómo lo manejen ustedes.
     */
    @Column(name = "vehicle_code", nullable = false, length = 50)
    private String vehicleCode;

    /**
     * Placa/patente visible en el correo.
     * En algunos correos viene además del código interno.
     */
    @Column(name = "license_plate", length = 50)
    private String licensePlate;

    /**
     * Tipo principal de alerta: IMPACTO, CHECKLIST, EXCESO_VELOCIDAD, etc.
     */
    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    /**
     * Subtipo o disparador: "Sensor Digital Impacto", "Máquina sin condiciones",
     * "Checklist NOK", etc.
     */
    @Column(name = "alert_subtype", length = 80)
    private String alertSubtype;

    /**
     * Plantilla / origen: "ALARM_EMAIL", "CHECKLIST_EMAIL", "OTRO_SISTEMA", etc.
     * Te sirve para saber de qué formato vino el parseo.
     */
    @Column(name = "template_source", length = 40)
    private String templateSource;

    /**
     * Severidad: INFO, WARNING, CRITICAL, BLOQUEA_OPERACIÓN, etc.
     */
    @Column(name = "severity", length = 20)
    private String severity;

    /**
     * Asunto del correo original.
     */
    @Column(length = 255)
    private String subject;

    /**
     * Planta / sitio: ej. "ABI-MA-PE-T1-Ate Beer".
     */
    @Column(length = 150)
    private String plant;

    /**
     * Área dentro de la planta: PATIO, BAHÍAS CARPA LATAS, LINEA 10, etc.
     */
    @Column(length = 100)
    private String area;

    /**
     * Nombre que aparece como “EQUANS” o similar (proveedor / dueño del equipo).
     * Opcional, pero útil para algunos correos.
     */
    @Column(name = "owner_or_vendor", length = 150)
    private String ownerOrVendor;

    /**
     * Marca / modelo tal como viene: "Toyota/32-8FG25", "Crown/GPC3040", "Linde/H40T-02", etc.
     */
    @Column(name = "brand_model", length = 150)
    private String brandModel;

    @Column(length = 150)
    private String operatorName;

    @Column(length = 100)
    private String operatorId;

    /**
     * Fecha/hora real del evento (la que viene en el correo).
     */
    @Column(name = "event_time", nullable = false)
    private ZonedDateTime eventTime;

    /**
     * Resumen corto, por ejemplo:
     * "Máquina sin condiciones para operar. Fallas: dirección sin juego; rayones..."
     */
    @Column(name = "short_description", length = 1000)
    private String shortDescription;

    /**
     * Detalle largo “normalizado” (markdown / texto plano) con la lista de checks,
     * observaciones, etc. Útil especialmente para los correos de checklist.
     */
    @Lob
    @Column(name = "details")
    private String details;

    /**
     * Payload crudo (HTML completo o texto tal cual llegó).
     * Siempre lo guardamos para poder reprocesar si cambia el parser.
     */
    @Lob
    @Column(name = "raw_payload", nullable = false, columnDefinition = "LONGTEXT")
    private String rawPayload;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alert_company"))
    private CompanyModel company;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
