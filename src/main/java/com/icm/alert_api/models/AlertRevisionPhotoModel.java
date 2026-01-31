package com.icm.alert_api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "alert_revision_photos",
        indexes = {
                @Index(name = "idx_rev_photo_revision_id", columnList = "revision_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRevisionPhotoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "revision_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_rev_photo_revision")
    )
    private AlertRevisionModel revision;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "content_type", length = 60)
    private String contentType; // image/jpeg, image/png

    @Lob
    @Column(name = "data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
