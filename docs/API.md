# API — milestone 1

Base URL: `http://localhost:8080`. The Next.js server proxies browser `/api/*` requests to this API.

| Method | Path | Result |
|---|---|---|
| GET | `/actuator/health` | service/database health |
| GET | `/api/v1/projects` | all projects |
| POST | `/api/v1/projects` | connect project, 201 |
| GET | `/api/v1/projects/{id}` | one project |
| PUT | `/api/v1/projects/{id}` | replace editable project fields |
| DELETE | `/api/v1/projects/{id}` | project plus runs, 204 |
| GET | `/api/v1/pipelines` | latest 50 runs |
| POST | `/api/v1/projects/{id}/pipelines` | manual queued run, 201 |
| POST | `/api/v1/webhooks/github` | verified GitHub event, 202 |

Project request:

```json
{
  "name": "payment-service",
  "repositoryUrl": "https://github.com/owner/payment-service",
  "branch": "main"
}
```

URLs are normalized to `https://github.com/owner/repository`. Only HTTPS `github.com` owner/repository URLs without credentials, query strings, fragments or nested paths are accepted. One connected project is allowed per repository, case-insensitively.

Pipeline response fields: `id`, `projectId`, `projectName`, `commitSha`, `branch`, `status`, `triggerType`, `createdAt`. Current creation state is always `QUEUED`.

Webhook headers:

- `X-GitHub-Event`
- `X-GitHub-Delivery`
- `X-Hub-Signature-256`

The signature is checked over the exact raw body using HMAC-SHA256. `push` events on branch refs create a run; other event/ref types return 202 and are ignored. Duplicate delivery IDs return the existing run in ordinary sequential delivery. The matching repository must already be connected.

Errors generally use:

```json
{
  "status": 400,
  "error": {"message": "The request is invalid"},
  "timestamp": "2026-09-20T00:00:00Z"
}
```

Status codes include 400 invalid request/signature, 404 unknown project/repository, and 409 duplicate/conflicting resource. Unexpected exceptions use Spring Boot's default error response in this starter.

