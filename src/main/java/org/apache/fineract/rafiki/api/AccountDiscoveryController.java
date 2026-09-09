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
import org.apache.fineract.rafiki.data.AccountDiscoveryResponse;
import org.apache.fineract.rafiki.data.ClientAccountsDiscoveryResponse;
import org.apache.fineract.rafiki.data.PaymentPointerResolutionResponse;
import org.apache.fineract.rafiki.service.AccountDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Concrete discovery API that Rafiki (or an operator / Open Payments client)
 * can call to resolve wallet addresses and payment pointers to Fineract accounts.
 *
 * Endpoints
 * ----------
 * GET /v1/rafiki/discovery/wallet-addresses/{walletAddress}
 * GET /v1/rafiki/discovery/rafiki-ids/{rafikiWalletAddressId}
 * GET /v1/rafiki/discovery/payment-pointers/{paymentPointer}
 * GET /v1/rafiki/discovery/clients/{clientId}/accounts
 */
@Slf4j
@RestController
@RequestMapping("/v1/rafiki/discovery")
@RequiredArgsConstructor
public class AccountDiscoveryController {

    private final AccountDiscoveryService discoveryService;

    /**
     * Resolve a wallet address string (supports both $pointer and https:// forms).
     */
    @GetMapping("/wallet-addresses/{walletAddress}")
    public ResponseEntity<AccountDiscoveryResponse> discoverByWalletAddress(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @PathVariable String walletAddress) {

        return withTenant(tenantHeader, () ->
                ResponseEntity.ok(discoveryService.discoverByWalletAddress(walletAddress)));
    }

    /**
     * Resolve by the internal Rafiki wallet address identifier.
     */
    @GetMapping("/rafiki-ids/{rafikiWalletAddressId}")
    public ResponseEntity<AccountDiscoveryResponse> discoverByRafikiId(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @PathVariable String rafikiWalletAddressId) {

        return withTenant(tenantHeader, () ->
                ResponseEntity.ok(discoveryService.discoverByRafikiWalletAddressId(rafikiWalletAddressId)));
    }

    /**
     * SPSP-style payment pointer resolution.
     * Example: GET /v1/rafiki/discovery/payment-pointers/$example.com/alice
     * (URL-encode the $ if necessary: %24example.com%2Falice)
     */
    @GetMapping("/payment-pointers/{paymentPointer}")
    public ResponseEntity<PaymentPointerResolutionResponse> resolvePaymentPointer(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @PathVariable String paymentPointer) {

        return withTenant(tenantHeader, () ->
                ResponseEntity.ok(discoveryService.resolvePaymentPointer(paymentPointer)));
    }

    /**
     * List every Rafiki-mapped account that belongs to a Fineract client.
     */
    @GetMapping("/clients/{clientId}/accounts")
    public ResponseEntity<ClientAccountsDiscoveryResponse> discoverByClient(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @PathVariable Long clientId) {

        return withTenant(tenantHeader, () ->
                ResponseEntity.ok(discoveryService.discoverByClientId(clientId)));
    }

    private <T> T withTenant(String tenantHeader, java.util.function.Supplier<T> action) {
        String tenant = (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "default";
        TenantContext.setTenantIdentifier(tenant);
        try {
            return action.get();
        } finally {
            TenantContext.clear();
        }
    }
}
