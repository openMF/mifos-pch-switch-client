package org.apache.fineract.rafiki.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletAddressResponse {

    private Long id;
    private Long clientId;
    private Long savingsAccountId;
    private String walletAddress;
    private String rafikiWalletAddressId;
    private String assetCode;
    private Short assetScale;
    private String status;
}
