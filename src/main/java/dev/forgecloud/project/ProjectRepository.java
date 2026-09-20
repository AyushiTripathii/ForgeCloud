package dev.forgecloud.project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByRepositoryFullNameIgnoreCase(String repositoryFullName);
    boolean existsByRepositoryFullNameIgnoreCase(String repositoryFullName);
}

