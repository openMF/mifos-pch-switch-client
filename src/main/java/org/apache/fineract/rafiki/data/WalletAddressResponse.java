/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletAddressResponse {

    private Long id;
    private Long clientId;
    private Long savingsAccountId;
    private String walletAddress;
    private String rafikiWalletAddressId;
    private String assetCode;
    private Short assetScale;
    private String status;
}
