package org.apache.fineract.rafiki.graphql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.RafikiConnectorProperties;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.domain.RafikiTenantConfig;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.repository.RafikiTenantConfigRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub GraphQL client for Rafiki Admin API.
 * In production replace the body of each method with real HTTP GraphQL calls
 * (including request signing as required by Rafiki).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RafikiGraphQLClient {

    private final RafikiTenantConfigRepository tenantConfigRepository;
    private final RafikiConnectorProperties properties;

    public String createWalletAddress(String walletAddress, String assetCode, Short assetScale) {
        log.info("STUB GraphQL: createWalletAddress address={} asset={}/{}", walletAddress, assetCode, assetScale);
        // Real implementation would execute a mutation against the tenant-specific admin URL
        return "wa_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public void createIncomingPaymentWithdrawal(String paymentId) {
        log.info("STUB GraphQL: createIncomingPaymentWithdrawal paymentId={}", paymentId);
        // mutation CreateIncomingPaymentWithdrawal ...
    }

    public void depositOutgoingPaymentLiquidity(String paymentId) {
        log.info("STUB GraphQL: depositOutgoingPaymentLiquidity paymentId={}", paymentId);
        // mutation DepositOutgoingPaymentLiquidity ...
    }

    public String resolveAdminUrl() {
        String tenant = TenantContext.getTenantIdentifier();
        return tenantConfigRepository.findByTenantIdentifier(tenant)
                .map(RafikiTenantConfig::getRafikiAdminUrl)
                .orElse(properties.getDefaultAdminUrl());
    }
}
