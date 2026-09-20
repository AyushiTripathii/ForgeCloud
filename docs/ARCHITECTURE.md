# Architecture

## Milestone 1

```text
Browser -> Next.js /api proxy -> Spring Boot -> PostgreSQL
                                  ^
GitHub push -> signed webhook ----|
```

The frontend never connects to PostgreSQL. Spring owns validation, transactions and persistence. A manual trigger or accepted push creates a durable `pipeline_runs` row in `QUEUED` state. No process consumes it in this milestone.

## Why this boundary

Project CRUD proves the complete frontend/API/database path. The webhook introduces a real external event without executing untrusted code. Pipeline records create the contract that a worker can use later. This separates control-plane requests from execution work.

## Modules

| Module | Responsibility now | Later extension |
|---|---|---|
| `project` | connect and manage repository metadata | ownership, GitHub installation |
| `github` | verify and parse pushes | installation auth, more events |
| `pipeline` | create/list durable runs | steps, transitions, retries |
| `common` | stable JSON errors | request IDs, observability |
| `apps/web` | projects and recent runs | logs, deployments, settings |

## Deliberate limits

- No login, organizations or project ownership.
- The repository is not checked against GitHub and no OAuth permission is granted.
- The webhook uses one local shared secret, not per-installation secrets.
- A delivery ID is deduplicated, but simultaneous identical deliveries may yield a 409 from the unique constraint rather than returning the earlier pipeline.
- All branch pushes create runs; branch filtering is a planned issue.
- Pipeline list is limited to the latest 50 and has no cursor.
- Manual runs have no commit SHA because ForgeCloud has not queried GitHub.
- No Redis queue, worker, log streaming or Docker execution.

## Security direction

Before external access: add authentication and ownership. Before real GitHub connection: use a GitHub App/OAuth installation and map installation/repository IDs rather than trusting names alone. Before running repositories: use dedicated workers, resource/time/network limits, ephemeral workspaces, non-root containers and strict cleanup. Never mount the host Docker socket inside a user build container.

Docker alone is not a complete hostile multi-tenant sandbox. Start with repositories owned by your team. Stronger isolation can be evaluated when the threat model requires it.

## Durable-job direction

PostgreSQL remains the source of truth. Add `pipeline_steps` and `job_attempts`, legal state transitions, an atomic lease/claim, lease expiry and fencing before multiple workers. If Redis is later used for delivery/wakeup, address database/queue consistency—prefer a transactional outbox or database polling over a destructive queue pop.

