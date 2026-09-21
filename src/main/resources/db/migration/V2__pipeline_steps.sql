CREATE TABLE pipeline_steps (
    id UUID PRIMARY KEY,

    pipeline_run_id UUID NOT NULL
        REFERENCES pipeline_runs(id) ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,

    step_order INTEGER NOT NULL CHECK (step_order >= 1),

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN (
            'PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'SKIPPED'
        )),

    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,

    exit_code INTEGER,

    CONSTRAINT uq_pipeline_step_order
        UNIQUE (pipeline_run_id, step_order)
);