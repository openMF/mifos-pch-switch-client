package org.apache.fineract.rafiki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "m_rafiki_liquidity_tx")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RafikiLiquidityTx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_identifier", nullable = false, length = 100)
    private String tenantIdentifier;

    @Column(name = "rafiki_payment_id", nullable = false, length = 64)
    private String rafikiPaymentId;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction; // INCOMING or OUTGOING

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "asset_code", length = 10)
    private String assetCode;

    @Column(name = "fineract_transaction_id")
    private Long fineractTransactionId;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

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
