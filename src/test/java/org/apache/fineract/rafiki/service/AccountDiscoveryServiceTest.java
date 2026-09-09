/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.service;

import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.AccountDiscoveryResponse;
import org.apache.fineract.rafiki.data.ClientAccountsDiscoveryResponse;
import org.apache.fineract.rafiki.data.PaymentPointerResolutionResponse;
import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.repository.RafikiWalletAddressRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDiscoveryServiceTest {

    @Mock
    private RafikiWalletAddressRepository repository;

    @InjectMocks
    private AccountDiscoveryService service;

    private RafikiWalletAddress sampleMapping;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantIdentifier("test-tenant");
        sampleMapping = RafikiWalletAddress.builder()
                .id(1L)
                .tenantIdentifier("test-tenant")
                .clientId(42L)
                .savingsAccountId(100L)
                .walletAddress("https://example.com/alice")
                .rafikiWalletAddressId("wa_abc123")
                .assetCode("USD")
                .assetScale((short) 2)
                .status("ACTIVE")
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void discoverByRafikiWalletAddressId_found() {
        when(repository.findByTenantIdentifierAndRafikiWalletAddressId("test-tenant", "wa_abc123"))
                .thenReturn(Optional.of(sampleMapping));

        AccountDiscoveryResponse result = service.discoverByRafikiWalletAddressId("wa_abc123");

        assertThat(result.getClientId()).isEqualTo(42L);
        assertThat(result.getSavingsAccountId()).isEqualTo(100L);
        assertThat(result.getWalletAddress()).isEqualTo("https://example.com/alice");
        assertThat(result.isReceivable()).isTrue();
    }

    @Test
    void discoverByRafikiWalletAddressId_notFound_throws() {
        when(repository.findByTenantIdentifierAndRafikiWalletAddressId("test-tenant", "unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.discoverByRafikiWalletAddressId("unknown"))
                .isInstanceOf(RafikiConnectorException.class)
                .hasMessageContaining("No account found");
    }

    @Test
    void discoverByWalletAddress_normalisesPaymentPointer() {
        when(repository.findByTenantIdentifierAndWalletAddress("test-tenant", "https://example.com/alice"))
                .thenReturn(Optional.of(sampleMapping));

        AccountDiscoveryResponse result = service.discoverByWalletAddress("$example.com/alice");

        assertThat(result.getRafikiWalletAddressId()).isEqualTo("wa_abc123");
        assertThat(result.getClientId()).isEqualTo(42L);
    }

    @Test
    void resolvePaymentPointer_success() {
        when(repository.findByTenantIdentifierAndWalletAddress("test-tenant", "https://example.com/alice"))
                .thenReturn(Optional.of(sampleMapping));

        PaymentPointerResolutionResponse result = service.resolvePaymentPointer("$example.com/alice");

        assertThat(result.getPaymentPointer()).isEqualTo("$example.com/alice");
        assertThat(result.getWalletAddress()).isEqualTo("https://example.com/alice");
        assertThat(result.getRafikiWalletAddressId()).isEqualTo("wa_abc123");
        assertThat(result.isReceivable()).isTrue();
        assertThat(result.getSharedSecretHint()).isNotBlank();
        assertThat(result.getClientId()).isEqualTo(42L);
        assertThat(result.getSavingsAccountId()).isEqualTo(100L);
    }

    @Test
    void resolvePaymentPointer_inactive_notReceivable() {
        sampleMapping.setStatus("INACTIVE");
        when(repository.findByTenantIdentifierAndWalletAddress("test-tenant", "https://example.com/alice"))
                .thenReturn(Optional.of(sampleMapping));

        PaymentPointerResolutionResponse result = service.resolvePaymentPointer("$example.com/alice");

        assertThat(result.isReceivable()).isFalse();
        assertThat(result.getSharedSecretHint()).isNull();
    }

    @Test
    void discoverByClientId_returnsAllMappings() {
        when(repository.findByTenantIdentifierAndClientId("test-tenant", 42L))
                .thenReturn(List.of(sampleMapping));

        ClientAccountsDiscoveryResponse result = service.discoverByClientId(42L);

        assertThat(result.getClientId()).isEqualTo(42L);
        assertThat(result.getTotalMappedAccounts()).isEqualTo(1);
        assertThat(result.getAccounts()).hasSize(1);
        assertThat(result.getAccounts().get(0).getRafikiWalletAddressId()).isEqualTo("wa_abc123");
    }
}
