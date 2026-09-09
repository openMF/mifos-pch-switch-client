/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.data;

import lombok.Builder;
import lombok.Data;

/**
 * SPSP / payment-pointer style resolution response.
 * Compatible with the information an Interledger / Open Payments client expects
 * when resolving a payment pointer such as $example.com/alice.
 */
@Data
@Builder
public class PaymentPointerResolutionResponse {

    /** The original payment pointer that was resolved. */
    private String paymentPointer;

    /** Canonical wallet address URL (https://...). */
    private String walletAddress;

    /** Rafiki wallet address identifier. */
    private String rafikiWalletAddressId;

    /** Asset that this account accepts. */
    private String assetCode;
    private Short assetScale;

    /** Whether the destination is ready to receive funds. */
    private boolean receivable;

    /** Optional shared secret placeholder (real SPSP would return a STREAM shared secret). */
    private String sharedSecretHint;

    /** Fineract client id that owns this destination. */
    private Long clientId;

    /** Fineract savings account that will be credited. */
    private Long savingsAccountId;
}
