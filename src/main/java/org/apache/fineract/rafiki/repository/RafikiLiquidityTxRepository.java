/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
