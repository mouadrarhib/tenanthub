# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

TenantHub is a multi-tenant SaaS backend: a Maven multi-module monorepo of 7 Spring
Boot services plus one shared library module. See `ROADMAP.md` for the build order —
services are built one at a time, in phases; don't jump ahead.

## Stack

- Java 17, Maven multi-module (not Gradle) — root `pom.xml` is the aggregator
- Spring Boot 4.1.0, Spring Cloud 2025.1.2 (`spring-cloud-dependencies` BOM, pinned in root `pom.xml`)
- Modules: `auth-service`, `tenant-service`, `project-service`, `notification-service`,
  `billing-service`, `gateway-service`, `discovery-service`, `shared-events` (plain jar, no Spring Boot)

## Commit conventions

Use **Conventional Commits**: `<type>(<scope>): <summary>`

- **type**: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `build`, `ci`
- **scope**: the module the change is confined to (`auth-service`, `shared-events`,
  `infra`, etc.); omit the scope for changes spanning multiple modules
- **summary**: imperative mood ("add", not "added"/"adds"), lowercase, no trailing
  period, ≤ 72 chars
- **body** (optional, blank line after summary): explain *why*, not what — the diff
  already shows what changed
- One logical change per commit — don't bundle unrelated modules or unrelated concerns
  into a single commit

Examples:

```
feat(auth-service): add JWT token generation

fix(project-service): scope task queries by tenant_id

chore: bump spring-cloud-dependencies to 2025.1.2
```

Hard rules:

- Never commit unless explicitly asked to in that turn — scaffolding/editing files is
  not implicit permission to commit them.
- Never amend an existing commit or force-push unless explicitly asked.
- Never use `--no-verify` or otherwise skip hooks.

## Secrets and config

- Real `application.yml` / `.env` files are gitignored and must never be committed —
  they hold local DB/mail/JWT credentials.
- Every service's committed config lives in `application.yml.example` (placeholder
  values only). When a service needs a new config key, add it to the `.example` file
  first, then mirror it into the local (gitignored) `application.yml`.

## Scope discipline

- Don't add entities, controllers, business logic, or messaging (Kafka) code to a
  service unless explicitly asked to build that specific piece — scaffolding and
  implementation are separate asks.
- Check `ROADMAP.md` before starting a new phase; confirm with the user if it's
  unclear which phase is next.

## Reference

- `ROADMAP.md` — phase-by-phase build checklist, source of truth for what's done.
  **Gitignored on purpose** — it's a personal local tracker, never commit it or remove
  it from `.gitignore`.
- `New folder/` — the original HTML planning docs (architecture, db schemas,
  dependencies, use cases, roadmap) `README.md` and `ROADMAP.md` were derived from.
  **Also gitignored on purpose** — same reasoning as `ROADMAP.md`, never commit it.
- Root `pom.xml` — module list + `spring-cloud-dependencies` version
