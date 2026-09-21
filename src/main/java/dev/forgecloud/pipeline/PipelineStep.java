package dev.forgecloud.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "pipeline_steps",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pipeline_step_order",
        columnNames = {"pipeline_run_id", "step_order"}
    )
)
public class PipelineStep {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_run_id", nullable = false)
    private PipelineRun pipelineRun;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PipelineStepStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "exit_code")
    private Integer exitCode;

    protected PipelineStep() {
        // Required by JPA.
    }

    public PipelineStep(
        PipelineRun pipelineRun,
        String name,
        int stepOrder
    ) {
        if (pipelineRun == null) {
            throw new IllegalArgumentException("Pipeline run is required");
        }

        if (name == null || name.isBlank() || name.length() > 100) {
            throw new IllegalArgumentException(
                "Step name must contain 1–100 characters"
            );
        }

        if (stepOrder < 1) {
            throw new IllegalArgumentException(
                "Step order must be at least 1"
            );
        }

        this.id = UUID.randomUUID();
        this.pipelineRun = pipelineRun;
        this.name = name;
        this.stepOrder = stepOrder;
        this.status = PipelineStepStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PipelineRun getPipelineRun() {
        return pipelineRun;
    }

    public String getName() {
        return name;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public PipelineStepStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Integer getExitCode() {
        return exitCode;
    }
}