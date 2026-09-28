# Windows Troubleshooting Guide

This guide covers common problems when setting up ForgeCloud locally on Windows.

Before troubleshooting, make sure you are running commands from the ForgeCloud project folder, the folder that contains `compose.yaml`.

> **Security:** Never share your `.env` file or real secret values. Use your own local values for required secrets.

## Before you start

For the Windows setup, ForgeCloud uses Docker Desktop with its WSL 2 Linux-container backend.

The project uses a `.env` file for local Compose configuration. Create it from `.env.example` and keep it **beside `compose.yaml`** in the project root.

In PowerShell, from the ForgeCloud project folder:

```powershell
Copy-Item .env.example .env
```

Open `.env` and set your own values for:

```text
POSTGRES_PASSWORD=
GITHUB_WEBHOOK_SECRET=
```

Do not commit `.env` or share its contents.

## Docker command is not recognized

### Error

PowerShell reports an error such as:

```text
docker : The term 'docker' is not recognized as the name of a cmdlet, function, script file, or operable program.
```

### Likely cause

The Docker CLI is not available in the current PowerShell session. This usually means Docker Desktop is not installed, or the Docker CLI is not available on `PATH`.

Downloading the Docker Desktop installer is **not the same as installing Docker Desktop**. Run the installer and complete the installation before expecting the `docker` command to work.

