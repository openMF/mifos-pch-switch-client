package org.apache.fineract.rafiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rafiki.connector")
public class RafikiConnectorProperties {

    private boolean enabled = true;
    private String defaultAdminUrl = "http://localhost:3001/graphql";
    private String defaultWebhookSecret = "change-me-in-production";
    private String signatureVersion = "1";
    private String defaultAssetCode = "USD";
    private short defaultAssetScale = 2;
}
