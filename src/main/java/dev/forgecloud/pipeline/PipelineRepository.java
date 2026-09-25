package dev.forgecloud.pipeline;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PipelineRepository
        extends JpaRepository<PipelineRun, UUID> {

    List<PipelineRun> findTop50ByOrderByCreatedAtDesc();

    Optional<PipelineRun> findByGithubDeliveryId(String deliveryId);

    @Query(value = """
        SELECT p.*
        FROM pipeline_runs p
        WHERE p.status = 'QUEUED'
          AND EXISTS (
              SELECT 1
              FROM pipeline_steps s
              WHERE s.pipeline_run_id = p.id
          )
        ORDER BY p.created_at ASC, p.id ASC
        LIMIT 1
        FOR UPDATE OF p SKIP LOCKED
        """, nativeQuery = true)
    Optional<PipelineRun> findNextQueuedForUpdate();
}