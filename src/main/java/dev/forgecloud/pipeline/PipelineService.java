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

    public PipelineService(PipelineRepository pipelines, ProjectRepository projects, ProjectService projectService) {
        this.pipelines = pipelines; this.projects = projects; this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<PipelineResponse> list() {
        return pipelines.findTop50ByOrderByCreatedAtDesc().stream().map(PipelineResponse::from).toList();
    }

    @Transactional
    public PipelineResponse manual(UUID projectId) {
        Project project = projectService.getEntity(projectId);
        return PipelineResponse.from(pipelines.save(new PipelineRun(project, null, project.getBranch(), TriggerType.MANUAL, null)));
    }

    @Transactional
    public PipelineResponse githubPush(String repositoryFullName, String branch, String sha, String deliveryId) {
        return pipelines.findByGithubDeliveryId(deliveryId).map(PipelineResponse::from).orElseGet(() -> {
            Project project = projects.findByRepositoryFullNameIgnoreCase(repositoryFullName)
                .orElseThrow(() -> new NotFoundException("No connected project matches this repository"));
            return PipelineResponse.from(pipelines.save(
                new PipelineRun(project, sha, branch, TriggerType.GITHUB_PUSH, deliveryId)));
        });
    }
}

