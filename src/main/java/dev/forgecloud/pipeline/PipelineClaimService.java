package dev.forgecloud.pipeline;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineClaimService {

    private final PipelineRepository pipelines;

    public PipelineClaimService(PipelineRepository pipelines) {
        this.pipelines = pipelines;
    }

    @Transactional
    public Optional<UUID> claimNext() {
        return pipelines.findNextQueuedForUpdate()
            .map(pipeline -> {
                pipeline.start();
                return pipeline.getId();
            });
    }
}