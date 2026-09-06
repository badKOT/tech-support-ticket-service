# AGENTS.md — tech-support-ticket-service

## Quickstart & Commands
```bash
# Full docker stack (Postgres, Keycloak, Backend, Frontend/Nginx, DB seed)
docker compose up -d --build

# Backend tests (H2 in-memory, no external deps needed)
./gradlew test                                                                # all tests
./gradlew test --tests authorization.TicketSecurityIntegrationTest             # single class
./gradlew test --tests "authorization.TicketSecurityIntegrationTest.requesterShouldSeeOwnTicket" # single method

# Backend local run (requires JWT_SECRET from .env, min 32 bytes Base64)
export JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
./gradlew bootRun

# Seed test data (idempotent, skips if users exist)
curl http://localhost:8080/api/init-db

# Frontend local dev (Vite SPA on :5173, proxies /api and /oauth2 to :8080)
cd frontend && npm install && npm run dev
cd frontend && npm run build
```

## Stack
- **Java 21**, **Spring Boot 3.5.16**, **Gradle** (Kotlin DSL)
- **Database**: PostgreSQL 17 (Docker/prod), H2 in-memory (test profile)
- **Frontend**: React 19, Vite, React Router 7 under `frontend/`
- **Auth**: Spring Security, JWT (HMAC-SHA256), OAuth2/OIDC (Keycloak 26.7), BCrypt + Pepper + Salt

## Architecture & Conventions
- Single-module backend under package `self.project.web.ticket.service`
- Services default to `@Transactional(readOnly = true)` at class level; write operations override with `@Transactional`
- DTOs are Java `record`s with static `from(Entity)` factories
- Controllers use `@Slf4j` and log `[HTTP_METHOD /path]` prefixes
- Entity IDs use `GenerationType.IDENTITY`
- Schema management: `hibernate.ddl-auto: update` — **no Flyway/Liquibase migration scripts exist**

## Environment & Testing Quirks
- `JWT_SECRET` is **mandatory** for `bootRun` and non-test profiles. Must decode to ≥256 bits (32 bytes).
- Spring integration tests use `@ActiveProfiles("test")` with built-in test secrets and H2 `create-drop` in `src/test/resources/application-test.yml`.
- `@CreationTimestamp` on `Ticket.createdAt` overrides manual `.setCreatedAt()`. To backdate tickets in tests or seeds, use `TicketRepository.updateCreatedAt(id, pastInstant)` / `updateClosedAt(id, pastInstant)` (`@Modifying` JPQL queries).

## Security & Auth Mechanics
- **JWT & Tokens**: Stateless bearer token model. Access token TTL = 15m; refresh token TTL = 7d. Refresh tokens are stored as SHA-256 hashes in `refresh_tokens` table and rotated on each `/api/auth/refresh` call (old token is revoked).
- **Password Hashing**: `PepperedPasswordEncoder` computes `BCrypt(password + pepper + salt)`. On every successful login, `PasswordRehashService` generates a fresh salt and rehashes the password automatically.
- **OIDC Flow**: `GET /oauth2/authorization/support-oidc` -> Keycloak -> Spring OAuth2 callback saves email in session -> frontend calls `POST /api/auth/oidc/token` to exchange session for a JWT pair. Local user with matching email must exist and be enabled.
- **Roles**: `REQUESTER`, `SUPPORT_AGENT`, `TEAM_LEAD`, `ADMIN` (defined in `UserRole` enum).

## Domain & RBAC Rules (`TicketAccessService`)
- **Read**: All authenticated users can view all tickets, projects, and comments.
- **Ticket Content Edit** (title/desc): `ADMIN` and `TEAM_LEAD` can edit any ticket. `REQUESTER` can only edit their own ticket if status is `OPEN`. `SUPPORT_AGENT` cannot edit content.
- **Ticket Status**: `ADMIN` and `TEAM_LEAD` can set any status. `SUPPORT_AGENT` can only change status if assigned to the ticket. `REQUESTER` can only reopen (`CLOSED`/`RESOLVED` -> `REOPENED`) own tickets.
- **Ticket Assignment**: `ADMIN` and `TEAM_LEAD` can assign to any enabled `SUPPORT_AGENT`/`TEAM_LEAD` or unassign (`assigneeId: null`). `SUPPORT_AGENT` can only self-assign. `REQUESTER` cannot assign.
- **Closed Timestamps**: `Ticket.closedAt` is set automatically to `Instant.now()` when transitioning to `CLOSED`, and reset to `null` if moved to any non-closed status.
- **Project Move & Analytics**: Only `ADMIN` and `TEAM_LEAD`.
- **Ticket Delete & Admin Endpoints** (`/api/admin/**`): `ADMIN` role only.
