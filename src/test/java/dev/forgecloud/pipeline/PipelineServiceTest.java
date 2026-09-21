package dev.forgecloud.pipeline;

import dev.forgecloud.project.Project;
import dev.forgecloud.project.ProjectRepository;
import dev.forgecloud.project.ProjectService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelines;

    @Mock
    private ProjectRepository projects;

    @Mock
    private ProjectService projectService;

    @Mock
    private PipelineStepRepository steps;

    @Mock
    private Project project;

    @Captor
    private ArgumentCaptor<List<PipelineStep>> stepsCaptor;

    private PipelineService service;

    @BeforeEach
    void setUp() {
        service = new PipelineService(
            pipelines,
            projects,
            projectService,
            steps
        );
    }

    @Test
    void manualPipelineCreatesThreePendingSteps() {
        UUID projectId = UUID.randomUUID();

        when(projectService.getEntity(projectId))
            .thenReturn(project);

        when(project.getBranch()).thenReturn("main");

        when(pipelines.save(any(PipelineRun.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PipelineResponse response = service.manual(projectId);

        assertEquals(PipelineStatus.QUEUED, response.status());
        assertEquals(TriggerType.MANUAL, response.triggerType());

        verify(pipelines).save(any(PipelineRun.class));
        verify(steps).saveAll(stepsCaptor.capture());

        assertDefaultSteps(stepsCaptor.getValue(), response.id());
    }

    @Test
    void githubPushCreatesThreePendingSteps() {
        String repository = "example/demo";
        String deliveryId = "delivery-001";

        when(pipelines.findByGithubDeliveryId(deliveryId))
            .thenReturn(Optional.empty());

        when(projects.findByRepositoryFullNameIgnoreCase(repository))
            .thenReturn(Optional.of(project));

        when(pipelines.save(any(PipelineRun.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PipelineResponse response = service.githubPush(
            repository,
            "main",
            "abc123",
            deliveryId
        );

        assertEquals(PipelineStatus.QUEUED, response.status());
        assertEquals(TriggerType.GITHUB_PUSH, response.triggerType());
        assertEquals("abc123", response.commitSha());

        verify(pipelines).save(any(PipelineRun.class));
        verify(steps).saveAll(stepsCaptor.capture());

        assertDefaultSteps(stepsCaptor.getValue(), response.id());
    }

    @Test
    void existingGithubDeliveryDoesNotCreateAnotherPipelineOrSteps() {
        String deliveryId = "delivery-001";

        PipelineRun existing = new PipelineRun(
            project,
            "abc123",
            "main",
            TriggerType.GITHUB_PUSH,
            deliveryId
        );

        when(pipelines.findByGithubDeliveryId(deliveryId))
            .thenReturn(Optional.of(existing));

        PipelineResponse response = service.githubPush(
            "example/demo",
            "main",
            "abc123",
            deliveryId
        );

        assertEquals(existing.getId(), response.id());

        verify(pipelines, never()).save(any(PipelineRun.class));
        verifyNoInteractions(steps, projects, projectService);
    }

    private void assertDefaultSteps(
        List<PipelineStep> savedSteps,
        UUID pipelineId
    ) {
        assertEquals(3, savedSteps.size());

        assertEquals(
            List.of(
                "Checkout repository",
                "Run tests",
                "Build application"
            ),
            savedSteps.stream().map(PipelineStep::getName).toList()
        );

        assertEquals(
            List.of(1, 2, 3),
            savedSteps.stream().map(PipelineStep::getStepOrder).toList()
        );

        for (PipelineStep step : savedSteps) {
            assertEquals(pipelineId, step.getPipelineRun().getId());
            assertEquals(PipelineStepStatus.PENDING, step.getStatus());

            assertNotNull(step.getId());
            assertNotNull(step.getCreatedAt());

            assertNull(step.getStartedAt());
            assertNull(step.getFinishedAt());
            assertNull(step.getExitCode());
        }

        assertEquals(
            3L,
            savedSteps.stream()
                .map(PipelineStep::getId)
                .distinct()
                .count()
        );
    }
}