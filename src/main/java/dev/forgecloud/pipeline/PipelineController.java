package dev.forgecloud.pipeline;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PipelineController {

    private final PipelineService service;

    public PipelineController(PipelineService service) {
        this.service = service;
    }

    @GetMapping("/pipelines")
    public Map<String, List<PipelineResponse>> list() {
        return Map.of("pipelines", service.list());
    }

    @GetMapping("/pipelines/{pipelineId}/steps")
    public Map<String, List<PipelineStepResponse>> listSteps(
        @PathVariable UUID pipelineId
    ) {
        return Map.of("steps", service.listSteps(pipelineId));
    }

    @PostMapping("/projects/{projectId}/pipelines")
    public ResponseEntity<Map<String, PipelineResponse>> trigger(
        @PathVariable UUID projectId
    ) {
        PipelineResponse pipeline = service.manual(projectId);

        return ResponseEntity
            .created(URI.create("/api/v1/pipelines/" + pipeline.id()))
            .body(Map.of("pipeline", pipeline));
    }
}