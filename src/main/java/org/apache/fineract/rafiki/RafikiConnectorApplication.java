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
