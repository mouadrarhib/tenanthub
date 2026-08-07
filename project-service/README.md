# project-service

The actual workspace — projects, tasks, and comments (see `tenanthub-db-schemas.html`).
Everything here is scoped behind a JWT that `auth-service` signed; this service
verifies it locally and never calls `auth-service` over the network to do so.

| | |
|---|---|
| Port | `8083` |
| Database | `tenanthub_project` (Postgres, schema managed by Flyway) |
| Tables | `projects`, `tasks`, `comments` |

## Data model

```
projects                        tasks                          comments
├── id (UUID, PK)                ├── id (UUID, PK)              ├── id (UUID, PK)
├── tenant_id (UUID, logical)    ├── project_id → projects.id   ├── task_id → tasks.id
├── name (varchar)               ├── title (varchar)            ├── author_user_id (UUID, logical)
├── description (text)           ├── status (TODO/DOING/DONE)   ├── content (text)
└── created_at (timestamp)       ├── assignee_user_id (logical) └── created_at (timestamp)
                                  └── due_date (date)
```

Real foreign keys (`ON DELETE CASCADE`) stay inside this database: `projects → tasks →
comments`. `tenant_id`, `assignee_user_id`, `author_user_id` are **logical** links to
other services' databases (Tenant Service, Auth Service) — never a real FK, per the
database-per-service rule. `tenant_id` is stored on every `project` row but **not yet
enforced** on queries (no cross-tenant scoping) — that lands in P3.

## Endpoints

All endpoints require `Authorization: Bearer <token>` from `auth-service`'s
`/api/auth/login` or `/api/auth/register` — except Actuator, Swagger, and `/error`.

| Method | Path | Auth |
|---|---|---|
| `POST` | `/api/projects` | any authenticated user |
| `GET` | `/api/projects` | any authenticated user |
| `GET` | `/api/projects/{id}` | any authenticated user |
| `PUT` | `/api/projects/{id}` | any authenticated user |
| `DELETE` | `/api/projects/{id}` | **`ADMIN`** |
| `POST` | `/api/projects/{projectId}/tasks` | any authenticated user |
| `GET` | `/api/projects/{projectId}/tasks` | any authenticated user |
| `GET` | `/api/tasks/{id}` | any authenticated user |
| `PUT` | `/api/tasks/{id}` | any authenticated user |
| `DELETE` | `/api/tasks/{id}` | **`ADMIN`** |
| `POST` | `/api/tasks/{taskId}/comments` | any authenticated user |
| `GET` | `/api/tasks/{taskId}/comments` | any authenticated user |
| `GET` | `/api/comments/{id}` | any authenticated user |
| `DELETE` | `/api/comments/{id}` | **`ADMIN`** |

Comments have no update endpoint — a posted comment is treated as immutable.

```
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@tenanthub.com","password":"password123"}' | jq -r .token)

curl -X POST http://localhost:8083/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"11111111-1111-1111-1111-111111111111","name":"Launch Website","description":"Q3 relaunch"}'

curl -X POST http://localhost:8083/api/projects/{projectId}/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Design homepage","dueDate":"2026-09-01"}'
```

## Authentication & authorization

`security/SecurityConfig` builds a `JwtDecoder` from a **shared HMAC secret**
(`jwt.secret`, must be byte-for-byte identical to `auth-service`'s) rather than a real
OAuth2/OIDC issuer — there's no `issuer-uri`, no JWK discovery. `auth-service` signs
with HS256; this service recomputes the same HMAC to verify. The full signing →
verifying walkthrough, including a real bug we hit around algorithm auto-negotiation,
is documented in `auth-service/README.md` — that's the deeper read; this section only
covers what's specific to *this* service.

`JwtAuthenticationConverter` reads the token's `roles` claim (not the OAuth2-standard
`scope`) and maps it to Spring Security authorities (`ROLE_ADMIN`, `ROLE_MEMBER`). The
filter chain then gates on those:

