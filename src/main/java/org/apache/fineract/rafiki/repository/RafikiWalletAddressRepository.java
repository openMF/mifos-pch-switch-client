/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RafikiWalletAddressRepository extends JpaRepository<RafikiWalletAddress, Long> {

    Optional<RafikiWalletAddress> findByTenantIdentifierAndRafikiWalletAddressId(
            String tenantIdentifier, String rafikiWalletAddressId);

    Optional<RafikiWalletAddress> findByTenantIdentifierAndWalletAddress(
            String tenantIdentifier, String walletAddress);

    List<RafikiWalletAddress> findByTenantIdentifierAndClientId(String tenantIdentifier, Long clientId);
}
