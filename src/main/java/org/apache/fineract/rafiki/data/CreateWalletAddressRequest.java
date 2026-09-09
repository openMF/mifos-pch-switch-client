/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWalletAddressRequest {

    @NotNull
    private Long clientId;

    private Long savingsAccountId;

    @NotBlank
    private String walletAddress;

    private String assetCode = "USD";

    private Short assetScale = 2;
}
