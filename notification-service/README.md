# notification-service

The mailroom — reacts to Kafka events, sends emails. No human ever calls this
service directly; every use case here starts with an event arriving. No REST API,
no controllers.

| | |
|---|---|
| Port | `8084` |
| Database | none (fully stateless — the `notif_db` audit-log table the db-schema doc describes as optional was skipped) |

## Events consumed

Full event shape, the DLT/retry story, and the end-to-end flow live in
`shared-events/README.md` — this is what happens on this end:

| Topic | Handler | What happens |
|---|---|---|
| `task.assigned` | `TaskEventListener` | Looks up the assignee's email via `auth-service`, sends a notification email via `JavaMailSender`. |

### Resolving an email address

The event only carries `assigneeUserId` — Project Service doesn't own emails.
This service calls `auth-service`'s internal lookup endpoint to get one:

```
GET {auth-service.base-url}/internal/users/{id}  →  {id, email}
```

That endpoint is unauthenticated (no JWT — there's no human in this call, it's
service-to-service) and reachable by direct URL, not through Eureka/Gateway yet
(P5). "No such user" is treated as a normal outcome (logged, skipped) — not a
failure worth retrying.

## Configuration

| Property | Meaning |
|---|---|
| `spring.mail.*` | SMTP connection for `JavaMailSender`. **Still placeholder values** (`smtp.example.com`) — real sending needs a real mailbox configured. |
| `spring.kafka.bootstrap-servers` / `consumer.*` | Kafka connection + consumer group (`notification-service`) |
| `auth-service.base-url` | Where to call for `GET /internal/users/{id}` |
| `eureka.client.service-url.defaultZone` | Present on the classpath, unused until P5 |

Real values live in the gitignored `application.properties`; `application.properties.example`
only ever holds placeholders.
