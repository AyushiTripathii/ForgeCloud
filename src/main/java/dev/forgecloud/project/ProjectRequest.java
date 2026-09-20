package dev.forgecloud.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 500) String repositoryUrl,
    @Size(max = 200) String branch
) {}