```java
.requestMatchers("/actuator/**", "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
.permitAll()
.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

So: any authenticated caller can create/read/update; only a token carrying `ROLE_ADMIN`
can `DELETE` anything. `/error` is public too — a validation failure or bad-JSON body
internally forwards to `/error` to render the response, and that forward re-enters the
filter chain and needs its own permit, or an otherwise-valid `400` gets turned into a
bare `401`/`403` instead.

A missing/invalid token → `401` (`WWW-Authenticate: Bearer error="invalid_token"`), a
valid token without `ADMIN` on a `DELETE` → `403`. Both come from Spring Security's
own OAuth2 resource-server filter, **before** the request reaches a controller — so
they use Spring Security's default error body, not this service's own `ErrorResponse`
shape below.

## Error handling

`exception/GlobalExceptionHandler` (`@RestControllerAdvice`) maps our own exceptions to
a consistent JSON body:

```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "Project not found: <id>", "path": "/api/projects/<id>" }
```

| Exception | Status |
|---|---|
| `ResourceNotFoundException` | `404` |
| `MethodArgumentNotValidException` (`@Valid` failures) | `400`, message lists every failing field |

Deliberately no blanket `Exception` handler — that would also swallow exceptions
Spring MVC already handles well (malformed JSON, a non-UUID path variable), turning
their normal `400` into a generic `500`.

## Events published

This service publishes to Kafka after each save completes (fire-and-forget — it
doesn't wait for or care who's listening). Full event shapes, the DLT/retry story,
and the end-to-end flow live in `shared-events/README.md`; this is just what
originates here:

| Topic | When | Consumed by |
|---|---|---|
| `project.created` | a project is created | `billing-service` (usage tracking) |
| `task.created` | a task is created | *(no consumer yet)* |
| `task.assigned` | a task is created **with** an assignee, or an existing task's assignee changes | `notification-service` (email) |

## API docs

Swagger UI: `http://localhost:8083/swagger-ui/index.html` · OpenAPI JSON:
`http://localhost:8083/v3/api-docs`. Both are public (see the security section above);
actually calling an endpoint through "Try it out" still needs a bearer token.

## Running locally

1. Postgres running locally, database `tenanthub_project` created (Flyway applies
   `db/migration/V1__init.sql` automatically on startup — no manual schema step).
2. `application.yml` (gitignored, copy from `application.yml.example`)
   needs `jwt.secret` set to the **exact same value** as `auth-service`'s.
3. Start `auth-service` first (port `8081`) so you have somewhere to get a token from.
4. `./mvnw spring-boot:run`

Eureka registration failures in the log (`Connection refused` to `localhost:8761`) are
expected and harmless right now — `discovery-service` doesn't exist yet (that's P5).

## Tests

```
./mvnw test
```

- **Controller tests** (`controller/*Test.java`) — `@WebMvcTest` + Mockito, service
  layer mocked, security filters disabled (`@AutoConfigureMockMvc(addFilters = false)`)
  since these test the controller/validation layer in isolation, not auth.
- **Repository tests** (`repository/*Test.java`) — `@DataJpaTest` against the real
  local Postgres (`@AutoConfigureTestDatabase(replace = Replace.NONE)`, not an embedded
  substitute — the schema uses Postgres-only features like `gen_random_uuid()`), each
  rolled back afterward.

Neither suite currently exercises the security filter chain itself (401/403/RBAC) —
that's still open.

## Configuration

| Property | Meaning |
|---|---|
| `spring.datasource.*` | Postgres connection for `tenanthub_project` |
| `jwt.secret` | Shared HMAC-SHA256 key — must match `auth-service` exactly |
| `spring.kafka.bootstrap-servers` | Kafka connection — see "Events published" above |
| `eureka.client.service-url.defaultZone` | Present on the classpath, unused until P5 |

Real values live in the gitignored `application.yml`; `application.yml.example`
only ever holds placeholders.

## Known limitations (by design, for now)

- **No tenant scoping.** `tenant_id` is stored but not enforced on any query — a token
  from tenant A can currently read/write projects belonging to tenant B. Fixing this is
  the whole point of P3.
- **No security-layer tests.** Controller tests mock the service and disable the
  filter chain; nothing yet asserts 401 without a token or 403 without `ADMIN`.
- **No admin-provisioning flow** on the auth side — see `auth-service/README.md`.
