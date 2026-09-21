package dev.forgecloud.pipeline;

import java.time.Instant;
import java.util.UUID;

public record PipelineStepResponse(
    UUID id,
    String name,
    int stepOrder,
    PipelineStepStatus status,
    Instant createdAt,
    Instant startedAt,
    Instant finishedAt,
    Integer exitCode
) {
    static PipelineStepResponse from(PipelineStep step) {
        return new PipelineStepResponse(
            step.getId(),
            step.getName(),
            step.getStepOrder(),
            step.getStatus(),
            step.getCreatedAt(),
            step.getStartedAt(),
            step.getFinishedAt(),
            step.getExitCode()
        );
    }
}