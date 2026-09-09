/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
