CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    repository_url VARCHAR(500) NOT NULL,
    repository_full_name VARCHAR(200) NOT NULL,
    branch VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pipeline_runs (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    commit_sha VARCHAR(64),
    branch VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,
    trigger_type VARCHAR(30) NOT NULL,
    github_delivery_id VARCHAR(100) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ
);

CREATE INDEX idx_pipeline_project_created ON pipeline_runs(project_id, created_at DESC);
CREATE UNIQUE INDEX uq_project_repository_name ON projects(lower(repository_full_name));
