# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tutor-Link (家教直连平台) — a full-stack tutoring platform connecting parents with university student tutors, bypassing intermediary fees. Backend is Spring Boot 3.2.5 / Java 17, frontend is a WeChat Mini Program built with uni-app (Vue 3 + Vite).

## Build & Run Commands

```bash
# Build all modules (skip tests — none exist yet)
mvn clean package -DskipTests

# Build only the deployable web module
mvn clean package -pl tutor-link-web -am -DskipTests

# Run locally (requires MySQL, Redis, Kafka — use docker-compose for infra)
mvn spring-boot:run -pl tutor-link-web

# Start infrastructure only (no app)
docker-compose up -d mysql redis kafka

# Full stack with Docker
docker-compose up --build

# Frontend (miniapp)
cd tutor-link-miniapp
npm install
npm run dev:mp-weixin   # WeChat dev server
npm run dev:h5           # H5 dev server
npm run build:mp-weixin  # Production WeChat build
npm run build:h5         # Production H5 build
```

## Architecture

Six Maven modules in a strict layered dependency chain:

```
common → model → dao ─┬→ service → web  (deployable Spring Boot app)
                       └→ mq ──────────┘
```

- **common** — Constants (`ResultCode`, `OrderStatus`, `UserRole` bitmask), exceptions, `ApiResult<T>` response envelope, utilities (`JwtUtil`, `SnowflakeIdUtil`, `RedisKeyUtil`)
- **model** — JPA/MyBatis-Plus entities, DTOs, enums. All entities extend `BaseEntity` with snowflake IDs and logical delete
- **dao** — MyBatis-Plus mapper interfaces. Config in `MyBatisPlusConfig`
- **mq** — Kafka topic definitions (4 topics: `order-events`, `order-delay`, `notification-events`, `outbox-retry`), producers, message DTOs
- **service** — Business logic organized by domain (user, order, payment, review, chat, search, notification, admin)
- **web** — Controllers, Spring Security config, JWT filter, WebSocket handlers, Flyway migrations. Entry point: `TutorLinkApplication`

The miniapp (`tutor-link-miniapp/`) is a separate uni-app project, not a Maven module.

## Key Patterns

**Transactional Outbox** — `OutboxMessage` entity + `OutboxRelayScheduler` (polls every 1s) ensures reliable Kafka publishing. Write outbox records in the same DB transaction as business data.

**Order State Machine** — `OrderStateMachine` manages 9 states defined in `OrderStatus`. Order expiration uses Redis ZSET (`OrderExpireScheduler`) with 30-minute auto-cancel.

**Authentication** — JWT stateless auth via `JwtAuthenticationFilter`. Access tokens expire in 2h, refresh tokens in 30d. Roles are bitmask-based: PARENT=1, TUTOR=2, ADMIN=4.

**API Response** — All endpoints return `ApiResult<T>` envelope with code/message/data. Error codes defined in `ResultCode`.

**ID Generation** — Snowflake IDs for all entities (configured in MyBatis-Plus via `IdentifierGenerator`).

## Configuration

- Main config: `tutor-link-web/src/main/resources/application.yml`
- Production overrides: `application-prod.yml` (profile: `prod`)
- DB migrations: `tutor-link-web/src/main/resources/db/migration/V1-V7__*.sql`
- Nginx: `nginx/conf.d/default.conf` — `/api/` → app, `/ws/` → WebSocket, `/` → static files

**Required environment variables** (for WeChat/OSS features):
`WX_APPID`, `WX_SECRET`, `WX_MCH_ID`, `WX_PAY_API_KEY`, `WX_PAY_CERT_PATH`, `WX_PAY_NOTIFY_URL`, `OSS_ENDPOINT`, `OSS_ACCESS_KEY_ID`, `OSS_ACCESS_KEY_SECRET`, `OSS_BUCKET_NAME`

## Tech Stack Versions

Spring Boot 3.2.5, MyBatis-Plus 3.5.6, Knife4j 4.4.0, JJWT 0.12.5, Hutool 5.8.26, Spring Kafka 3.1.4, MySQL 8, Redis 7, Kafka 3.7 (KRaft mode, no ZooKeeper)

## Conventions

- Package root: `com.tutorlink`
- All tables: InnoDB, utf8mb4, BIGINT snowflake PKs, `is_deleted` logical delete, created/updated audit columns
- API docs auto-generated at `/doc.html` (Knife4j) when app is running