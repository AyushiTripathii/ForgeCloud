package dev.forgecloud.pipeline;

import dev.forgecloud.project.Project;
import dev.forgecloud.project.ProjectRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate",
    "forgecloud.worker.enabled=false"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PipelineClaimService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PipelineClaimServiceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("forgecloud_worker_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            POSTGRES::getJdbcUrl
        );
        registry.add(
            "spring.datasource.username",
            POSTGRES::getUsername
        );
        registry.add(
            "spring.datasource.password",
            POSTGRES::getPassword
        );
        registry.add(
            "spring.datasource.driver-class-name",
            () -> "org.postgresql.Driver"
        );
    }

    @Autowired
    private PipelineClaimService claims;

    @Autowired
    private PipelineRepository pipelines;

    @Autowired
    private PipelineStepRepository steps;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactions;

    @BeforeEach
    void cleanDatabase() {
        transactions = new TransactionTemplate(transactionManager);

        // These repositories use only the temporary test database.
        transactions.executeWithoutResult(status -> {
            steps.deleteAllInBatch();
            pipelines.deleteAllInBatch();
            projects.deleteAllInBatch();
        });
    }

    @Test
    void emptyQueueReturnsNoPipeline() {
        assertThat(claims.claimNext()).isEmpty();
    }

    @Test
    void claimPersistsRunningStatusAndStartTime() {
        UUID id = createPipeline(true);
        Instant before = Instant.now();

        assertThat(claims.claimNext()).contains(id);

        Instant after = Instant.now();
        PipelineRun saved = pipelines.findById(id).orElseThrow();

        assertThat(saved.getStatus())
            .isEqualTo(PipelineStatus.RUNNING);

        // PostgreSQL timestamps have microsecond precision.
        assertThat(saved.getStartedAt()).isNotNull();
        assertThat(saved.getStartedAt())
            .isBetween(before.minusMillis(1), after.plusMillis(1));

        assertThat(saved.getFinishedAt()).isNull();
    }

    @Test
    void claimsOldestEligiblePipelineFirst() {
        UUID newer = createPipeline(true);
        UUID older = createPipeline(true);

        setCreatedAt(older, Instant.parse("2026-01-01T00:00:00Z"));
        setCreatedAt(newer, Instant.parse("2026-01-02T00:00:00Z"));

        assertThat(claims.claimNext()).contains(older);
        assertThat(claims.claimNext()).contains(newer);
        assertThat(claims.claimNext()).isEmpty();
    }

    @Test
    void runningPipelineIsNotClaimedAgain() {
        UUID id = createPipeline(true);

        assertThat(claims.claimNext()).contains(id);
        assertThat(claims.claimNext()).isEmpty();

        assertThat(pipelines.findById(id).orElseThrow().getStatus())
            .isEqualTo(PipelineStatus.RUNNING);
    }

    @Test
    void skipsOlderPipelineWithoutSteps() {
        UUID withoutSteps = createPipeline(false);
        UUID withSteps = createPipeline(true);

        setCreatedAt(
            withoutSteps,
            Instant.parse("2026-01-01T00:00:00Z")
        );
        setCreatedAt(
            withSteps,
            Instant.parse("2026-01-02T00:00:00Z")
        );

        assertThat(claims.claimNext()).contains(withSteps);
        assertThat(claims.claimNext()).isEmpty();

        PipelineRun skipped =
            pipelines.findById(withoutSteps).orElseThrow();

        assertThat(skipped.getStatus())
            .isEqualTo(PipelineStatus.QUEUED);
        assertThat(skipped.getStartedAt()).isNull();
    }

    @Test
    void secondWorkerSkipsPipelineLockedByFirstWorker() {
        UUID id = createPipeline(true);
        ExecutorService secondWorker =
            Executors.newSingleThreadExecutor();

        try {
            transactions.executeWithoutResult(transaction -> {
                // This claim joins the surrounding transaction,
                // keeping the row locked until this block finishes.
                assertThat(claims.claimNext()).contains(id);

                Future<Optional<UUID>> secondClaim =
                    secondWorker.submit(() -> claims.claimNext());

                try {
                    // A second transaction must skip the locked row.
                    // It must neither claim it nor wait for its release.
                    assertThat(secondClaim.get(10, TimeUnit.SECONDS))
                        .isEmpty();
                } catch (Exception exception) {
                    throw new AssertionError(
                        "The second worker did not skip the locked pipeline",
                        exception
                    );
                }
            });

            // The first transaction has now committed.
            assertThat(pipelines.findById(id).orElseThrow().getStatus())
                .isEqualTo(PipelineStatus.RUNNING);

            assertThat(claims.claimNext()).isEmpty();
        } finally {
            secondWorker.shutdownNow();
        }
    }

    private UUID createPipeline(boolean includeSteps) {
        return transactions.execute(transaction -> {
            String repositoryName = "test/repo-" + UUID.randomUUID();

            Project project = projects.save(
                new Project(
                    "Worker test",
                    "https://github.com/" + repositoryName,
                    repositoryName,
                    "main"
                )
            );

            PipelineRun pipeline = pipelines.save(
                new PipelineRun(
                    project,
                    null,
                    "main",
                    TriggerType.MANUAL,
                    null
                )
            );

            if (includeSteps) {
                steps.save(
                    new PipelineStep(
                        pipeline,
                        "Checkout repository",
                        1
                    )
                );
            }

            return pipeline.getId();
        });
    }

    private void setCreatedAt(UUID id, Instant createdAt) {
        jdbc.update(
            "UPDATE pipeline_runs SET created_at = ? WHERE id = ?",
            Timestamp.from(createdAt),
            id
        );
    }
}