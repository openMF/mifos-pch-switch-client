package org.apache.fineract.rafiki.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.springframework.stereotype.Component;

/**
 * No-op handler – incoming_payment.created usually requires no action from the ASE.
 */
@Slf4j
@Component
public class IncomingPaymentCreatedHandler implements RafikiEventHandler {

    @Override
    public String supports() {
        return "incoming_payment.created";
    }

    @Override
    public void handle(RafikiWebhookEvent event) {
        log.debug("Received incoming_payment.created id={} – no action required", event.getId());
    }
}
