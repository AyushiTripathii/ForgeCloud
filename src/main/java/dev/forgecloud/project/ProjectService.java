package dev.forgecloud.project;

import dev.forgecloud.common.BadRequestException;
import dev.forgecloud.common.ConflictException;
import dev.forgecloud.common.NotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) { this.projects = projects; }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projects.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Project getEntity(UUID id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        ParsedRepository parsed = parse(request.repositoryUrl());
        if (projects.existsByRepositoryFullNameIgnoreCase(parsed.fullName())) {
            throw new ConflictException("This GitHub repository is already connected");
        }
        String branch = normalizeBranch(request.branch());
        Project project = new Project(request.name().trim(), parsed.url(), parsed.fullName(), branch);
        return ProjectResponse.from(projects.save(project));
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = getEntity(id);
        ParsedRepository parsed = parse(request.repositoryUrl());
        projects.findByRepositoryFullNameIgnoreCase(parsed.fullName())
            .filter(other -> !other.getId().equals(id))
            .ifPresent(other -> { throw new ConflictException("This GitHub repository is already connected"); });
        project.update(request.name().trim(), parsed.url(), parsed.fullName(), normalizeBranch(request.branch()));
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = getEntity(id);
        projects.delete(project);
    }

    private String normalizeBranch(String value) {
        if (value == null || value.isBlank()) return "main";
        return value.trim();
    }

    static ParsedRepository parse(String raw) {
        try {
            URI uri = URI.create(raw.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || !"github.com".equals(uri.getHost().toLowerCase(Locale.ROOT))
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new BadRequestException("Use an HTTPS github.com repository URL");
            }
            String path = uri.getPath().replaceFirst("^/", "").replaceFirst("/$", "");
            if (path.endsWith(".git")) path = path.substring(0, path.length() - 4);
            String[] pieces = path.split("/");
            if (pieces.length != 2 || pieces[0].isBlank() || pieces[1].isBlank()) {
                throw new BadRequestException("Repository URL must contain an owner and repository");
            }
            String fullName = pieces[0] + "/" + pieces[1];
            return new ParsedRepository("https://github.com/" + fullName, fullName);
        } catch (IllegalArgumentException exception) {
            if (exception instanceof BadRequestException badRequest) throw badRequest;
            throw new BadRequestException("Repository URL is invalid");
        }
    }

    record ParsedRepository(String url, String fullName) {}
}

