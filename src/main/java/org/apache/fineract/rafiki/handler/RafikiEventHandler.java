package org.apache.fineract.rafiki.handler;

import org.apache.fineract.rafiki.data.RafikiWebhookEvent;

public interface RafikiEventHandler {

    /**
     * @return the exact event type this handler supports (e.g. "incoming_payment.completed")
     */
    String supports();

    void handle(RafikiWebhookEvent event);
}
