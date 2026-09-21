package dev.forgecloud.pipeline;

import dev.forgecloud.project.Project;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:pipeline-step-tests",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.ANY
)
class PipelineStepRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PipelineStepRepository steps;

    @Test
    void retrievesOnlyRequestedPipelineStepsInAscendingOrder() {
        Project project = new Project(
            "Test project",
            "https://github.com/example/demo",
            "example/demo",
            "main"
        );
        entityManager.persist(project);

        PipelineRun firstPipeline = new PipelineRun(
            project,
            null,
            "main",
            TriggerType.MANUAL,
            null
        );

        PipelineRun secondPipeline = new PipelineRun(
            project,
            null,
            "main",
            TriggerType.MANUAL,
            null
        );

        entityManager.persist(firstPipeline);
        entityManager.persist(secondPipeline);

        // Deliberately insert steps in the wrong execution order.
        PipelineStep build = new PipelineStep(
            firstPipeline, "Build application", 3
        );
        PipelineStep checkout = new PipelineStep(
            firstPipeline, "Checkout repository", 1
        );
        PipelineStep tests = new PipelineStep(
            firstPipeline, "Run tests", 2
        );

        entityManager.persist(build);
        entityManager.persist(checkout);
        entityManager.persist(tests);

        // This step belongs to a different pipeline.
        PipelineStep otherStep = new PipelineStep(
            secondPipeline, "Other pipeline step", 1
        );
        entityManager.persist(otherStep);

        // Write the rows, then clear cached entities.
        entityManager.flush();
        entityManager.clear();

        List<PipelineStep> result =
            steps.findByPipelineRun_IdOrderByStepOrderAsc(
                firstPipeline.getId()
            );

        assertThat(result).hasSize(3);

        assertThat(result)
            .extracting(PipelineStep::getStepOrder)
            .containsExactly(1, 2, 3);

        assertThat(result)
            .extracting(PipelineStep::getName)
            .containsExactly(
                "Checkout repository",
                "Run tests",
                "Build application"
            );

        assertThat(result)
            .extracting(PipelineStep::getId)
            .containsExactly(
                checkout.getId(),
                tests.getId(),
                build.getId()
            );

        assertThat(result).allSatisfy(step ->
            assertThat(step.getPipelineRun().getId())
                .isEqualTo(firstPipeline.getId())
        );
    }
}