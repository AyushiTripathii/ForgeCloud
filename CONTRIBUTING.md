# Contributing

Read `START_HERE.md` and `docs/ARCHITECTURE.md`. Choose an unassigned issue and agree on its acceptance criteria. Use branches such as `feat/12-project-editing` and open a pull request; do not push directly to protected `main`.

Before review, run relevant backend tests/verify, frontend typecheck/build and the smoke check. Format Java consistently. Update API/architecture docs for contract changes and add a Flyway migration for schema changes—never edit an already-applied shared migration.

Do not commit `.env`, tokens, repository credentials or logs containing secrets. Do not weaken webhook validation. Any worker issue must explain isolation, timeout, cleanup and failure recovery. Any queue issue must explain durable state and the database/queue consistency boundary.

Keep pull requests scoped. Include the problem, behavior, tests and risks. The repository owner must choose a license and configure reviews/branch protection before accepting outside contributions.

