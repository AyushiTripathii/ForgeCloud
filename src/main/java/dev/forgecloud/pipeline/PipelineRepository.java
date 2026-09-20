package dev.forgecloud.pipeline;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRepository extends JpaRepository<PipelineRun, UUID> {
    List<PipelineRun> findTop50ByOrderByCreatedAtDesc();
    Optional<PipelineRun> findByGithubDeliveryId(String deliveryId);
}

