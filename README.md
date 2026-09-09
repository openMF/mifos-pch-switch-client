# Mifos X - Interledger Rafiki Connector

Multi-tenant Spring Boot 3 / Java 21 connector (plugin-style) that links Apache Mifos X with an Interledger Rafiki instance.

## Features

- Webhook endpoint for all major Rafiki events (`incoming_payment.*`, `outgoing_payment.*`, …)
- Idempotent event processing (`m_rafiki_event`)
- Wallet-address mapping between Mifos X clients/savings accounts and Rafiki wallet addresses
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

## Use as Mifos X plugin

1. Build the jar: `mvn clean package -DskipTests`
2. Drop `target/Mifos X-rafiki-connector-1.0.0-SNAPSHOT.jar` into the `libs/` (or plugins) folder of a Mifos X Spring Boot distribution.
3. Start Mifos X with `-Dloader.path=libs/`
4. The `RafikiConnectorAutoConfiguration` will be picked up automatically.

In a full Mifos X integration you would also:

- Bridge `TenantContext` to `ThreadLocalContextUtil`
- Replace the GraphQL stubs with a real signed client
- Call Mifos X’s `SavingsAccountWritePlatformService` / command source for deposits & holds
- Register additional Liquibase changelogs against the tenant database via Mifos X’s extension points

## Project layout

```
src/main/java/org/apache/Mifos X/rafiki/
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

# Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Apache Mifos X (multi-tenant)            │
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
# Functional Features Implemented – Mifos X ↔ Rafiki Connector

## 1. Multi-tenancy
- Tenant isolation via `X-Tenant-Identifier` header and `TenantContext`
- All tables keyed by `tenant_identifier`
- Per-tenant configuration support (`m_rafiki_tenant_config`)

## 2. Wallet address management
- Create mapping between Mifos X client + savings account ↔ Rafiki wallet address
- Lookup mappings by Rafiki wallet address ID or by wallet address string
- List all mapped wallet addresses for a given Mifos X client
- REST API: `POST /v1/rafiki/wallet-addresses`, `GET /v1/rafiki/wallet-addresses/client/{clientId}`

## 3. Account discovery
- Resolve wallet address → Mifos X account
- Resolve Rafiki internal wallet address ID → Mifos X account
- SPSP-style payment pointer resolution (`$domain/user` and `https://` forms)
- List all Rafiki-mapped accounts belonging to a Mifos X client
- REST API under `/v1/rafiki/discovery/...`

## 4. Webhook handling (Rafiki → Mifos X)
- Receive and process Rafiki webhook events
- Idempotent processing (duplicate event IDs are ignored)
- Signature verification (`Rafiki-Signature` header, HMAC-SHA256)
- Supported event handlers:
  - `incoming_payment.created` (no-op / acknowledgement)
  - `incoming_payment.completed` (withdraw liquidity + credit Mifos X account)
  - `outgoing_payment.created` (deposit liquidity to Rafiki)
- REST API: `POST /v1/rafiki/webhooks`

## 5. Liquidity orchestration
- Withdraw incoming payment liquidity from Rafiki (stub GraphQL call)
- Deposit outgoing payment liquidity to Rafiki (stub GraphQL call)
- Record liquidity movements in `m_rafiki_liquidity_tx`
- Stub credit of Mifos X savings account (ready for real Mifos X service integration)

## 6. Persistence / schema
- Liquibase-managed tenant tables:
  - `m_rafiki_tenant_config`
  - `m_rafiki_wallet_address`
  - `m_rafiki_event` (idempotency + audit)
  - `m_rafiki_liquidity_tx`
- JPA entities + Spring Data repositories for all of the above

## 7. Integration stubs (ready for production wiring)
- Rafiki Admin GraphQL client (create wallet address, withdraw/deposit liquidity)
- Auto-configuration so the module can be loaded as a Mifos X plugin
- Global exception handling for connector and signature errors

## 8. Testing
- Unit tests for webhook service (idempotency, handler dispatch, unknown events)
- Unit tests for incoming payment completed handler
- Unit tests for account discovery service (all resolution paths)
- WebMvc tests for webhook and discovery controllers

## 9. Operational / packaging
- Standalone Spring Boot application (port 8089) for development
- Plugin-ready packaging (`AutoConfiguration.imports`)
- Maven build (Java 21, Spring Boot 3.3)
- README with usage examples for webhooks, wallet mapping and discovery

---

## Not yet implemented (explicit extension points)

- Real signed GraphQL calls to a live Rafiki instance
- Full Open Payments / GNAP grant flows
- Direct calls into Mifos X’s `SavingsAccountWritePlatformService` / command bus
- Additional webhook handlers (`incoming_payment.expired`, `outgoing_payment.completed/failed`, `peer.liquidity_low`, etc.)
- Live balance / account-number enrichment from Mifos X core services
- Production SPSP shared-secret generation

## Licence

Mozilla License

```
 Copyright since 2026 Mifos Initiative
 
 This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
```
