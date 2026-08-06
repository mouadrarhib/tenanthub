# billing-service

The finance office — tracks usage per tenant against their plan's limits. Mostly
event-driven (reacts to Kafka), no REST API yet (the "view my invoices & usage"
use case from the docs isn't built — this phase only covers tracking).

| | |
|---|---|
| Port | `8085` |
| Database | `tenanthub_billing` (Postgres, schema managed by Hibernate `ddl-auto=update`) |
| Tables | `tenant_plan_limits`, `usage_records` |

## Data model

```
tenant_plan_limits              usage_records
├── tenant_id (UUID, PK)        ├── id (UUID, PK)
├── plan_name (varchar)         ├── tenant_id (UUID, logical)
├── max_users (int)             ├── metric_name (varchar, e.g. "projects_count")
└── max_projects (int)          ├── value (int)
                                 └── recorded_at (timestamp)
```

`tenant_plan_limits` is a trusted local copy, kept in sync via `tenant.created` —
this service never calls `tenant-service` to check a limit. `usage_records` is an
append-only log (one row per unit of usage), not a running counter — current usage
is `count(*)` filtered by `tenant_id` + `metric_name`.

## Events consumed

Full event shape, the DLT/retry story, and the end-to-end flow live in
`shared-events/README.md` — this is what happens on this end:

| Topic | Handler | What happens |
|---|---|---|
| `tenant.created` | `TenantEventListener` | Upserts a `tenant_plan_limits` row (tenantId is the primary key, so a re-delivered event just overwrites, never duplicates). |
| `project.created` | `ProjectEventListener` | Appends a `usage_records` row, counts the tenant's total, and logs a warning if that count exceeds the cached `max_projects`. |

**Detection, not enforcement.** The warning is only a log line — nothing stops
`project-service` from creating a project past the limit. Real enforcement would
need `project-service` to call this service *before* saving, which doesn't exist.

## Configuration

| Property | Meaning |
|---|---|
| `spring.datasource.*` | Postgres connection for `tenanthub_billing` |
| `spring.kafka.bootstrap-servers` / `consumer.*` | Kafka connection + consumer group (`billing-service`) |
| `eureka.client.service-url.defaultZone` | Present on the classpath, unused until P5 |

Real values live in the gitignored `application.properties`; `application.properties.example`
only ever holds placeholders.
