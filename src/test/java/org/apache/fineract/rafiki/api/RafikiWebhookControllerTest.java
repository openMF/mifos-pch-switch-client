/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.service.RafikiWebhookService;
import org.apache.fineract.rafiki.service.WebhookSignatureVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RafikiWebhookController.class)
class RafikiWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RafikiWebhookService webhookService;

    @MockBean
    private WebhookSignatureVerifier signatureVerifier;

    @Test
    void receive_validEvent_returns200() throws Exception {
        doNothing().when(signatureVerifier).verify(any(), any());
        doNothing().when(webhookService).process(any());

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setId("evt-ctrl-1");
        event.setType("incoming_payment.created");
        event.setData(Map.of("id", "pay-1"));

        mockMvc.perform(post("/v1/rafiki/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Tenant-Identifier", "default")
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }
}
