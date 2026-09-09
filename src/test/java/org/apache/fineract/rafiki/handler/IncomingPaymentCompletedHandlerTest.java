package org.apache.fineract.rafiki.handler;

import org.apache.fineract.rafiki.data.RafikiWebhookEvent;
import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.service.LiquidityService;
import org.apache.fineract.rafiki.service.WalletAddressMappingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomingPaymentCompletedHandlerTest {

    @Mock
    private LiquidityService liquidityService;

    @Mock
    private WalletAddressMappingService mappingService;

    @InjectMocks
    private IncomingPaymentCompletedHandler handler;

    @Test
    void handle_happyPath_withdrawsAndCredits() {
        RafikiWalletAddress mapping = RafikiWalletAddress.builder()
                .id(1L)
                .rafikiWalletAddressId("wa-123")
                .savingsAccountId(99L)
                .assetCode("USD")
                .build();

        when(mappingService.findByRafikiWalletAddressId("wa-123")).thenReturn(Optional.of(mapping));
        when(liquidityService.creditFineractAccount(eq(99L), any(), eq("pay-1"))).thenReturn(1001L);

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setId("evt-1");
        event.setType("incoming_payment.completed");
        event.setData(Map.of(
                "id", "pay-1",
                "walletAddressId", "wa-123",
                "receivedAmount", "25.50"
        ));

        handler.handle(event);

        verify(liquidityService).withdrawIncomingPayment(eq("pay-1"), eq(new BigDecimal("25.50")), eq("USD"));
        verify(liquidityService).creditFineractAccount(eq(99L), eq(new BigDecimal("25.50")), eq("pay-1"));
    }

    @Test
    void handle_unknownWallet_throws() {
        when(mappingService.findByRafikiWalletAddressId("unknown")).thenReturn(Optional.empty());

        RafikiWebhookEvent event = new RafikiWebhookEvent();
        event.setData(Map.of("id", "pay-1", "walletAddressId", "unknown", "receivedAmount", "10"));

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(RafikiConnectorException.class)
                .hasMessageContaining("No Fineract mapping");
    }

    @Test
    void supports_returnsCorrectType() {
        org.assertj.core.api.Assertions.assertThat(handler.supports())
                .isEqualTo("incoming_payment.completed");
    }
}
