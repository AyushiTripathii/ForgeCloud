package dev.forgecloud.pipeline;

import dev.forgecloud.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_runs")
public class PipelineRun {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id") private Project project;
    @Column(name = "commit_sha", length = 64) private String commitSha;
    @Column(nullable = false, length = 200) private String branch;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private PipelineStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "trigger_type", nullable = false, length = 30) private TriggerType triggerType;
    @Column(name = "github_delivery_id", unique = true, length = 100) private String githubDeliveryId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "finished_at") private Instant finishedAt;

    protected PipelineRun() {}
    public PipelineRun(Project project, String commitSha, String branch, TriggerType triggerType, String deliveryId) {
        this.id = UUID.randomUUID(); this.project = project; this.commitSha = commitSha; this.branch = branch;
        this.status = PipelineStatus.QUEUED; this.triggerType = triggerType; this.githubDeliveryId = deliveryId;
        this.createdAt = Instant.now();
    }
    public void start() {
    if (this.status != PipelineStatus.QUEUED) {
        throw new IllegalStateException(
            "Only a queued pipeline can start"
        );
    }

    Instant now = Instant.now();
    this.status = PipelineStatus.RUNNING;
    this.startedAt = now;
}
    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public String getCommitSha() { return commitSha; }
    public String getBranch() { return branch; }
    public PipelineStatus getStatus() { return status; }
    public TriggerType getTriggerType() { return triggerType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
}

