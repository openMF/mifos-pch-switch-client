package org.apache.fineract.rafiki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "m_rafiki_wallet_address",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_rafiki_wallet_tenant_address",
                             columnNames = {"tenant_identifier", "wallet_address"}),
           @UniqueConstraint(name = "uk_rafiki_wallet_tenant_rafiki_id",
                             columnNames = {"tenant_identifier", "rafiki_wallet_address_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RafikiWalletAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_identifier", nullable = false, length = 100)
    private String tenantIdentifier;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "savings_account_id")
    private Long savingsAccountId;

    @Column(name = "wallet_address", nullable = false, length = 512)
    private String walletAddress;

    @Column(name = "rafiki_wallet_address_id", nullable = false, length = 64)
    private String rafikiWalletAddressId;

    @Column(name = "asset_code", length = 10)
    private String assetCode;

    @Column(name = "asset_scale")
    private Short assetScale;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

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
