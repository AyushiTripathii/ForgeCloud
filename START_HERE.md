# Start ForgeCloud here

You are building **one long-running project**. This first version is small enough to understand, while its database and APIs are ready to grow into the CI/CD system in your outline.

## Install now

1. Git and VS Code.
2. Docker Desktop. On Windows, use its WSL 2 Linux-container backend.
3. Optional for local editing: JDK 21, Maven 3.9+, Node.js 24 and Python 3.

Do not install Redis, Kafka, Kubernetes, MinIO, Prometheus or gRPC yet.

## First run

Extract the ZIP, open the `forgecloud-spring` directory in VS Code, and open PowerShell there:

```powershell
Copy-Item .env.example .env
notepad .env
```

Set `POSTGRES_PASSWORD` and `GITHUB_WEBHOOK_SECRET` to your own independent random values, save and close the file. Future-module values can remain blank. Then run:

```powershell
docker compose up --build
```

Open http://localhost:3000. Then:

1. Connect a repository such as `https://github.com/your-name/demo-api`.
2. Click **Run pipeline**.
3. Confirm a `QUEUED` record appears.
4. Restart Compose and confirm the data remains.

Nothing is cloned or executed yet. `QUEUED` means ForgeCloud persisted work that a future worker will claim.

## Understand one flow

Start with `apps/web/app/page.tsx`. Its form sends JSON to `ProjectController`, which calls `ProjectService`. The service validates and normalizes the GitHub URL, then `ProjectRepository` stores the entity in PostgreSQL. The response comes back to React and is prepended to state.

Next, trace **Run pipeline** through `PipelineController` and `PipelineService`. This is the beginning of the future job system.

## First issues

Create the issues in `docs/ISSUES.md` in your GitHub repository:

1. M1-01: verify the starter and commit dependency lock state.
2. M1-02: add project editing to the dashboard.
3. M1-03: expose pipeline details and filters.
4. Only after the foundation is green, start M2-01 for pipeline steps.

## Put it on GitHub

```sh
git init
git add .
git commit -m "Initialize ForgeCloud Spring milestone 1"
git branch -M main
```

Create an empty GitHub repository, then use the remote/push commands GitHub gives you. Nothing is pushed automatically. Pick a license and enable pull-request reviews before inviting outside contributors.
