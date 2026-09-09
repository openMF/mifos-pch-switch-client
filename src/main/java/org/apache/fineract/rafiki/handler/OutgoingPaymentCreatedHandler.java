package org.apache.fineract.rafiki.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.service.LiquidityService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutgoingPaymentCreatedHandler implements RafikiEventHandler {

    private final LiquidityService liquidityService;

    @Override
    public String supports() {
        return "outgoing_payment.created";
    }

    @Override
    public void handle(RafikiWebhookEvent event) {
        Map<String, Object> data = event.getData();
        if (data == null) {
            throw new RafikiConnectorException("Missing data in outgoing_payment.created");
        }

        String paymentId = stringVal(data.get("id"));
        BigDecimal debitAmount = parseAmount(data.get("debitAmount"));

        // In production: check balance, place hold on Fineract account, then deposit liquidity
        liquidityService.depositOutgoingPaymentLiquidity(paymentId, debitAmount, "USD");

        log.info("Handled outgoing_payment.created for payment {} amount {}", paymentId, debitAmount);
    }

    private String stringVal(Object o) {
        return o == null ? null : o.toString();
    }

    private BigDecimal parseAmount(Object amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (amount instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(amount.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
