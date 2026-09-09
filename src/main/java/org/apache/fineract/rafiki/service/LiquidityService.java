package org.apache.fineract.rafiki.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.domain.RafikiLiquidityTx;
import org.apache.fineract.rafiki.graphql.RafikiGraphQLClient;
import org.apache.fineract.rafiki.repository.RafikiLiquidityTxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiquidityService {

    private final RafikiLiquidityTxRepository liquidityTxRepository;
    private final RafikiGraphQLClient graphQLClient;

    /**
     * Calls Rafiki createIncomingPaymentWithdrawal and records the liquidity movement.
     */
    @Transactional
    public void withdrawIncomingPayment(String paymentId, BigDecimal amount, String assetCode) {
        String tenant = TenantContext.getTenantIdentifier();

        graphQLClient.createIncomingPaymentWithdrawal(paymentId);

        RafikiLiquidityTx tx = RafikiLiquidityTx.builder()
                .tenantIdentifier(tenant)
                .rafikiPaymentId(paymentId)
                .direction("INCOMING")
                .amount(amount)
                .assetCode(assetCode)
                .status("WITHDRAWN")
                .build();
        liquidityTxRepository.save(tx);

        log.info("Withdrawn incoming payment {} amount {} for tenant {}", paymentId, amount, tenant);
    }

    /**
     * Credits a Fineract savings account (stub – in real integration call Fineract deposit API / service).
     */
    @Transactional
    public Long creditFineractAccount(Long savingsAccountId, BigDecimal amount, String externalId) {
        // TODO: Integrate with Fineract SavingsAccountWritePlatformService or command bus
        log.info("STUB: Credit Fineract savings account {} with {} (externalId={})",
                savingsAccountId, amount, externalId);
        return System.currentTimeMillis(); // fake transaction id
    }

    /**
     * Holds funds and deposits liquidity to Rafiki for an outgoing payment.
     */
    @Transactional
    public void depositOutgoingPaymentLiquidity(String paymentId, BigDecimal amount, String assetCode) {
        String tenant = TenantContext.getTenantIdentifier();

        // TODO: Hold funds on Fineract side first
        graphQLClient.depositOutgoingPaymentLiquidity(paymentId);

        RafikiLiquidityTx tx = RafikiLiquidityTx.builder()
                .tenantIdentifier(tenant)
                .rafikiPaymentId(paymentId)
                .direction("OUTGOING")
                .amount(amount)
                .assetCode(assetCode)
                .status("DEPOSITED")
                .build();
        liquidityTxRepository.save(tx);

        log.info("Deposited outgoing payment liquidity {} amount {} for tenant {}", paymentId, amount, tenant);
    }
}
