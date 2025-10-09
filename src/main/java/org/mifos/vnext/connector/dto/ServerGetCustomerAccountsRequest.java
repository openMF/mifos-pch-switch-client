package org.mifos.vnext.connector.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerGetCustomerAccountsRequest {
    private String sourceFspId;
    private String destinationFspId;
    private String partyId;
}
