# ForgeCloud

ForgeCloud is a developer CI/CD and deployment platform. A developer connects a GitHub repository; ForgeCloud will eventually test, build, containerize, deploy and monitor it.

This repository is the safe **Milestone 1 foundation**:

- Spring Boot REST API and PostgreSQL persistence
- GitHub project registration
- manual pipeline triggers
- HMAC-SHA256 verification for GitHub push webhooks
- webhook delivery deduplication
- pipeline history with explicit states
- Next.js + TypeScript dashboard
- Flyway migrations, tests, Docker Compose and CI

Pipelines remain `QUEUED`. There is intentionally no worker and no user-code execution yet.

## Run it

Read [START_HERE.md](START_HERE.md), then from this directory:

```powershell
Copy-Item .env.example .env
# Edit .env and set POSTGRES_PASSWORD and GITHUB_WEBHOOK_SECRET first.
notepad .env
docker compose up --build
```

macOS/Linux uses `cp .env.example .env`. Open http://localhost:3000 after both servers start. The first build downloads images and dependencies.

## Structure

| Path | Purpose |
|---|---|
| `src/main/java/dev/forgecloud/project` | repository project CRUD |
| `src/main/java/dev/forgecloud/pipeline` | pipeline records and triggers |
| `src/main/java/dev/forgecloud/github` | signed webhook receiver |
| `src/main/java/dev/forgecloud/common` | API errors |
| `src/main/resources/db/migration` | versioned PostgreSQL schema |
| `apps/web` | Next.js dashboard |
| `deploy` | backend container image |
| `scripts/smoke.py` | full API/webhook smoke check |
| `docs` | architecture, API, roadmap and issues |

## Local checks

```sh
mvn test
mvn verify
cd apps/web
npm install
npm run typecheck
npm run build
```

With the stack running, first set `GITHUB_WEBHOOK_SECRET` in your shell to the value in `.env` (the script does not load that file):

```sh
python3 scripts/smoke.py
```

Windows may use `py scripts/smoke.py`.

## Honest status

The source tree, JSON, YAML and Python syntax were validated while packaging. The earlier environment check found Java 17, not the required Java 21; Maven and Docker were unavailable. Spring compilation, automated tests, Next.js build and full Compose startup have **not** yet been executed. Issue M1-01 is the first issue so your team verifies this starter on a development machine.

Secret handling has been updated: expanded Git/Docker ignore rules, blank example secrets and required environment variables without default passwords. Read [docs/SECRET_CONFIGURATION.md](docs/SECRET_CONFIGURATION.md) before starting or committing.

The Dockerfiles currently install dependencies without committed Maven/npm lock artifacts. Maven resolves versions through its effective POM; npm should gain a committed `package-lock.json` during M1-01 and then switch from `npm install` to `npm ci`.

Do not expose this milestone publicly: it has no user authentication. Use only non-sensitive test repositories. A valid signature proves possession of the shared webhook secret, but access control and GitHub App/OAuth installation mapping still belong to later milestones.

## Reference outline

Your complete revised idea is preserved in [docs/PROJECT_OUTLINE.md](docs/PROJECT_OUTLINE.md). The roadmap converts that vision into buildable stages.

## License

No license has been selected. The repository owner should choose one before accepting outside contributions.
