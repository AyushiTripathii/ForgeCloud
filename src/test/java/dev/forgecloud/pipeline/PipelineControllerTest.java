package dev.forgecloud.pipeline;

import dev.forgecloud.common.ApiExceptionHandler;
import dev.forgecloud.project.ProjectRepository;
import dev.forgecloud.project.ProjectService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PipelineControllerTest {

    private PipelineRepository pipelines;
    private PipelineStepRepository steps;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pipelines = mock(PipelineRepository.class);
        steps = mock(PipelineStepRepository.class);

        ProjectRepository projects = mock(ProjectRepository.class);
        ProjectService projectService = mock(ProjectService.class);

        PipelineService service = new PipelineService(
            pipelines,
            projects,
            projectService,
            steps
        );

        mockMvc = MockMvcBuilders
            .standaloneSetup(new PipelineController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void returnsStepsInRepositoryOrder() throws Exception {
        UUID pipelineId = UUID.randomUUID();
        PipelineRun pipeline = mock(PipelineRun.class);

        PipelineStep checkout = new PipelineStep(
            pipeline, "Checkout repository", 1
        );

        PipelineStep tests = new PipelineStep(
            pipeline, "Run tests", 2
        );

        PipelineStep build = new PipelineStep(
            pipeline, "Build application", 3
        );

        when(pipelines.existsById(pipelineId)).thenReturn(true);

        when(steps.findByPipelineRun_IdOrderByStepOrderAsc(pipelineId))
            .thenReturn(List.of(checkout, tests, build));

        mockMvc.perform(
                get("/api/v1/pipelines/{pipelineId}/steps", pipelineId)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps", hasSize(3)))
            .andExpect(jsonPath("$.steps[0].id")
                .value(checkout.getId().toString()))
            .andExpect(jsonPath("$.steps[0].name")
                .value("Checkout repository"))
            .andExpect(jsonPath("$.steps[0].stepOrder").value(1))
            .andExpect(jsonPath("$.steps[0].status").value("PENDING"))
            .andExpect(jsonPath("$.steps[1].name").value("Run tests"))
            .andExpect(jsonPath("$.steps[1].stepOrder").value(2))
            .andExpect(jsonPath("$.steps[1].status").value("PENDING"))
            .andExpect(jsonPath("$.steps[2].name")
                .value("Build application"))
            .andExpect(jsonPath("$.steps[2].stepOrder").value(3))
            .andExpect(jsonPath("$.steps[2].status").value("PENDING"));

        verify(steps)
            .findByPipelineRun_IdOrderByStepOrderAsc(pipelineId);
    }

    @Test
    void unknownPipelineReturns404() throws Exception {
        UUID pipelineId = UUID.randomUUID();

        when(pipelines.existsById(pipelineId)).thenReturn(false);

        mockMvc.perform(
                get("/api/v1/pipelines/{pipelineId}/steps", pipelineId)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error.message")
                .value("Pipeline not found"));

        verifyNoInteractions(steps);
    }

    @Test
    void pipelineWithoutStepsReturnsEmptyArray() throws Exception {
        UUID pipelineId = UUID.randomUUID();

        when(pipelines.existsById(pipelineId)).thenReturn(true);

        when(steps.findByPipelineRun_IdOrderByStepOrderAsc(pipelineId))
            .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/pipelines/{pipelineId}/steps", pipelineId)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps", hasSize(0)));

        verify(steps)
            .findByPipelineRun_IdOrderByStepOrderAsc(pipelineId);
    }
}