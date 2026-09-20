package dev.forgecloud.pipeline;

import java.time.Instant;
import java.util.UUID;

public record PipelineResponse(UUID id, UUID projectId, String projectName, String commitSha, String branch,
                               PipelineStatus status, TriggerType triggerType, Instant createdAt) {
    static PipelineResponse from(PipelineRun run) {
        return new PipelineResponse(run.getId(), run.getProject().getId(), run.getProject().getName(),
            run.getCommitSha(), run.getBranch(), run.getStatus(), run.getTriggerType(), run.getCreatedAt());
    }
}

