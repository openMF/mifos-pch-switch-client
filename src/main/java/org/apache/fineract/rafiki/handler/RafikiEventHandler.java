/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.handler;

import org.apache.fineract.rafiki.data.RafikiWebhookEvent;

public interface RafikiEventHandler {

    /**
     * @return the exact event type this handler supports (e.g. "incoming_payment.completed")
     */
    String supports();

    void handle(RafikiWebhookEvent event);
}
