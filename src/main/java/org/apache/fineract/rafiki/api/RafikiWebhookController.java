/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.service.RafikiWebhookService;
import org.apache.fineract.rafiki.service.WebhookSignatureVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook endpoint that Rafiki calls.
 * Must return HTTP 200 for successful processing so Rafiki does not retry.
 */
@Slf4j
@RestController
@RequestMapping("/v1/rafiki/webhooks")
@RequiredArgsConstructor
public class RafikiWebhookController {

    private final RafikiWebhookService webhookService;
    private final WebhookSignatureVerifier signatureVerifier;

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "Rafiki-Signature", required = false) String signature,
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @RequestBody RafikiWebhookEvent event) {

        String tenant = (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "default";
        TenantContext.setTenantIdentifier(tenant);
        try {
            log.info("Received Rafiki webhook type={} id={} tenant={}", event.getType(), event.getId(), tenant);
            signatureVerifier.verify(signature, event);
            webhookService.process(event);
            return ResponseEntity.ok().build();
        } finally {
            TenantContext.clear();
        }
    }
}
