/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.api;

import org.apache.fineract.rafiki.data.AccountDiscoveryResponse;
import org.apache.fineract.rafiki.data.PaymentPointerResolutionResponse;
import org.apache.fineract.rafiki.service.AccountDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountDiscoveryController.class)
class AccountDiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountDiscoveryService discoveryService;

    @Test
    void discoverByWalletAddress_returns200() throws Exception {
        AccountDiscoveryResponse response = AccountDiscoveryResponse.builder()
                .walletAddress("https://example.com/alice")
                .rafikiWalletAddressId("wa_abc123")
                .clientId(42L)
                .savingsAccountId(100L)
                .assetCode("USD")
                .assetScale((short) 2)
                .status("ACTIVE")
                .receivable(true)
                .build();

        when(discoveryService.discoverByWalletAddress(anyString())).thenReturn(response);

        mockMvc.perform(get("/v1/rafiki/discovery/wallet-addresses/https://example.com/alice")
                        .header("X-Tenant-Identifier", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rafikiWalletAddressId").value("wa_abc123"))
                .andExpect(jsonPath("$.clientId").value(42))
                .andExpect(jsonPath("$.receivable").value(true));
    }

    @Test
    void resolvePaymentPointer_returns200() throws Exception {
        PaymentPointerResolutionResponse response = PaymentPointerResolutionResponse.builder()
                .paymentPointer("$example.com/alice")
                .walletAddress("https://example.com/alice")
                .rafikiWalletAddressId("wa_abc123")
                .assetCode("USD")
                .assetScale((short) 2)
                .receivable(true)
                .clientId(42L)
                .savingsAccountId(100L)
                .build();

        when(discoveryService.resolvePaymentPointer(anyString())).thenReturn(response);

        mockMvc.perform(get("/v1/rafiki/discovery/payment-pointers/$example.com/alice")
                        .header("X-Tenant-Identifier", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentPointer").value("$example.com/alice"))
                .andExpect(jsonPath("$.receivable").value(true));
    }
}
