package dev.forgecloud.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PipelineStepRepository
        extends JpaRepository<PipelineStep, UUID> {

    List<PipelineStep> findByPipelineRun_IdOrderByStepOrderAsc(
        UUID pipelineRunId
    );
}