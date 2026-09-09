/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiTenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RafikiTenantConfigRepository extends JpaRepository<RafikiTenantConfig, Long> {

    Optional<RafikiTenantConfig> findByTenantIdentifier(String tenantIdentifier);
}
