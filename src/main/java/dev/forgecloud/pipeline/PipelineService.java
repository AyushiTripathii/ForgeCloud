package dev.forgecloud.pipeline;

import dev.forgecloud.common.NotFoundException;
import dev.forgecloud.project.Project;
import dev.forgecloud.project.ProjectRepository;
import dev.forgecloud.project.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineService {

    private final PipelineRepository pipelines;
    private final ProjectRepository projects;
    private final ProjectService projectService;
    private final PipelineStepRepository steps;

    public PipelineService(
        PipelineRepository pipelines,
        ProjectRepository projects,
        ProjectService projectService,
        PipelineStepRepository steps
    ) {
        this.pipelines = pipelines;
        this.projects = projects;
        this.projectService = projectService;
        this.steps = steps;
    }

    @Transactional(readOnly = true)
    public List<PipelineResponse> list() {
        return pipelines.findTop50ByOrderByCreatedAtDesc()
            .stream()
            .map(PipelineResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PipelineStepResponse> listSteps(UUID pipelineId) {
        if (!pipelines.existsById(pipelineId)) {
            throw new NotFoundException("Pipeline not found");
        }

        return steps.findByPipelineRun_IdOrderByStepOrderAsc(pipelineId)
            .stream()
            .map(PipelineStepResponse::from)
            .toList();
    }

    @Transactional
    public PipelineResponse manual(UUID projectId) {
        Project project = projectService.getEntity(projectId);

        PipelineRun pipeline = createPipelineWithSteps(
            project,
            null,
            project.getBranch(),
            TriggerType.MANUAL,
            null
        );

        return PipelineResponse.from(pipeline);
    }

    @Transactional
    public PipelineResponse githubPush(
        String repositoryFullName,
        String branch,
        String sha,
        String deliveryId
    ) {
        return pipelines.findByGithubDeliveryId(deliveryId)
            .map(PipelineResponse::from)
            .orElseGet(() -> {
                Project project = projects
                    .findByRepositoryFullNameIgnoreCase(repositoryFullName)
                    .orElseThrow(() -> new NotFoundException(
                        "No connected project matches this repository"
                    ));

                PipelineRun pipeline = createPipelineWithSteps(
                    project,
                    sha,
                    branch,
                    TriggerType.GITHUB_PUSH,
                    deliveryId
                );

                return PipelineResponse.from(pipeline);
            });
    }

    private PipelineRun createPipelineWithSteps(
        Project project,
        String sha,
        String branch,
        TriggerType triggerType,
        String deliveryId
    ) {
        PipelineRun pipeline = pipelines.save(
            new PipelineRun(
                project,
                sha,
                branch,
                triggerType,
                deliveryId
            )
        );

        steps.saveAll(List.of(
            new PipelineStep(pipeline, "Checkout repository", 1),
            new PipelineStep(pipeline, "Run tests", 2),
            new PipelineStep(pipeline, "Build application", 3)
        ));

        return pipeline;
    }
}