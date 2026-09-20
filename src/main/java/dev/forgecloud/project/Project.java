package dev.forgecloud.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {
    @Id private UUID id;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "repository_url", nullable = false, length = 500) private String repositoryUrl;
    @Column(name = "repository_full_name", nullable = false, unique = true, length = 200) private String repositoryFullName;
    @Column(nullable = false, length = 200) private String branch;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Project() {}

    public Project(String name, String repositoryUrl, String repositoryFullName, String branch) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.repositoryUrl = repositoryUrl;
        this.repositoryFullName = repositoryFullName;
        this.branch = branch;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String name, String repositoryUrl, String repositoryFullName, String branch) {
        this.name = name;
        this.repositoryUrl = repositoryUrl;
        this.repositoryFullName = repositoryFullName;
        this.branch = branch;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public String getRepositoryFullName() { return repositoryFullName; }
    public String getBranch() { return branch; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

