package dev.forgecloud.project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String repositoryUrl,
    String repositoryFullName,
    String branch,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getRepositoryUrl(),
            project.getRepositoryFullName(), project.getBranch(), project.getCreatedAt(), project.getUpdatedAt());
    }
}

