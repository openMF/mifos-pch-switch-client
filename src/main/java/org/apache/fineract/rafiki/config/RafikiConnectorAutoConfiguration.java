package org.apache.fineract.rafiki.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "org.apache.fineract.rafiki")
@EnableJpaRepositories(basePackages = "org.apache.fineract.rafiki.repository")
@EnableConfigurationProperties(RafikiConnectorProperties.class)
public class RafikiConnectorAutoConfiguration {
}
