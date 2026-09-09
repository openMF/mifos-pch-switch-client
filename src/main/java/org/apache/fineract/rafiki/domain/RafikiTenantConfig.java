package org.apache.fineract.rafiki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "m_rafiki_tenant_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RafikiTenantConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_identifier", nullable = false, unique = true, length = 100)
    private String tenantIdentifier;

    @Column(name = "rafiki_admin_url", nullable = false, length = 512)
    private String rafikiAdminUrl;

    @Column(name = "rafiki_webhook_secret", length = 256)
    private String rafikiWebhookSecret;

    @Column(name = "rafiki_signature_version", length = 10)
    private String rafikiSignatureVersion = "1";

    @Column(name = "default_asset_code", length = 10)
    private String defaultAssetCode = "USD";

    @Column(name = "default_asset_scale")
    private Short defaultAssetScale = 2;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
