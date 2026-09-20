package dev.forgecloud.project;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService service;
    public ProjectController(ProjectService service) { this.service = service; }

    @GetMapping
    public Map<String, List<ProjectResponse>> list() { return Map.of("projects", service.list()); }

    @GetMapping("/{id}")
    public Map<String, ProjectResponse> get(@PathVariable UUID id) {
        return Map.of("project", ProjectResponse.from(service.getEntity(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, ProjectResponse>> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse project = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + project.id())).body(Map.of("project", project));
    }

    @PutMapping("/{id}")
    public Map<String, ProjectResponse> update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return Map.of("project", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

