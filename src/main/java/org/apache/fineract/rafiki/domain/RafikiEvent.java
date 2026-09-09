package org.apache.fineract.rafiki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "m_rafiki_event",
       uniqueConstraints = @UniqueConstraint(name = "uk_rafiki_event_tenant_id",
                                             columnNames = {"tenant_identifier", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RafikiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_identifier", nullable = false, length = 100)
    private String tenantIdentifier;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "processed")
    private Boolean processed = false;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
