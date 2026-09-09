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
 * Result of discovering a Fineract account that is linked to a Rafiki wallet address
 * or payment pointer.
 */
@Data
@Builder
public class AccountDiscoveryResponse {

    private String walletAddress;
    private String rafikiWalletAddressId;
    private Long clientId;
    private Long savingsAccountId;
    private String assetCode;
    private Short assetScale;
    private String status;

    /** Optional human-readable client display name (stubbed). */
    private String clientDisplayName;

    /** Whether the account can currently receive incoming payments. */
    private boolean receivable;

    /** Optional list of additional linked accounts for the same client. */
    private List<LinkedAccountSummary> linkedAccounts;

    @Data
    @Builder
    public static class LinkedAccountSummary {
        private Long savingsAccountId;
        private String accountNumber;
        private String currency;
        private String status;
    }
}
