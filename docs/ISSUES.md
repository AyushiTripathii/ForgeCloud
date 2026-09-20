# Issues to create

These are copy-ready plans; they have not been created on GitHub.

## M1-01 — Verify starter and lock dependencies

Labels: `setup`, `priority:high`. Depends on: none.

Run Compose, backend tests/verify, frontend typecheck/build and `scripts/smoke.py`. Generate and commit `apps/web/package-lock.json`; change web Dockerfile/CI to `npm ci`. Record the actually tested JDK, Maven, Node and Docker versions.

Acceptance: a clean checkout builds; CI is green; smoke passes; README validation statement is updated with evidence.

## M1-02 — Add project editing in the dashboard

Labels: `frontend`, `good first issue`. Depends on: M1-01.

Use the existing PUT endpoint. Populate fields, support Save/Cancel, block overlapping saves and keep old UI data when a request fails.

Acceptance: edits survive reload; Cancel sends no request; validation/server errors are visible; component tests cover success/failure.

## M1-03 — Pipeline detail endpoint and filters

Labels: `backend`, `frontend`. Depends on: M1-01.

Add `GET /pipelines/{id}` and project/status filters. Add a detail page. Keep response DTOs rather than serializing entities.

Acceptance: unknown IDs return 404; invalid status returns 400; database query is bounded; web handles loading/error/empty states.

## M1-04 — Restrict webhooks to configured branch

Labels: `github`, `backend`. Depends on: M1-01.

Only create a pipeline when the pushed branch matches the project's configured branch. Return a clear accepted/ignored response otherwise.

Acceptance: tests cover matching/nonmatching branches and tag refs; no pipeline is created for ignored refs.

## M1-05 — Make concurrent webhook deduplication idempotent

Labels: `distributed-systems`, `backend`. Depends on: M1-01.

Handle two simultaneous deliveries with the same ID so both callers receive the same logical result rather than one receiving 409. Preserve the database unique constraint.

Acceptance: concurrency integration test creates exactly one run and both requests have documented successful/idempotent behavior.

## M1-06 — Authentication and project ownership

Labels: `security`, `backend`. Depends on: M1-01.

Introduce users/sessions and owner-scoped repository access. Do not accept owner ID from client JSON. Decide CSRF protection if using cookies.

Acceptance: anonymous mutation fails; cross-user read/write/delete fails; ownership tests cover every project/pipeline route.

## M1-07 — GitHub App installation connection

Labels: `github`, `security`. Depends on: M1-06.

Use least-privilege GitHub installation authorization and stable repository IDs. Do not store long-lived tokens in plaintext.

Acceptance: only installed repositories can connect; revoked installation fails safely; secret/token logging is prevented.

## M2-01 — Pipeline steps and transition rules

Labels: `pipeline`, `backend`. Depends on: M1-03.

Add steps and an explicit legal transition service. Persist timestamps and failure summary. Completed states are terminal.

Acceptance: illegal transitions fail; queued work survives restart; integration tests prove ordering and terminal behavior.

## M2-02 — Single trusted worker

Labels: `worker`, `infrastructure`. Depends on: M2-01.

Create a separate Spring Boot worker process. Initially run only a repository fixture controlled by the team. Define atomic claim, timeout and cleanup.

Acceptance: success/nonzero/timeout each persist correct states; killing the worker has documented recovery; API request threads never execute builds.

## M2-03 — Docker execution sandbox baseline

Labels: `docker`, `security`. Depends on: M2-02.

Execute the trusted fixture as non-root with CPU/memory/PID/time limits, read-only base filesystem where practical, isolated temporary workspace and guaranteed cleanup.

Acceptance: limits are demonstrated; timed-out container is removed; worker credentials are absent inside build; host Docker socket is not mounted into the user container.

