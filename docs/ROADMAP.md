# Roadmap

| Version | Deliverable | Evidence before moving on |
|---|---|---|
| 0.1 | projects, verified webhook, queued runs | CI green, smoke test passes, restart preserves data |
| 0.2 | pipeline steps and trusted demo worker | fixed fixture succeeds/fails and records every transition |
| 0.3 | isolated Docker execution | timeout/resources/cleanup tested; no API-thread execution |
| 0.4 | bounded live logs with SSE | reconnect resumes from offset; slow client cannot block worker |
| 0.5 | multiple workers and optional Redis delivery | atomic claim; no job lost when worker exits |
| 0.6 | leases, attempts, retries and heartbeats | crash/expiry/fencing tests pass |
| 0.7 | BuildKit image and local registry | immutable digest linked to source and build result |
| 0.8 | single-node deployment | health-checked demo app reachable locally |
| 0.9 | deployment history and rollback | previous healthy digest restores service |
| 1.0 | auth, secrets, audit, operations hardening | isolation/security/recovery checklist passes |

Only after that consider Kafka, gRPC, MinIO/S3, Traefik, Prometheus/Grafana, OpenTelemetry and replicas. Kubernetes may eventually host ForgeCloud; it is not needed to implement ForgeCloud's first CI/CD system.

## Worker sequence

Start with a trusted built-in fixture, not arbitrary public repositories. Add `pipeline_steps` (`CLONE`, `TEST`, `BUILD`) and `job_attempts`. A worker atomically claims one queued attempt, launches an isolated container, captures a bounded log, records exit result and cleans its workspace. Cancellation and timeout must reach the process/container.

After one worker is reliable, make claims safe across workers using leases and fencing. Redis can wake workers, but PostgreSQL should keep durable state. Add GitHub clone credentials only after GitHub installation ownership and secret handling are implemented.

