/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.domain.RafikiEvent;
import org.apache.fineract.rafiki.handler.RafikiEventHandler;
import org.apache.fineract.rafiki.repository.RafikiEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RafikiWebhookServiceTest {

    @Mock
    private RafikiEventRepository eventRepository;

    @Mock
    private RafikiEventHandler completedHandler;

    private RafikiWebhookService service;

    @BeforeEach
    void setUp() {
        when(completedHandler.supports()).thenReturn("incoming_payment.completed");
        service = new RafikiWebhookService(eventRepository, List.of(completedHandler), new ObjectMapper());
        service.init();
        TenantContext.setTenantIdentifier("test-tenant");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void process_newEvent_callsHandlerAndMarksProcessed() {
        when(eventRepository.existsByTenantIdentifierAndEventId(eq("test-tenant"), eq("evt-1")))
                .thenReturn(false);
        when(eventRepository.save(any(RafikiEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setId("evt-1");
        event.setType("incoming_payment.completed");
        event.setData(Map.of("id", "pay-1"));

        service.process(event);

        verify(completedHandler).handle(event);

        ArgumentCaptor<RafikiEvent> captor = ArgumentCaptor.forClass(RafikiEvent.class);
        verify(eventRepository, atLeastOnce()).save(captor.capture());
        RafikiEvent saved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(saved.getProcessed()).isTrue();
        assertThat(saved.getEventId()).isEqualTo("evt-1");
        assertThat(saved.getTenantIdentifier()).isEqualTo("test-tenant");
    }

    @Test
    void process_duplicateEvent_isIgnored() {
        when(eventRepository.existsByTenantIdentifierAndEventId(eq("test-tenant"), eq("evt-dup")))
                .thenReturn(true);

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setId("evt-dup");
        event.setType("incoming_payment.completed");

        service.process(event);

        verify(completedHandler, never()).handle(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void process_unknownType_recordsError() {
        when(eventRepository.existsByTenantIdentifierAndEventId(any(), any())).thenReturn(false);
        when(eventRepository.save(any(RafikiEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setId("evt-unknown");
        event.setType("some.unknown.event");

        service.process(event);

        verify(completedHandler, never()).handle(any());
        ArgumentCaptor<RafikiEvent> captor = ArgumentCaptor.forClass(RafikiEvent.class);
        verify(eventRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getErrorMessage()).contains("No handler");
    }
}
