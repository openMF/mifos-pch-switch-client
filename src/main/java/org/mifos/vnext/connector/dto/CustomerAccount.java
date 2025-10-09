package org.mifos.vnext.connector.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccount {
    private String sourceFspId;
    private String destinationFspId;
    private String accountId;
    private String accountNickname;
    private String currency;
}
