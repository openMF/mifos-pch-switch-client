package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiLiquidityTx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RafikiLiquidityTxRepository extends JpaRepository<RafikiLiquidityTx, Long> {

    Optional<RafikiLiquidityTx> findByTenantIdentifierAndRafikiPaymentId(
            String tenantIdentifier, String rafikiPaymentId);

    List<RafikiLiquidityTx> findByTenantIdentifierAndStatus(String tenantIdentifier, String status);
}
