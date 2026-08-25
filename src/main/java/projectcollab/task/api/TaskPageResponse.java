package projectcollab.task.api;

import org.springframework.data.domain.Page;
import projectcollab.task.domain.Task;

import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static TaskPageResponse from(Page<Task> tasks) {
        return new TaskPageResponse(
                tasks.getContent()
                        .stream()
                        .map(TaskResponse::from)
                        .toList(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.isFirst(),
                tasks.isLast()
        );
    }
}
