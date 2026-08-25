package projectcollab.task.api;

import projectcollab.task.domain.Task;
import projectcollab.task.domain.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        Long assigneeId,
        String assigneeName,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TaskResponse from(Task task) {
        Long assigneeId = task.getAssignee() == null
                ? null
                : task.getAssignee().getId();
        String assigneeName = task.getAssignee() == null
                ? null
                : task.getAssignee().getName();

        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                assigneeId,
                assigneeName,
                task.getVersion(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
