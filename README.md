# Apache Fineract ↔ Interledger Rafiki Connector

Multi-tenant Spring Boot 3 / Java 21 connector (plugin-style) that links Apache Fineract with an Interledger Rafiki instance.

## Features

- Webhook endpoint for all major Rafiki events (`incoming_payment.*`, `outgoing_payment.*`, …)
- Idempotent event processing (`m_rafiki_event`)
- Wallet-address mapping between Fineract clients/savings accounts and Rafiki wallet addresses
- Liquidity withdrawal / deposit orchestration (stubs ready for real GraphQL Admin API)
- Liquibase schema for tenant databases
- Signature verification for Rafiki webhooks
- Unit & WebMvc tests

## Quick start (standalone for development)

```bash
mvn clean test
mvn spring-boot:run
```

Webhook endpoint: `POST http://localhost:8089/v1/rafiki/webhooks`  
Header: `X-Tenant-Identifier: default` (optional)  
Header: `Rafiki-Signature: t=..., v1=...` (optional in dev)

Create wallet mapping:

```bash
curl -X POST http://localhost:8089/v1/rafiki/wallet-addresses \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Identifier: default" \
  -d '{"clientId":1,"savingsAccountId":10,"walletAddress":"$example.com/alice","assetCode":"USD","assetScale":2}'
```

## Use as Fineract plugin

1. Build the jar: `mvn clean package -DskipTests`
2. Drop `target/fineract-rafiki-connector-1.0.0-SNAPSHOT.jar` into the `libs/` (or plugins) folder of a Fineract Spring Boot distribution.
3. Start Fineract with `-Dloader.path=libs/`
4. The `RafikiConnectorAutoConfiguration` will be picked up automatically.

In a full Fineract integration you would also:

- Bridge `TenantContext` to `ThreadLocalContextUtil`
- Replace the GraphQL stubs with a real signed client
- Call Fineract’s `SavingsAccountWritePlatformService` / command source for deposits & holds
- Register additional Liquibase changelogs against the tenant database via Fineract’s extension points

## Project layout

```
src/main/java/org/apache/fineract/rafiki/
  api/          – REST controllers
  config/       – Auto-configuration & properties
  data/         – DTOs
  domain/       – JPA entities
  handler/      – Webhook event handlers
  graphql/      – Rafiki Admin API client (stub)
  repository/   – Spring Data repositories
  service/      – Business logic
src/main/resources/db/changelog/ – Liquibase
src/test/       – Unit & controller tests
```

# Apache Fineract ↔ Interledger Rafiki Connector – Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Apache Fineract (multi-tenant)            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  mifos-rafiki-connector (plugin / custom module)    │  │
│  │                                                        │  │
│  │  API Layer (JAX-RS)                                    │  │
│  │    • RafikiWebhookApiResource                          │  │
│  │    • RafikiAdminApiResource (wallet, liquidity, peer)  │  │
│  │                                                        │  │
│  │  Service Layer                                         │  │
│  │    • RafikiWebhookService (idempotent event handling)  │  │
│  │    • RafikiGraphQLClient (Admin API)                   │  │
│  │    • WalletAddressMappingService                       │  │
│  │    • LiquidityService (deposit / withdraw)             │  │
│  │    • Incoming/OutgoingPaymentService                   │  │
│  │                                                        │  │
│  │  Domain / DAO                                          │  │
│  │    • m_rafiki_wallet_address                           │  │
│  │    • m_rafiki_event (idempotency)                      │  │
│  │    • m_rafiki_liquidity_tx                             │  │
│  │    • m_rafiki_tenant_config                            │  │
│  │                                                        │  │
│  │  Liquibase (tenant DB) + Spring AutoConfiguration      │  │
│  └────────────────────────────────────────────────────────┘  │
│                              │                               │
│                              │ HTTPS (signed)                │
└──────────────────────────────┼───────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                 Interledger Rafiki (main)                    │
│  • Backend Admin GraphQL API                                 │
│  • Webhooks (incoming_payment.*, outgoing_payment.*, ...)    │
│  • Open Payments / ILP connector                             │
└──────────────────────────────────────────────────────────────┘
```

## Licence

Mozilla License
