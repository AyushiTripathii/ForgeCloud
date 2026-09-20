# Learn alongside the project

## First week

| Day | Learn | Do |
|---|---|---|
| 1 | Spring request → service → repository flow | start Compose and trace project creation |
| 2 | JPA entities, transactions and Flyway | inspect tables and restart persistence |
| 3 | HTTP status codes, validation and DTOs | run requests and read error responses |
| 4 | Next.js state, fetch and proxying | add or design project editing |
| 5 | webhook event, raw body and HMAC | run the signed webhook smoke flow |
| 6 | Git branches, issues, PR reviews and CI | merge one small reviewed issue |
| 7 | pipeline state machine | design steps and legal transitions |

## Explain this before adding a worker

A push event is external input. ForgeCloud authenticates it with the shared signature, reads repository/ref/SHA, deduplicates by delivery ID, maps the repository to a connected project and stores a `QUEUED` run. The HTTP request does not build anything.

Then learn in this order: state transitions → atomic claims → Docker process lifecycle → bounded logs → SSE → retries and attempts → leases/heartbeats → image digests → deployments → health checks → rollback → observability.

