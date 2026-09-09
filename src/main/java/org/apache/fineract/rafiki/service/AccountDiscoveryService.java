/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.AccountDiscoveryResponse;
import org.apache.fineract.rafiki.data.ClientAccountsDiscoveryResponse;
import org.apache.fineract.rafiki.data.PaymentPointerResolutionResponse;
import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.repository.RafikiWalletAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete discovery component.
 *
 * Allows Rafiki (or any authorised caller) to:
 * <ul>
 *   <li>Resolve a wallet address / payment pointer to a Fineract account</li>
 *   <li>List all mapped accounts that belong to a Fineract client</li>
 *   <li>Obtain SPSP-style resolution data needed for incoming payments</li>
 * </ul>
 *
 * In a full production deployment this service would also call Fineract's
 * ClientReadPlatformService / SavingsAccountReadPlatformService to enrich
 * the response with live balances, account numbers, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDiscoveryService {

    private final RafikiWalletAddressRepository walletAddressRepository;

    /**
     * Discover the Fineract account linked to a Rafiki wallet address id.
     */
    @Transactional(readOnly = true)
    public AccountDiscoveryResponse discoverByRafikiWalletAddressId(String rafikiWalletAddressId) {
        String tenant = TenantContext.getTenantIdentifier();
        RafikiWalletAddress mapping = walletAddressRepository
                .findByTenantIdentifierAndRafikiWalletAddressId(tenant, rafikiWalletAddressId)
                .orElseThrow(() -> new RafikiConnectorException(
                        "No account found for Rafiki walletAddressId: " + rafikiWalletAddressId));

        return toDiscoveryResponse(mapping);
    }

    /**
     * Discover the Fineract account linked to a wallet address string
     * (e.g. "$example.com/alice" or "https://example.com/alice").
     */
    @Transactional(readOnly = true)
    public AccountDiscoveryResponse discoverByWalletAddress(String walletAddress) {
        String tenant = TenantContext.getTenantIdentifier();
        String normalised = normaliseWalletAddress(walletAddress);

        RafikiWalletAddress mapping = walletAddressRepository
                .findByTenantIdentifierAndWalletAddress(tenant, normalised)
                .or(() -> walletAddressRepository.findByTenantIdentifierAndWalletAddress(tenant, walletAddress))
                .orElseThrow(() -> new RafikiConnectorException(
                        "No account found for wallet address: " + walletAddress));

        return toDiscoveryResponse(mapping);
    }

    /**
     * SPSP / payment-pointer resolution.
     * Accepts both "$domain/user" and "https://domain/user" forms.
     */
    @Transactional(readOnly = true)
    public PaymentPointerResolutionResponse resolvePaymentPointer(String paymentPointer) {
        String tenant = TenantContext.getTenantIdentifier();
        String normalised = normaliseWalletAddress(paymentPointer);

        Optional<RafikiWalletAddress> opt = walletAddressRepository
                .findByTenantIdentifierAndWalletAddress(tenant, normalised);

        if (opt.isEmpty()) {
            // also try the raw value in case the stored form differs
            opt = walletAddressRepository.findByTenantIdentifierAndWalletAddress(tenant, paymentPointer);
        }

        RafikiWalletAddress mapping = opt.orElseThrow(() ->
                new RafikiConnectorException("Payment pointer could not be resolved: " + paymentPointer));

        boolean receivable = "ACTIVE".equalsIgnoreCase(mapping.getStatus());

        return PaymentPointerResolutionResponse.builder()
                .paymentPointer(paymentPointer)
                .walletAddress(mapping.getWalletAddress())
                .rafikiWalletAddressId(mapping.getRafikiWalletAddressId())
                .assetCode(mapping.getAssetCode())
                .assetScale(mapping.getAssetScale())
                .receivable(receivable)
                .sharedSecretHint(receivable ? "stream-secret-placeholder" : null)
                .clientId(mapping.getClientId())
                .savingsAccountId(mapping.getSavingsAccountId())
                .build();
    }

    /**
     * List all Rafiki-mapped accounts that belong to a given Fineract client.
     */
    @Transactional(readOnly = true)
    public ClientAccountsDiscoveryResponse discoverByClientId(Long clientId) {
        String tenant = TenantContext.getTenantIdentifier();
        List<RafikiWalletAddress> mappings =
                walletAddressRepository.findByTenantIdentifierAndClientId(tenant, clientId);

        List<AccountDiscoveryResponse> accounts = mappings.stream()
                .map(this::toDiscoveryResponse)
                .collect(Collectors.toList());

        return ClientAccountsDiscoveryResponse.builder()
                .clientId(clientId)
                .tenantIdentifier(tenant)
                .accounts(accounts)
                .totalMappedAccounts(accounts.size())
                .build();
    }

    /**
     * Normalise payment pointers:
     *   $example.com/alice  →  https://example.com/alice
     *   already-https URLs are left unchanged.
     */
    private String normaliseWalletAddress(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.startsWith("$")) {
            return "https://" + trimmed.substring(1);
        }
        return trimmed;
    }

    private AccountDiscoveryResponse toDiscoveryResponse(RafikiWalletAddress m) {
        boolean receivable = "ACTIVE".equalsIgnoreCase(m.getStatus());
        return AccountDiscoveryResponse.builder()
                .walletAddress(m.getWalletAddress())
                .rafikiWalletAddressId(m.getRafikiWalletAddressId())
                .clientId(m.getClientId())
                .savingsAccountId(m.getSavingsAccountId())
                .assetCode(m.getAssetCode())
                .assetScale(m.getAssetScale())
                .status(m.getStatus())
                .clientDisplayName("Client-" + m.getClientId()) // stub – replace with real ClientData
                .receivable(receivable)
                .build();
    }
}
