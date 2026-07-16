# AGENTS.md — tech-support-ticket-service

## Quickstart
```bash
docker compose up -d          # PostgreSQL 17
./gradlew bootRun             # app on :8080
curl http://localhost:8080/api/init-db  # seed test data (idempotent)
```

## Stack
- **Java 21**, **Spring Boot 4.1.0**, **Gradle 9.5.1** (Kotlin DSL)
- **PostgreSQL 17** (prod, via Docker), **H2** (test, in-memory)
- **Lombok** for boilerplate (@Getter, @Slf4j, etc.)
- No Spring Security, No migrations (Flyway/Liquibase)

## Commands
```bash
./gradlew test     # runs the single @SpringBootTest smoke test
./gradlew bootRun  # starts app (also runs tests)
```
There are **no lint, format, or typecheck** commands configured.

## Architecture
- Single-module, standard layered MVC under `self.project.web.ticket.service`
- Entities: `Ticket`, `Comment`, `User`, `Project`, `TicketStatus` (enum)
- DTOs: Java `record` classes with static `from(Entity)` factory
- All API paths under `/api`; static SPA frontend at `/` (`src/main/resources/static/index.html`)

## Database
- `hibernate.ddl-auto: update` — schema is created/updated from JPA annotations automatically. **No migration scripts exist.**
- Prod config: `src/main/resources/application.yml`
- Test config: `src/test/resources/application.yml` (H2, `create-drop`)

## API Endpoints
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/projects` | List projects |
| GET | `/api/users` | List users |
| GET | `/api/init-db` | Seed 27 tickets across 3 projects with varied dates (idempotent) |
| GET/POST | `/api/projects/{id}/tickets` | List/create tickets |
| GET/PUT | `/api/tickets/{id}` | Get/update ticket (send `assigneeId: null` to unassign; `projectId` to reassign) |
| GET/POST | `/api/tickets/{id}/comments` | List/add comments |
| GET | `/api/projects/{id}/analytics` | Status counts, created-vs-resolved time-series, avg resolution per assignee |
| GET/POST | `/api/admin/users` | List/create users |
| PUT/DELETE | `/api/admin/users/{id}` | Update/delete user |
| GET/POST | `/api/admin/projects` | List/create projects |
| PUT/DELETE | `/api/admin/projects/{id}` | Update/delete project |

## Frontend
- `index.html` — landing page: user list + project cards; nav link to Admin
- `project.html` — project view with ticket list, create form, detail modal (split-field status/assignee/project), comments, and Analytics modal (Chart.js 4.4.4 from CDN)
- `admin.html` — inline CRUD for users and projects
- Status dropdown rules (frontend-only): **CLOSED** shows only `REOPENED`; all other statuses hide `REOPENED`
- `<option>` elements strip HTML; the `(me)` suffix on the current user is plain text, not italic

## Database & Entities
- `hibernate.ddl-auto: update` — schema is created/updated from JPA annotations automatically. **No migration scripts exist.**
- Prod config: `src/main/resources/application.yml`
- Test config: `src/test/resources/application.yml` (H2, `create-drop`)
- `Ticket.closedAt` is set automatically when status transitions to `CLOSED` (not reset on later changes)
- `@CreationTimestamp` on `Ticket.createdAt` overrides manual `.setCreatedAt()`. To backdate tickets in test data, use `TicketRepository.updateCreatedAt(id, pastInstant)` which is a `@Modifying` JPQL UPDATE that bypasses the annotation

## Conventions
- `TicketService` defaults to `@Transactional(readOnly=true)` at class level; write methods override with `@Transactional`
- Controllers log with `@Slf4j`, prefixing method+path in each log line
- Entity IDs use `GenerationType.IDENTITY`
- DTOs: Java `record` classes with static `from(Entity)` factory
- No auth — endpoints are public