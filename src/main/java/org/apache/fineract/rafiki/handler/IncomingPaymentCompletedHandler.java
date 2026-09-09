/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.service.LiquidityService;
import org.apache.fineract.rafiki.service.WalletAddressMappingService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncomingPaymentCompletedHandler implements RafikiEventHandler {

    private final LiquidityService liquidityService;
    private final WalletAddressMappingService mappingService;

    @Override
    public String supports() {
        return "incoming_payment.completed";
    }

    @Override
    public void handle(RafikiWebhookEvent event) {
        Map<String, Object> data = event.getData();
        if (data == null) {
            throw new RafikiConnectorException("Missing data in incoming_payment.completed");
        }

        String paymentId = stringVal(data.get("id"));
        String walletAddressId = stringVal(data.get("walletAddressId"));
        BigDecimal amount = parseAmount(data.get("receivedAmount"));

        RafikiWalletAddress mapping = mappingService.findByRafikiWalletAddressId(walletAddressId)
                .orElseThrow(() -> new RafikiConnectorException(
                        "No Fineract mapping for Rafiki walletAddressId: " + walletAddressId));

        // 1. Withdraw from Rafiki
        liquidityService.withdrawIncomingPayment(paymentId, amount, mapping.getAssetCode());

        // 2. Credit the mapped Fineract account
        if (mapping.getSavingsAccountId() != null) {
            Long txId = liquidityService.creditFineractAccount(
                    mapping.getSavingsAccountId(), amount, paymentId);
            log.info("Credited savings account {} with {} (fineractTxId={})",
                    mapping.getSavingsAccountId(), amount, txId);
        } else {
            log.warn("Wallet mapping {} has no savingsAccountId – liquidity withdrawn but not credited",
                    mapping.getId());
        }
    }

    private String stringVal(Object o) {
        return o == null ? null : o.toString();
    }

    private BigDecimal parseAmount(Object receivedAmount) {
        // Rafiki amounts are typically strings like "1000" (scaled) or objects.
        // Simplified parser for the skeleton.
        if (receivedAmount == null) {
            return BigDecimal.ZERO;
        }
        if (receivedAmount instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        String s = receivedAmount.toString();
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            log.warn("Could not parse receivedAmount: {}", s);
            return BigDecimal.ZERO;
        }
    }
}
