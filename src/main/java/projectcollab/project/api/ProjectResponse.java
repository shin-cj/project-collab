package projectcollab.project.api;

import projectcollab.project.domain.Project;
import projectcollab.project.domain.Role;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Role myRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project,Role myRole){
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                myRole,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