For installation requirements and steps, see the official [Docker Desktop installation documentation](https://docs.docker.com/desktop/setup/install/windows-install/).

If `docker --version` works but `docker info` cannot connect to the engine, see the [Docker Desktop WSL 2 documentation](https://docs.docker.com/desktop/features/wsl/) and the troubleshooting section below.


For installation requirements and steps, see the official [Docker Desktop installation documentation](https://docs.docker.com/desktop/setup/install/windows-install/).

If `docker --version` works but `docker info` cannot connect to the engine, see the [Docker Desktop WSL 2 documentation](https://docs.docker.com/desktop/features/wsl/) and the troubleshooting section below.


### Next steps

1. Install Docker Desktop for Windows from the official Docker documentation.
2. During setup, use the WSL 2 Linux-container backend as recommended by the project setup instructions.
3. Start Docker Desktop and wait for it to finish starting.
4. Open a new PowerShell window so it picks up the Docker CLI configuration.
5. Check that the Docker CLI is available:

```powershell id="plq3q1"
docker --version
```

6. Check that Docker Compose is available:

```powershell id="l1j4u9"
docker compose version
```

If both commands return version information, continue with the ForgeCloud setup instructions.

## Docker Desktop is installed but the engine is not running

### Error

The `docker` command is recognized, but commands that need the Docker Engine fail. For example:

```powershell
docker info
```

may fail because Docker cannot connect to the Docker Engine.

### Likely cause

Docker Desktop is installed, but Docker Desktop has not finished starting, or its Docker Engine is not currently running.

### Next steps

1. Start Docker Desktop from the Windows Start menu.
2. Wait until Docker Desktop finishes starting and shows that Docker is running.
3. Open a new PowerShell window.
4. Check whether the Docker Engine is available:

```powershell
docker info
```

5. If `docker info` succeeds, verify Docker Compose as well:

```powershell
docker compose version
```

6. If Docker Desktop still cannot start the engine, check Docker Desktop's settings and confirm that the WSL 2 based engine is enabled, as recommended by the ForgeCloud setup instructions.

Do not remove Docker volumes or run cleanup commands that delete volumes while troubleshooting. ForgeCloud stores PostgreSQL data in a Docker volume, so deleting that volume can remove local database data.

## `POSTGRES_PASSWORD` or `GITHUB_WEBHOOK_SECRET` is missing

### Error

Docker Compose reports an error similar to:

```text
Set POSTGRES_PASSWORD in .env
```

or:

```text
Set GITHUB_WEBHOOK_SECRET in .env
```

### Likely cause

The required environment variables are missing or empty in the local `.env` file.

ForgeCloud expects `.env` to be in the **same project root folder as `compose.yaml`**.

### Next steps

1. In PowerShell, make sure you are in the ForgeCloud project folder:

```powershell
Get-Location
```

2. Check that `compose.yaml` and `.env` are in the same folder:

```powershell
Get-ChildItem compose.yaml, .env
```

3. If `.env` does not exist, create it from the example file:

```powershell
Copy-Item .env.example .env
```

4. Open the `.env` file:

```powershell
notepad .env
```

5. Set your own local values for:

```text
POSTGRES_PASSWORD=your-local-password
GITHUB_WEBHOOK_SECRET=your-local-webhook-secret
```

Use your own values. Do not copy real credentials from another person or repository.

6. Save the file and run:

```powershell
docker compose up --build
```

### Security reminder

Never commit `.env`, paste its contents into an issue or pull request, or ask another person to share their secret values.

The repository's `.env.example` contains empty placeholders so each developer can create their own local values.

## Commands are run outside the project folder

### Error

Docker Compose cannot find the project configuration. For example:

```text
no configuration file provided: not found
```

or PowerShell cannot find files such as `.env` or `.env.example`.

### Likely cause

The command was run from a directory other than the ForgeCloud project folder.

ForgeCloud commands such as `docker compose up --build` expect to find `compose.yaml` in the current directory.

### Next steps

1. In PowerShell, check the current directory:

```powershell
Get-Location
```

2. List the files in the current directory:

```powershell
Get-ChildItem
```

You should see `compose.yaml` and other ForgeCloud project files.

3. If you are not in the ForgeCloud project folder, change to it. For example:

```powershell
Set-Location C:\path\to\ForgeCloud
```

Replace the path with the actual location where you cloned or extracted the ForgeCloud repository.

4. Confirm that `compose.yaml` is present:

```powershell
Get-ChildItem compose.yaml
```

5. Run the Docker Compose command from that folder:

```powershell
docker compose up --build
```

If you created `.env`, it should also be in this same project folder, beside `compose.yaml`.

## Maven is not installed locally

### Error

PowerShell reports an error such as:

```text
mvn : The term 'mvn' is not recognized as the name of a cmdlet, function, script file, or operable program.
```

### Likely cause

Apache Maven is not installed, or the Maven executable is not available on the system `PATH`.

ForgeCloud does not include a Maven wrapper (`mvnw`), so Maven must be installed separately if you want to run Maven commands locally.

### Next steps

1. Check whether Maven is available:

```powershell
mvn --version
```

2. If PowerShell reports that `mvn` is not recognized, install Maven using the official [Apache Maven installation instructions](https://maven.apache.org/install).
3. Make sure the Java Development Kit (JDK) required by the project is installed. ForgeCloud currently specifies Java 21 in `pom.xml`.
4. Open a new PowerShell window after installing Maven so the updated `PATH` is available.
5. Check Maven again:

```powershell
mvn --version
```

6. From the ForgeCloud project folder, you can then run the project's Maven checks:

```powershell
mvn test
```

If you only want to run ForgeCloud through Docker, follow the Docker setup instructions first. You do not need to install Maven just to run the Docker Compose setup.

## Pipelines remain `QUEUED`

### Symptom

A pipeline is created successfully, but its status remains:

```text
QUEUED
```

### Likely cause

This is expected behavior in the current ForgeCloud implementation.

ForgeCloud currently persists the pipeline request, but a worker that clones repositories and executes user code has not been implemented yet. A `QUEUED` status therefore does not necessarily indicate a local setup problem.

### Next steps

1. Confirm that the ForgeCloud services started successfully.
2. Check the application logs for startup errors if the application itself is not working.
3. If the application is running and a newly created pipeline remains `QUEUED`, no local Docker or Maven troubleshooting is required just because of that status.
4. Refer to the project's current documentation for the implementation status of pipeline execution.

### Current limitation

At this stage, `QUEUED` means that ForgeCloud has persisted the pipeline work and is waiting for functionality that is planned for a future worker.

The current application does not yet clone the repository or execute user code. Do not treat a `QUEUED` pipeline by itself as evidence that Docker, PostgreSQL, or the local setup is broken.
