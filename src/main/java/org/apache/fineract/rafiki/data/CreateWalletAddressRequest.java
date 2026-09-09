package org.apache.fineract.rafiki.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWalletAddressRequest {

    @NotNull
    private Long clientId;

    private Long savingsAccountId;

    @NotBlank
    private String walletAddress;

    private String assetCode = "USD";

    private Short assetScale = 2;
}
