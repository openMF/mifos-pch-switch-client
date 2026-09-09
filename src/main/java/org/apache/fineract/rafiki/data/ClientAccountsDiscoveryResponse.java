/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Discovery result when listing all Rafiki-mapped accounts that belong to a
 * given Fineract client.
 */
@Data
@Builder
public class ClientAccountsDiscoveryResponse {

    private Long clientId;
    private String tenantIdentifier;
    private List<AccountDiscoveryResponse> accounts;
    private int totalMappedAccounts;
}
