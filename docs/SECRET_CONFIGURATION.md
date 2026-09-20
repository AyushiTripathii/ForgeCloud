# Configure secrets before starting

Copy `.env.example` to `.env`, then fill in `POSTGRES_PASSWORD` and `GITHUB_WEBHOOK_SECRET` with your own independent random values. The example contains no passwords or keys. Compose refuses to start if either required value is empty.

Docker Compose reads `.env` and passes the database password as `SPRING_DATASOURCE_PASSWORD` and the webhook secret as `FORGECLOUD_GITHUB_WEBHOOK_SECRET` to Spring. Spring itself does not automatically load `.env`.

For running Maven directly, set `SPRING_DATASOURCE_PASSWORD` and `FORGECLOUD_GITHUB_WEBHOOK_SECRET` in your IDE or shell. The default JDBC URL is `jdbc:postgresql://localhost:5432/forgecloud`; override it with `SPRING_DATASOURCE_URL` if needed. This project does not read a generic `DATABASE_URL` variable.

For `scripts/smoke.py`, set `GITHUB_WEBHOOK_SECRET` in the shell to the same value you chose in `.env`. The script does not load `.env` automatically.

GitHub OAuth, JWT, Redis and AWS names in the example are reserved for future modules; leaving them empty is correct. Those integrations are not implemented by adding variables. Never prefix a secret with `NEXT_PUBLIC_`.

## Before committing

Run `git status --short` and review `git diff --cached`. `.env.example` should be tracked; `.env`, local/prod configuration, private keys and build outputs should not. Ignore rules cannot detect secrets embedded in source code or remove files already tracked.

If an exact sensitive file is already tracked, use `git rm --cached -- <exact-path>` to remove it from the index while keeping your working copy. If a real credential was committed or pushed, revoke/rotate it; a later ignore rule does not erase history.

The Docker ignore files also exclude environment files and keys from build contexts. Keep real credentials outside the source tree where possible. Public test fixtures may use non-secret example HMAC keys; they are not application credentials.

If PostgreSQL already has a data volume, changing `.env` does not change the existing database user's password. Match the existing password or change it deliberately in PostgreSQL. Do not delete the data volume just to change a password.
