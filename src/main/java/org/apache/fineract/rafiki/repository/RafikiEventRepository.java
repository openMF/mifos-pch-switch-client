/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RafikiEventRepository extends JpaRepository<RafikiEvent, Long> {

    boolean existsByTenantIdentifierAndEventId(String tenantIdentifier, String eventId);
}
