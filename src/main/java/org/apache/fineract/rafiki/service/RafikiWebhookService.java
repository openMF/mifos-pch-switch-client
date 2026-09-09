package org.apache.fineract.rafiki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.domain.RafikiEvent;
import org.apache.fineract.rafiki.handler.RafikiEventHandler;
import org.apache.fineract.rafiki.repository.RafikiEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RafikiWebhookService {

    private final RafikiEventRepository eventRepository;
    private final List<RafikiEventHandler> handlers;
    private final ObjectMapper objectMapper;

    private Map<String, RafikiEventHandler> handlerMap;

    @PostConstruct
    void init() {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(RafikiEventHandler::supports, Function.identity(), (a, b) -> a));
        log.info("Registered Rafiki event handlers: {}", handlerMap.keySet());
    }

    @Transactional
    public void process(RafikiWebhookEvent event) {
        String tenant = TenantContext.getTenantIdentifier();

        if (eventRepository.existsByTenantIdentifierAndEventId(tenant, event.getId())) {
            log.info("Duplicate Rafiki event {} for tenant {} – ignored", event.getId(), tenant);
            return;
        }

        RafikiEvent entity = RafikiEvent.builder()
                .tenantIdentifier(tenant)
                .eventId(event.getId())
                .eventType(event.getType())
                .payload(serialize(event))
                .processed(false)
                .build();
        eventRepository.save(entity);

        RafikiEventHandler handler = handlerMap.get(event.getType());
        if (handler == null) {
            log.warn("No handler registered for Rafiki event type: {}", event.getType());
            entity.setErrorMessage("No handler for type " + event.getType());
            eventRepository.save(entity);
            return;
        }

        try {
            handler.handle(event);
            entity.setProcessed(true);
            entity.setProcessedAt(OffsetDateTime.now());
        } catch (Exception ex) {
            log.error("Failed processing Rafiki event {} type {}", event.getId(), event.getType(), ex);
            entity.setErrorMessage(ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(1000, ex.getMessage().length())) : "Unknown error");
            eventRepository.save(entity);
            throw ex; // allow Rafiki to retry
        } finally {
            eventRepository.save(entity);
        }
    }

    private String serialize(RafikiWebhookEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "{}";
        }
    }
}
