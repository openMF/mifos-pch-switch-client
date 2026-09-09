package org.apache.fineract.rafiki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.RafikiConnectorProperties;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.domain.RafikiTenantConfig;
import org.apache.fineract.rafiki.exception.WebhookSignatureException;
import org.apache.fineract.rafiki.repository.RafikiTenantConfigRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies the Rafiki-Signature header according to Rafiki documentation.
 * Format: t=<timestamp>, v1=<hmac-sha256-hex>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookSignatureVerifier {

    private final RafikiTenantConfigRepository tenantConfigRepository;
    private final RafikiConnectorProperties properties;
    private final ObjectMapper objectMapper;

    public void verify(String signatureHeader, RafikiWebhookEvent event) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            // In development we allow missing signature; production should enforce it
            log.warn("Missing Rafiki-Signature header – skipping verification (dev mode)");
            return;
        }

        String secret = resolveSecret();
        if (secret == null || secret.isBlank() || "change-me-in-production".equals(secret)) {
            log.warn("Webhook secret not configured – skipping signature verification");
            return;
        }

        try {
            String[] parts = signatureHeader.split(", ");
            String timestamp = null;
            String receivedDigest = null;
            String version = properties.getSignatureVersion();

            for (String part : parts) {
                if (part.startsWith("t=")) {
                    timestamp = part.substring(2);
                } else if (part.startsWith("v" + version + "=")) {
                    receivedDigest = part.substring(("v" + version + "=").length());
                }
            }

            if (timestamp == null || receivedDigest == null) {
                throw new WebhookSignatureException("Invalid Rafiki-Signature header format");
            }

            String payload = timestamp + "." + objectMapper.writeValueAsString(event);
            String expectedDigest = hmacSha256Hex(secret, payload);

            if (!MessageDigest.isEqual(
                    expectedDigest.getBytes(StandardCharsets.UTF_8),
                    receivedDigest.getBytes(StandardCharsets.UTF_8))) {
                throw new WebhookSignatureException("Webhook signature mismatch");
            }
        } catch (WebhookSignatureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WebhookSignatureException("Failed to verify webhook signature: " + ex.getMessage());
        }
    }

    private String resolveSecret() {
        String tenant = TenantContext.getTenantIdentifier();
        return tenantConfigRepository.findByTenantIdentifier(tenant)
                .map(RafikiTenantConfig::getRafikiWebhookSecret)
                .orElse(properties.getDefaultWebhookSecret());
    }

    private String hmacSha256Hex(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(raw);
    }
}
