# tenant-service

The leasing office — signs up new companies (tenants), assigns them a plan, tracks
status. Owns `tenant_db`; nothing else can query it directly.

| | |
|---|---|
| Port | `8082` |
| Database | `tenant_db` (Postgres, schema managed by Hibernate `ddl-auto=update`) |
| Tables | `tenants`, `plans` |

## Endpoints

| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/tenants/signup` | `{name, planId}` → `201` + the new tenant. Unknown `planId` → `404`. |
| `GET` | `/api/tenants/{id}` | `404` if unknown. |
| `GET` | `/api/plans` | Lists all plans (`free`/`pro`/`enterprise`). |

`tenants.plan_id → plans.id` is the one real foreign key in the whole system that
stays inside a single database — everywhere else, cross-service IDs (`tenant_id`,
`assignee_user_id`, etc.) are logical links only.

## Events published

Full event shape, the DLT/retry story, and the end-to-end flow live in
`shared-events/README.md` — this is just what originates here:

| Topic | When | Consumed by |
|---|---|---|
| `tenant.created` | a tenant finishes signup | `billing-service` — caches the tenant's plan limits (`maxUsers`, `maxProjects`) to check usage against later |

The event carries the plan's limits directly (not just a `planId`) so
`billing-service` never has to call back into this service to find out what a
tenant is allowed to have.

## Configuration

| Property | Meaning |
|---|---|
| `spring.datasource.*` | Postgres connection for `tenant_db` |
| `spring.kafka.bootstrap-servers` | Kafka connection — see "Events published" above |
| `eureka.client.service-url.defaultZone` | Present on the classpath, unused until P5 |

Real values live in the gitignored `application.yml`; `application.yml.example`
only ever holds placeholders.
