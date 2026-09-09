/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone Spring Boot entry point for development and testing of the
 * Fineract ↔ Rafiki connector.
 *
 * In production this module is intended to be loaded as a Fineract plugin
 * via -Dloader.path or as a custom module under custom/interledger/rafiki/connector.
 */
@SpringBootApplication
public class RafikiConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RafikiConnectorApplication.class, args);
    }
}
