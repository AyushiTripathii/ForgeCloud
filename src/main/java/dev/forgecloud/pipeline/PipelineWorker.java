package dev.forgecloud.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "forgecloud.worker.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class PipelineWorker {

    private static final Logger log =
        LoggerFactory.getLogger(PipelineWorker.class);

    private final PipelineClaimService claims;

    public PipelineWorker(PipelineClaimService claims) {
        this.claims = claims;
    }

    @Scheduled(
        fixedDelayString = "${forgecloud.worker.poll-interval-ms:5000}",
        initialDelayString = "${forgecloud.worker.poll-interval-ms:5000}"
    )
    public void poll() {
        claims.claimNext().ifPresent(pipelineId ->
            log.info("Claimed pipeline {}", pipelineId)
        );
    }
}